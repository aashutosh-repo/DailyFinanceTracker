from langchain_ollama import ChatOllama
import os


llm = ChatOllama(
    model=os.getenv("OLLAMA_CHAT_MODEL","qwen2.5-coder:7b"),
    base_url=os.getenv("OLLAMA_BASE_URL","http://localhost:11434"),
    temperature=0
)