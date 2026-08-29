from fastapi import FastAPI
from pydantic import BaseModel

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