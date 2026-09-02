from langchain_postgres import PGVector

from app.rag.embeddings import embeddings
import os


CONNECTION_STRING = os.getenv("VECTOR_DATABASE_URL", "postgresql+psycopg://postgres:postgres@localhost:5432/postgres"
)


COLLECTION_NAME = os.getenv("VECTOR_COLLECTION_NAME","stock_knowledge")


_vector_store = None

def is_vector_store_enabled() -> bool:
    return os.getenv("VECTOR_STORE_ENABLED",
                     os.getenv("APP_VECTOR_STORE_ENABLED", "false")
                     ).lower() == "true"

def get_vector_store(): 
    global _vector_store
    if not is_vector_store_enabled():
        raise RuntimeError("Vector store is not enabled. Please set VECTOR_STORE_ENABLED to true.")
    if _vector_store is None:
        _vector_store = PGVector(
            embeddings=embeddings,
            connection_string=CONNECTION_STRING,
            collection_name=COLLECTION_NAME,
            use_jsonb=True,
            create_extensions=os.getenv("VECTOR_STORE_CREATE_EXTENSIONS", "false").lower() == "true"
        )
    return _vector_store