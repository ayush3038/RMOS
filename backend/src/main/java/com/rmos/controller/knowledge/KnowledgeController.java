package com.rmos.controller.knowledge;

import com.rmos.dto.knowledge.KnowledgeSearchRequest;
import com.rmos.dto.knowledge.KnowledgeSearchResponse;
import com.rmos.domain.auth.Role;
import com.rmos.repository.knowledge.VectorSearchRepository.VectorSearchResult;
import com.rmos.service.knowledge.KnowledgeRetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('VIEWER', 'PLANNER', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<KnowledgeSearchResponse> search(@RequestBody KnowledgeSearchRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Scope mapping based on Role authorization principle to respect authorization
        // scope natively.
        List<String> allowedScopes = List.of("PUBLIC", "INTERNAL");

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")
                || a.getAuthority().equals("REVIEWER") || a.getAuthority().equals("PLANNER"))) {
            allowedScopes = List.of("PUBLIC", "INTERNAL", "CONFIDENTIAL"); // Or appropriate logic
        }

        String documentType = null;
        if (request.getFilters() != null && request.getFilters().containsKey("documentType")) {
            documentType = request.getFilters().get("documentType");
        }

        List<VectorSearchResult> rawResults = retrievalService.retrieve(request.getQuery(), request.getTopK(),
                allowedScopes, documentType);

        List<KnowledgeSearchResponse.ResultItem> items = rawResults.stream().map(raw -> {
            KnowledgeSearchResponse.ResultItem item = new KnowledgeSearchResponse.ResultItem();
            item.documentId = raw.documentId.toString();
            item.chunkId = raw.chunkId.toString();
            item.content = raw.content;
            item.similarity = raw.similarity;
            item.sourceReference = raw.sourceReference;
            item.documentType = raw.documentType;
            item.publishedAt = raw.publishedAt;
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new KnowledgeSearchResponse(items));
    }
}
