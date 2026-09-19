package com.rmos.dto.knowledge;

import java.util.List;

public class KnowledgeSearchResponse {
    private List<ResultItem> results;

    public KnowledgeSearchResponse(List<ResultItem> results) {
        this.results = results;
    }

    public List<ResultItem> getResults() {
        return results;
    }

    public static class ResultItem {
        public String documentId;
        public String chunkId;
        public String content;
        public double similarity;
        public String sourceReference;
        public String documentType;
        public String publishedAt;
    }
}
