package com.foundit.controller;

import com.foundit.dto.request.LostItemRequest;
import com.foundit.dto.response.BaseResponse;
import com.foundit.model.LostItem;
import com.foundit.model.User;
import com.foundit.repository.LostItemRepository;
import com.foundit.repository.UserRepository;
import com.foundit.service.AIService;
import com.foundit.service.EmbeddingService;
import com.foundit.service.FileStorageService;
import com.foundit.service.MatchingEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/lost-items")
public class LostItemController {

    private static final Logger log = LoggerFactory.getLogger(LostItemController.class);

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AIService aiService;

    @Autowired
    private MatchingEngineService matchingEngineService;

    @Autowired
    private EmbeddingService embeddingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> reportLostItem(
            @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestPart("data") String dataJson) throws com.fasterxml.jackson.core.JsonProcessingException {
        
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        LostItemRequest request = mapper.readValue(dataJson, LostItemRequest.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        LostItem item = new LostItem();
        item.setUser(user);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(LostItem.Category.valueOf(request.getCategory()));
        item.setColor(request.getColor());
        item.setDateLost(request.getDateLost());
        item.setLocationLost(request.getLocationLost());

        // File and AI analysis are optional
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            item.setImageUrl(fileName);

            AIService.AnalysisResult analysis = aiService.analyzeImage(file);
            item.setAiTags(analysis.toJson());
            // Override category with AI's detection if it's valid
            if (isValidCategory(analysis.getCategory())) {
                item.setCategory(LostItem.Category.valueOf(analysis.getCategory()));
            }

            // Generate 384-dim embedding for cosine similarity matching
            String embeddingText = EmbeddingService.buildEmbeddingText(
                analysis, request.getDescription(), request.getTitle()
            );
            List<Double> vector = embeddingService.generateEmbedding(embeddingText);
            if (vector != null) {
                item.setEmbedding(embeddingService.vectorToJson(vector));
                log.info("Embedding stored for lost item: {} dims", vector.size());
            }
        }

        lostItemRepository.save(item);
        
        // Trigger Match Engine (will skip AI matching if no vector is stored)
        matchingEngineService.runMatchingEngine(item);

        return ResponseEntity.ok(BaseResponse.success("Lost item reported and matching engine triggered", item));
    }

    @GetMapping
    public ResponseEntity<?> getAllLostItems() {
        return ResponseEntity.ok(BaseResponse.success("Fetched all lost items", lostItemRepository.findAll()));
    }

    private boolean isValidCategory(String cat) {
        if (cat == null) return false;
        for (LostItem.Category c : LostItem.Category.values()) {
            if (c.name().equalsIgnoreCase(cat)) return true;
        }
        return false;
    }
}
