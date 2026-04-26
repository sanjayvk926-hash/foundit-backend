package com.foundit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqApiKey;

    // Groq API endpoint (OpenAI-compatible)
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    // LLaMA 4 Scout — free, fast, supports image vision
    private static final String MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Analyze an uploaded image using Groq Vision (LLaMA 4 Scout).
     * Returns structured metadata: category, color, brand, description, tags.
     * Falls back gracefully if API call fails.
     */
    public AnalysisResult analyzeImage(MultipartFile file) {
        try {
            return callGroqVision(file);
        } catch (Exception e) {
            log.error("Groq Vision API error (falling back to empty tags): {}", e.getMessage());
            // Graceful fallback — item is saved without AI tags, matching is skipped
            return new AnalysisResult("Other", "Unknown", null, "", new ArrayList<>());
        }
    }

    private AnalysisResult callGroqVision(MultipartFile file) throws Exception {
        // Convert image to Base64 data URL
        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        // Build the vision message (OpenAI-compatible format)
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", Map.of("url", dataUrl));

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text",
            "You are an AI assistant for a campus lost & found system. " +
            "Analyze this image and return ONLY a valid JSON object (no markdown, no explanation) with these exact fields:\n" +
            "{\n" +
            "  \"category\": \"<one of: Electronics, Bag, Keys, Clothing, Stationery, IDCARD, Other>\",\n" +
            "  \"color\": \"<primary color of the item>\",\n" +
            "  \"brand\": \"<brand name if clearly visible, otherwise null>\",\n" +
            "  \"description\": \"<one concise sentence describing the item>\",\n" +
            "  \"tags\": [\"<8 to 12 descriptive keywords about the item's appearance, type, and features>\"]\n" +
            "}"
        );

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textContent, imageContent));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", 512);
        requestBody.put("temperature", 0.1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Calling Groq Vision API (model: {})...", MODEL);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(GROQ_API_URL, entity, Map.class);

        if (response != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) msg.get("content");

                // Strip markdown fences if the model wraps response in them
                content = content.trim();
                if (content.startsWith("```")) {
                    content = content.replaceAll("(?s)```[a-z]*\\n?", "").replaceAll("```", "").trim();
                }

                log.info("Groq raw response: {}", content);

                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(content, Map.class);

                String category    = (String) parsed.getOrDefault("category", "Other");
                String color       = (String) parsed.getOrDefault("color", "Unknown");
                String brand       = (String) parsed.get("brand");
                String description = (String) parsed.getOrDefault("description", "");
                @SuppressWarnings("unchecked")
                List<String> tags  = (List<String>) parsed.getOrDefault("tags", new ArrayList<>());

                log.info("Groq Analysis: category={}, color={}, tags={}", category, color, tags);
                return new AnalysisResult(category, color, brand, description, tags);
            }
        }

        throw new RuntimeException("Empty or invalid response from Groq Vision API");
    }

    // ── Inner result class ─────────────────────────────────────────────────────
    public static class AnalysisResult {
        private final String category;
        private final String color;
        private final String brand;
        private final String description;
        private final List<String> tags;

        public AnalysisResult(String category, String color, String brand,
                              String description, List<String> tags) {
            this.category    = category;
            this.color       = color;
            this.brand       = brand;
            this.description = description;
            this.tags        = tags != null ? tags : new ArrayList<>();
        }

        public String getCategory()       { return category; }
        public String getColor()          { return color; }
        public String getBrand()          { return brand; }
        public String getDescription()    { return description; }
        public List<String> getTags()     { return tags; }

        /** Serialize to JSON string for storage in the `ai_tags` DB column. */
        public String toJson() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("category",    category);
                map.put("color",       color);
                map.put("brand",       brand);
                map.put("description", description);
                map.put("tags",        tags);
                return mapper.writeValueAsString(map);
            } catch (Exception e) {
                return "{}";
            }
        }
    }
}
