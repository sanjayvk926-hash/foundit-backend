package com.foundit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foundit.model.FoundItem;
import com.foundit.model.LostItem;
import com.foundit.model.Match;
import com.foundit.repository.FoundItemRepository;
import com.foundit.repository.LostItemRepository;
import com.foundit.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid matching engine combining:
 *   - Vector cosine similarity (50%) — semantic understanding via MiniLM embeddings
 *   - Tag Jaccard similarity (30%)   — keyword overlap from Groq analysis
 *   - Color match bonus (10%)        — exact color agreement
 *   - Category match bonus (10%)     — same item type
 *
 * Match threshold: 25% (0.25) combined score
 */
@Service
public class MatchingEngineService {

    private static final Logger log = LoggerFactory.getLogger(MatchingEngineService.class);

    @Autowired private LostItemRepository  lostItemRepository;
    @Autowired private FoundItemRepository foundItemRepository;
    @Autowired private MatchRepository     matchRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmbeddingService    embeddingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double MATCH_THRESHOLD = 0.25;

    // ─── Called when a Found Item is reported ────────────────────────────────
    public void runMatchingEngine(FoundItem foundItem) {
        log.info("Running match engine for found item id={}", foundItem.getId());

        List<Double> foundVector   = embeddingService.jsonToVector(foundItem.getEmbedding());
        List<String> foundTags     = extractTags(foundItem.getAiTags());
        String       foundColor    = extractField(foundItem.getAiTags(), "color");
        String       foundCategory = foundItem.getCategory() != null ? foundItem.getCategory().name() : "";

        if (foundVector == null && foundTags.isEmpty()) {
            log.warn("No embedding or tags on found item {}. Skipping match.", foundItem.getId());
            return;
        }

        for (LostItem lostItem : lostItemRepository.findAll()) {
            if (lostItem.getUser().getId().equals(foundItem.getFinderUser().getId())) continue;
            if (matchAlreadyExists(lostItem.getId(), foundItem.getId())) continue;

            List<Double> lostVector = embeddingService.jsonToVector(lostItem.getEmbedding());
            List<String> lostTags   = extractTags(lostItem.getAiTags());
            if (lostVector == null && lostTags.isEmpty()) continue;

            double score = calculateScore(
                foundVector, foundTags, foundColor, foundCategory,
                lostVector,  lostTags,
                extractField(lostItem.getAiTags(), "color"),
                lostItem.getCategory() != null ? lostItem.getCategory().name() : ""
            );

            log.info("Score found={} ↔ lost={}: {}", foundItem.getId(), lostItem.getId(), score);
            if (score >= MATCH_THRESHOLD) {
                log.info("✅ Match! Score={}", score);
                createMatch(lostItem, foundItem, score);
            }
        }
    }

    // ─── Called when a Lost Item is reported ─────────────────────────────────
    public void runMatchingEngine(LostItem lostItem) {
        log.info("Running match engine for lost item id={}", lostItem.getId());

        List<Double> lostVector   = embeddingService.jsonToVector(lostItem.getEmbedding());
        List<String> lostTags     = extractTags(lostItem.getAiTags());
        String       lostColor    = extractField(lostItem.getAiTags(), "color");
        String       lostCategory = lostItem.getCategory() != null ? lostItem.getCategory().name() : "";

        if (lostVector == null && lostTags.isEmpty()) {
            log.warn("No embedding or tags on lost item {}. Skipping match.", lostItem.getId());
            return;
        }

        for (FoundItem foundItem : foundItemRepository.findAll()) {
            if (foundItem.getFinderUser().getId().equals(lostItem.getUser().getId())) continue;
            if (matchAlreadyExists(lostItem.getId(), foundItem.getId())) continue;

            List<Double> foundVector = embeddingService.jsonToVector(foundItem.getEmbedding());
            List<String> foundTags   = extractTags(foundItem.getAiTags());
            if (foundVector == null && foundTags.isEmpty()) continue;

            double score = calculateScore(
                lostVector, lostTags, lostColor, lostCategory,
                foundVector, foundTags,
                extractField(foundItem.getAiTags(), "color"),
                foundItem.getCategory() != null ? foundItem.getCategory().name() : ""
            );

            log.info("Score lost={} ↔ found={}: {}", lostItem.getId(), foundItem.getId(), score);
            if (score >= MATCH_THRESHOLD) {
                log.info("✅ Match! Score={}", score);
                createMatch(lostItem, foundItem, score);
            }
        }
    }

