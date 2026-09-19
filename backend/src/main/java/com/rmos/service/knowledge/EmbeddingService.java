package com.rmos.service.knowledge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${rmos.ai.nim.base-url:http://localhost:8000}")
    private String nimBaseUrl;

    @Value("${rmos.ai.nim.api-key:}")
    private String nimApiKey;

    @Value("${EMBEDDING_PROVIDER:nvidia}")
    private String provider;

    @Value("${EMBEDDING_MODEL:nvidia/nv-embedqa-e5-v5}")
    private String modelName;

    @Value("${EMBEDDING_DIMENSIONS:1024}")
    private int dimensions;

    private final RestTemplate restTemplate;

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
    }

    public String getModelName() {
        return modelName;
    }

    public int getDimensions() {
        return dimensions;
    }

    /**
     * Reaches out to the configured NIM AI embeddings endpoint to generate float[].
     * If the API Key is empty or provider isn't NVIDIA, throws exception or returns
     * unavailable semantics.
     */
    public float[] generateEmbedding(String text) {
        if ("none".equalsIgnoreCase(provider) || nimApiKey == null || nimApiKey.isEmpty()) {
            throw new IllegalStateException(
                    "Embedding Provider is unavailable or not configured. Cannot process fake RAG.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(nimApiKey);

            // standard OpenAI spec payload body compatible with NIM
            Map<String, Object> body = Map.of(
                    "input", List.of(text),
                    "model", modelName,
                    "input_type", "passage" // specific hint for e5-v5 occasionally, safely ignored if not.
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(nimBaseUrl + "/embeddings", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // OpenAI payload standard extracts float arrays from `data[0].embedding`
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    List<Double> doubleList = (List<Double>) dataList.get(0).get("embedding");
                    float[] floats = new float[doubleList.size()];
                    for (int i = 0; i < doubleList.size(); i++) {
                        floats[i] = doubleList.get(i).floatValue();
                    }

                    if (floats.length != dimensions) {
                        throw new IllegalStateException("Generated vector dimension " + floats.length
                                + " does not match expected " + dimensions);
                    }
                    return floats;
                }
            }
            throw new RuntimeException("Embedding API returned unexpected result structure.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to reach embedding provider: " + e.getMessage(), e);
        }
    }
}
