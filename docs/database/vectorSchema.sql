Docker Commands for pgvector Database Setup:
1. docker exec -it pgvector-db psql -U postgres
--open psql terminal and run the following SQL commands:
2. CREATE DATABASE pgvector_db;
3. \c pgvector_db
4. CREATE EXTENSION vector;
5. CREATE TABLE documents (
       embedding_id UUID PRIMARY KEY,
       embedding VECTOR(1024),
       text TEXT,
       metadata JSONB
   );
6. INSERT INTO documents (embedding_id, embedding, text, metadata) VALUES
   ('123e4567-e89b-12d3-a456-426614174000', '[0.1, 0.2, ..., 0.1024]', 'Sample document text', '{"author": "John Doe", "date": "2024-06-01"}');
7. CREATE INDEX ON documents
   USING hnsw (embedding vector_cosine_ops);