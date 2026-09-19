package com.rmos.service.knowledge;

import com.rmos.repository.knowledge.VectorSearchRepository;
import com.rmos.repository.knowledge.VectorSearchRepository.VectorSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeRetrievalService {

    private final EmbeddingService embeddingService;
    private final VectorSearchRepository vectorSearchRepository;

    public KnowledgeRetrievalService(EmbeddingService embeddingService, VectorSearchRepository vectorSearchRepository) {
        this.embeddingService = embeddingService;
        this.vectorSearchRepository = vectorSearchRepository;
    }

    public List<VectorSearchResult> retrieve(String query, int topK, List<String> allowedScopes,
            String documentTypeFilter) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }
        if (topK < 1 || topK > 100) {
            topK = 5;
        }

        try {
            float[] queryVector = embeddingService.generateEmbedding(query);
            return vectorSearchRepository.searchKnowledge(queryVector, topK, allowedScopes, documentTypeFilter);
        } catch (IllegalStateException e) {
            // Unavailable semantics handling. If vectors missing, we fallback gracefully to
            // empty.
            return List.of();
        }
    }
}
