CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    source_system VARCHAR(100) NOT NULL,
    source_reference VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    access_scope VARCHAR(100) NOT NULL,
    published_at TIMESTAMP,
    ingested_at TIMESTAMP NOT NULL,
    content_hash VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_embeddings (
    id UUID PRIMARY KEY,
    chunk_id UUID NOT NULL REFERENCES knowledge_chunks(id) ON DELETE CASCADE,
    embedding vector(${vector_dim}),
    embedding_model VARCHAR(100) NOT NULL,
    embedding_dimensions INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX knowledge_embeddings_embedding_idx ON knowledge_embeddings USING hnsw (embedding vector_cosine_ops);