    // ─── Hybrid scoring ───────────────────────────────────────────────────────
    private double calculateScore(List<Double> vecA, List<String> tagsA, String colorA, String catA,
                                  List<Double> vecB, List<String> tagsB, String colorB, String catB) {

        // 1. Vector cosine similarity (semantic understanding)
        double vectorScore = 0.0;
        if (vecA != null && vecB != null) {
            vectorScore = Math.max(0.0, embeddingService.cosineSimilarity(vecA, vecB));
        }

        // 2. Tag Jaccard similarity (keyword overlap)
        double tagScore = jaccardSimilarity(tagsA, tagsB);

        // 3. Color match (partial string match)
        double colorScore = 0.0;
        if (colorA != null && colorB != null && !colorA.isBlank() && !colorB.isBlank()) {
            String cA = colorA.toLowerCase();
            String cB = colorB.toLowerCase();
            if (cA.contains(cB.split(" ")[0]) || cB.contains(cA.split(" ")[0])) {
                colorScore = 1.0;
            }
        }

        // 4. Category match
        double catScore = (!catA.isEmpty() && catA.equalsIgnoreCase(catB)) ? 1.0 : 0.0;

        // Weights: vector 50%, tags 30%, color 10%, category 10%
        double combined = (vectorScore * 0.50)
                        + (tagScore    * 0.30)
                        + (colorScore  * 0.10)
                        + (catScore    * 0.10);

        log.debug("  vectorScore={:.3f}  tagScore={:.3f}  colorScore={}  catScore={}  → combined={:.3f}",
                  vectorScore, tagScore, colorScore, catScore, combined);

        return combined;
    }

    // ─── Jaccard: |A ∩ B| / |A ∪ B| ─────────────────────────────────────────
    private double jaccardSimilarity(List<String> a, List<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> setA = a.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> setB = b.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> intersection = new HashSet<>(setA); intersection.retainAll(setB);
        Set<String> union        = new HashSet<>(setA); union.addAll(setB);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // ─── JSON helpers ─────────────────────────────────────────────────────────
    private List<String> extractTags(String aiTagsJson) {
        try {
            if (aiTagsJson == null || aiTagsJson.isBlank()) return Collections.emptyList();
            Map<String, Object> parsed = objectMapper.readValue(aiTagsJson, new TypeReference<>() {});
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) parsed.get("tags");
            return tags != null ? tags : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String extractField(String aiTagsJson, String field) {
        try {
            if (aiTagsJson == null || aiTagsJson.isBlank()) return null;
            Map<String, Object> parsed = objectMapper.readValue(aiTagsJson, new TypeReference<>() {});
            Object val = parsed.get(field);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean matchAlreadyExists(Long lostId, Long foundId) {
        return matchRepository.findAll().stream().anyMatch(m ->
            m.getLostItem().getId().equals(lostId) &&
            m.getFoundItem().getId().equals(foundId)
        );
    }

    private void createMatch(LostItem lostItem, FoundItem foundItem, double score) {
        Match match = new Match();
        match.setLostItem(lostItem);
        match.setFoundItem(foundItem);
        match.setMatchScore(BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP));
        match.setStatus(Match.MatchStatus.PENDING);
        Match saved = matchRepository.save(match);
        notificationService.createMatchNotification(lostItem.getUser(), saved);
        notificationService.createMatchNotification(foundItem.getFinderUser(), saved);
        log.info("Match #{} saved. Users notified.", saved.getId());
    }
}
