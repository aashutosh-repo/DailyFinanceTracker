from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from app.services.analysis_jobs import get_analysis_job, submit_analyze_job
from app.services.assistant import ask_assistant



app = FastAPI(
    title="Stock AI Assistant"
)


class ChatRequest(BaseModel):

    question: str


@app.post("/api/ai/chat")
def chat(request: ChatRequest):

    return ask_assistant(
        request.question
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

