package com.rmos.service.knowledge;

import com.rmos.domain.knowledge.KnowledgeChunk;
import com.rmos.domain.knowledge.KnowledgeDocument;
import com.rmos.domain.knowledge.KnowledgeEmbedding;
import com.rmos.repository.knowledge.KnowledgeChunkRepository;
import com.rmos.repository.knowledge.KnowledgeDocumentRepository;
import com.rmos.repository.knowledge.KnowledgeEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeIngestionService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeEmbeddingRepository embeddingRepository;
    private final KnowledgeChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public KnowledgeIngestionService(KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            KnowledgeEmbeddingRepository embeddingRepository,
            KnowledgeChunkingService chunkingService,
            EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingRepository = embeddingRepository;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public KnowledgeDocument ingest(String title, String content, String documentType, String sourceSystem,
            String sourceReference, String accessScope) {

        String contentHash = computeHash(content);

        // Idempotency: return existing doc if hash matches.
        return documentRepository.findByContentHash(contentHash).orElseGet(() -> {
            KnowledgeDocument doc = new KnowledgeDocument();
            doc.setTitle(title);
            doc.setDocumentType(documentType);
            doc.setSourceSystem(sourceSystem);
            doc.setSourceReference(sourceReference);
            doc.setAccessScope(accessScope);
            doc.setContentHash(contentHash);
            doc.setStatus("INGESTED");

            KnowledgeDocument savedDoc = documentRepository.save(doc);

            List<String> textChunks = chunkingService.chunkText(content);
            for (int i = 0; i < textChunks.size(); i++) {
                String text = textChunks.get(i);

                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocument(savedDoc);
                chunk.setChunkIndex(i);
                chunk.setContent(text);
                KnowledgeChunk savedChunk = chunkRepository.save(chunk);

                float[] embedding = embeddingService.generateEmbedding(text);

                KnowledgeEmbedding ke = new KnowledgeEmbedding();
                ke.setChunk(savedChunk);
                ke.setEmbedding(embedding);
                ke.setEmbeddingModel(embeddingService.getModelName());
                ke.setEmbeddingDimensions(embeddingService.getDimensions());
                embeddingRepository.save(ke);
            }
            return savedDoc;
        });
    }

    private String computeHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not hash content", e);
        }
    }
}
