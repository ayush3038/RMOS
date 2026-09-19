package com.rmos.repository.knowledge;

import com.rmos.domain.knowledge.KnowledgeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KnowledgeEmbeddingRepository extends JpaRepository<KnowledgeEmbedding, UUID> {
}
