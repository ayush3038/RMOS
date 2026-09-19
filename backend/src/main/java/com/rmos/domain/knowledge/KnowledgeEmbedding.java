package com.rmos.domain.knowledge;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_embeddings")
public class KnowledgeEmbedding {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private KnowledgeChunk chunk;

    // Persisted using raw JSON or typed bindings depending on driver compat.
    // For direct vector insertions using Spring Data JPA, many opt for native
    // queries
    // or string mapping for the pgvector type instead of heavy custom types if
    // avoiding hibernate-vector to keep backward compatibility simple.
    // The pgvector JDBC driver uses PGobject or float[]. We will use float[] arrays
    // for standard mapping?
    // Wait, since we import pgvector driver, pgvector maps "com.pgvector.PGvector"
    // object natively or float[].
    // Let's use float[] for the embedding vector representation in Entity, and
    // configure a dialect/type or use String if easiest.
    // To minimize risks with arbitrary hibernate versions, standard JDBC maps
    // PostgreSQL vector to float[]. Wait.
    // We will leave the mapping as float[] but insert via native JDBC/repository if
    // needed, or register PGvector type.
    @Column(name = "embedding", columnDefinition = "vector")
    private float[] embedding;

    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    @Column(name = "embedding_dimensions", nullable = false)
    private int embeddingDimensions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public KnowledgeEmbedding() {
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public KnowledgeChunk getChunk() {
        return chunk;
    }

    public void setChunk(KnowledgeChunk chunk) {
        this.chunk = chunk;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    public void setEmbeddingDimensions(int embeddingDimensions) {
        this.embeddingDimensions = embeddingDimensions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
