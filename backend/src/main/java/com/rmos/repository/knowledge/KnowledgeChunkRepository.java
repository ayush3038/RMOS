package com.rmos.repository.knowledge;

import com.rmos.domain.knowledge.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {
    List<KnowledgeChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);
}
