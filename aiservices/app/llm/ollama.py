from langchain_ollama import ChatOllama


llm = ChatOllama(
    model="qwen2.5-coder:7b",
    base_url="http://localhost:11434",
    temperature=0
)