from langchain_postgres import PGVector

from app.rag.embeddings import embeddings


CONNECTION_STRING = (
    "postgresql+psycopg://"
    "postgres:postgres@localhost:5432/postgres"
)


COLLECTION_NAME = "stock_knowledge"


vector_store = PGVector(
    embeddings=embeddings,
    connection=CONNECTION_STRING,
    collection_name=COLLECTION_NAME,
    use_jsonb=True
)