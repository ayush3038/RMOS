package com.rmos.repository.knowledge;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class VectorSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public VectorSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class VectorSearchResult {
        public UUID documentId;
        public UUID chunkId;
        public String content;
        public double similarity;
        public String sourceReference;
        public String documentType;
        public String publishedAt;

        // standard no-arg / mapping logic inside RowMapper
    }

    public List<VectorSearchResult> searchKnowledge(float[] queryVector, int topK, List<String> allowedScopes,
            String documentTypeFilter) {
        // Build raw vector array string for Postgres eg. "[0.1, 0.2, ...]"
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < queryVector.length; i++) {
            sb.append(queryVector[i]);
            if (i < queryVector.length - 1)
                sb.append(",");
        }
        sb.append("]");
        String vectorStr = sb.toString();

        StringBuilder query = new StringBuilder("""
                    SELECT
                        d.id AS document_id,
                        c.id AS chunk_id,
                        c.content AS content,
                        1 - (e.embedding <=> ?::vector) AS similarity,
                        d.source_reference,
                        d.document_type,
                        CAST(d.published_at AS VARCHAR) AS published_at
                    FROM knowledge_embeddings e
                    JOIN knowledge_chunks c ON e.chunk_id = c.id
                    JOIN knowledge_documents d ON c.document_id = d.id
                    WHERE 1=1
                """);

        // Auth
        if (allowedScopes != null && !allowedScopes.isEmpty()) {
            query.append(" AND d.access_scope IN (");
            for (int i = 0; i < allowedScopes.size(); i++) {
                query.append("'").append(allowedScopes.get(i).replace("'", "''")).append("'");
                if (i < allowedScopes.size() - 1)
                    query.append(",");
            }
            query.append(") ");
        } else {
            // Implicit deny if scopes are completely empty and auth is strictly enforced.
            return List.of();
        }

        if (documentTypeFilter != null && !documentTypeFilter.isEmpty()) {
            query.append(" AND d.document_type = '").append(documentTypeFilter.replace("'", "''")).append("' ");
        }

        query.append(" ORDER BY e.embedding <=> ?::vector LIMIT ?");

        return jdbcTemplate.query(query.toString(), new RowMapper<VectorSearchResult>() {
            @Override
            public VectorSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                VectorSearchResult result = new VectorSearchResult();
                result.documentId = (UUID) rs.getObject("document_id");
                result.chunkId = (UUID) rs.getObject("chunk_id");
                result.content = rs.getString("content");
                result.similarity = rs.getDouble("similarity");
                result.sourceReference = rs.getString("source_reference");
                result.documentType = rs.getString("document_type");
                result.publishedAt = rs.getString("published_at");
                return result;
            }
        }, vectorStr, vectorStr, topK);
    }
}
