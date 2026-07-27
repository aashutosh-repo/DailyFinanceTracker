-- enable pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- documents table for RAG
CREATE TABLE IF NOT EXISTS documents (
    embedding_id TEXT PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(1024)
);

-- index for similarity search
CREATE INDEX IF NOT EXISTS documents_embedding_idx
ON documents
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);