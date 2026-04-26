package com.foundit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Generates 384-dimensional semantic embedding vectors using
 * HuggingFace Inference API (sentence-transformers/all-MiniLM-L6-v2).
 *
 * These vectors enable cosine similarity matching between items —
 * semantically similar descriptions score high even if they use different words.
 * e.g. "navy blue wallet" ≈ "dark blue purse"
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Free HuggingFace Inference API — 384-dim MiniLM model
    private static final String HF_API_URL =
        "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";

    @Value("${huggingface.api.key:}")
    private String hfApiKey;

    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Generate a 384-dimensional embedding vector from text.
     * Returns null on failure (caller should handle gracefully).
     *
     * @param text  The text to embed (description + tags + color + category)
     * @return      List of 384 doubles, or null if API call fails
     */
    public List<Double> generateEmbedding(String text) {
        try {
            if (text == null || text.isBlank()) return null;

            // Truncate to 512 tokens (model limit) — ~2000 chars is safe
            String input = text.length() > 2000 ? text.substring(0, 2000) : text;

            Map<String, Object> requestBody = Map.of("inputs", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (hfApiKey != null && !hfApiKey.isBlank()) {
                headers.setBearerAuth(hfApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling HuggingFace Embedding API...");
            ResponseEntity<String> response = restTemplate.exchange(
                HF_API_URL, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody().trim();

                // Response is a 2D array [[v1, v2, ...]] — take the first (and only) row
                if (body.startsWith("[[")) {
                    List<List<Double>> nested = objectMapper.readValue(body, new TypeReference<>() {});
                    if (!nested.isEmpty()) {
                        log.info("Embedding generated: {} dimensions", nested.get(0).size());
                        return nested.get(0);
                    }
                }
                // Some model versions return a 1D array [v1, v2, ...]
                else if (body.startsWith("[")) {
                    List<Double> flat = objectMapper.readValue(body, new TypeReference<>() {});
                    log.info("Embedding generated: {} dimensions", flat.size());
                    return flat;
                }
            }

            log.warn("Unexpected HuggingFace response format");
            return null;

        } catch (Exception e) {
            log.error("HuggingFace Embedding API error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Serialize a vector to JSON string for DB storage.
     */
    public String vectorToJson(List<Double> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deserialize a JSON string back to a vector.
     */
    public List<Double> jsonToVector(String json) {
        try {
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Could not parse embedding vector: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cosine similarity between two vectors. Returns 0.0 if either is null/mismatched.
     *
     * Formula: (A · B) / (||A|| × ||B||)
     * Result range: -1.0 (opposite) to 1.0 (identical)
     */
    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size()) return 0.0;

        double dotProduct  = 0.0;
        double normA       = 0.0;
        double normB       = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA      += a.get(i) * a.get(i);
            normB      += b.get(i) * b.get(i);
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * Build a rich semantic text from Groq analysis result.
     * This is the text that gets embedded — more context = better similarity.
     */
    public static String buildEmbeddingText(AIService.AnalysisResult analysis,
                                             String userDescription, String title) {
        StringBuilder sb = new StringBuilder();
        if (title != null)                  sb.append(title).append(". ");
        if (userDescription != null)        sb.append(userDescription).append(". ");
        if (analysis.getDescription() != null) sb.append(analysis.getDescription()).append(". ");
        if (analysis.getColor() != null)    sb.append("Color: ").append(analysis.getColor()).append(". ");
        if (analysis.getBrand() != null)    sb.append("Brand: ").append(analysis.getBrand()).append(". ");
        if (analysis.getCategory() != null) sb.append("Category: ").append(analysis.getCategory()).append(". ");
        if (!analysis.getTags().isEmpty())  sb.append("Tags: ").append(String.join(", ", analysis.getTags()));
        return sb.toString().trim();
    }
}
