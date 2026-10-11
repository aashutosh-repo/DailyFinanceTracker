import logging
import re
from typing import Any, Literal

from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel, Field

from app.services.analysis_jobs import get_analysis_job, submit_analyze_job
from app.services.assistant import ask_assistant
from app.services.request_context import request_id_context
from starlette.middleware.cors import CORSMiddleware

app = FastAPI(
    title="Stock AI Assistant"
)
logger = logging.getLogger(__name__)


class ChatResponse(BaseModel):
    response: str
    tools_used: list[str] = Field(default_factory=list)
    tools_results: list[dict[str, Any]] = Field(default_factory=list)
    grounded: bool = False
    grounding_issues: list[str] = Field(default_factory=list)
    sources: list[str] = Field(default_factory=list)
    status: Literal["SUCCESS", "PARTIAL", "ERROR"] = "ERROR"
    evidence_sufficient: bool = False
    tools_executed: list[str] = Field(default_factory=list)
    provider_metadata: list[dict[str, Any]] = Field(default_factory=list)
    data_quality_limitations: list[str] = Field(default_factory=list)
    evidence_references: list[str] = Field(default_factory=list)
    synthetic_data: bool = False

app.add_middleware(
    CORSMiddleware,
    allow_origins =[
        "http://localhost:4200",
        "http://127.0.0.1:4200",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
    ]
)


@app.middleware("http")
async def attach_request_id(request: Request, call_next):
    supplied_id = request.headers.get("x-request-id", "")
    request_id = re.sub(r"[^A-Za-z0-9_.-]", "", supplied_id)[:128] or None
    token = request_id_context.set(request_id)
    try:
        response = await call_next(request)
        if request_id:
            response.headers["X-Request-ID"] = request_id
        return response
    finally:
        request_id_context.reset(token)

class ChatRequest(BaseModel):

    question: str


@app.post("/api/ai/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    try:
        return ask_assistant(request.question)
    except Exception as exc:
        logger.error(
            "AI chat request failed",
            extra={"request_id": request_id_context.get(), "failure_category": type(exc).__name__},
        )
        return ChatResponse(
            response="The requested analysis could not be completed because an internal service failed.",
            status="ERROR",
            grounded=False,
            grounding_issues=["The request failed before a validated analysis response was available."],
            data_quality_limitations=["No validated evidence is available."],
        )

@app.get("/api/ai/chat/jobs", status_code=202)
def submit_chat_jobs(request: ChatRequest):
    return submit_analyze_job(
        request.question
    )

@app.get("/api/ai/chat/jobs/{job_id}", status_code=202)
def get_chat_job(job_id: str):
    job = get_analysis_job(job_id)

    if job is None:
        raise HTTPException(status_code=404, detail="Job not found")

    return job

