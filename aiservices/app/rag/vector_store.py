from langchain_postgres import PGVector

from app.rag.embeddings import embeddings
import os


CONNECTION_STRING = os.getenv("VECTOR_DATABASE_URL", "postgresql+psycopg://postgres:postgres@localhost:5432/postgres"
)


COLLECTION_NAME = os.getenv("VECTOR_COLLECTION_NAME","stock_knowledge")


vector_store = PGVector(
    embeddings=embeddings,
    connection=CONNECTION_STRING,
    collection_name=COLLECTION_NAME,
    use_jsonb=True
)