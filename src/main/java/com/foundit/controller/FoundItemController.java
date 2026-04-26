package com.foundit.controller;

import com.foundit.dto.request.FoundItemRequest;
import com.foundit.dto.response.BaseResponse;
import com.foundit.model.FoundItem;
import com.foundit.model.LostItem;
import com.foundit.model.User;
import com.foundit.repository.FoundItemRepository;
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
@RequestMapping("/api/v1/found-items")
public class FoundItemController {

    private static final Logger log = LoggerFactory.getLogger(FoundItemController.class);

    @Autowired
    private FoundItemRepository foundItemRepository;

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
    public ResponseEntity<?> reportFoundItem(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @RequestPart("data") String dataJson) throws com.fasterxml.jackson.core.JsonProcessingException {
        
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        FoundItemRequest request = mapper.readValue(dataJson, FoundItemRequest.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        // 1. Store File
        String fileName = fileStorageService.storeFile(file);

        // 2. AI Analysis
        AIService.AnalysisResult analysis = aiService.analyzeImage(file);

        // 3. Create Item
        FoundItem item = new FoundItem();
        item.setFinderUser(user);
        item.setLocationFound(request.getLocationFound());
        item.setDateFound(request.getDateFound());
        item.setDescription(request.getDescription());
        item.setImageUrl(fileName);
        item.setCategory(LostItem.Category.valueOf(
            isValidCategory(analysis.getCategory()) ? analysis.getCategory() : "Other"
        ));
        item.setAiTags(analysis.toJson());

        // 4. Generate semantic embedding vector (384-dim) for cosine similarity matching
        String embeddingText = EmbeddingService.buildEmbeddingText(
            analysis,
            request.getDescription(),
            null  // FoundItem has no title field
        );
        List<Double> vector = embeddingService.generateEmbedding(embeddingText);
        if (vector != null) {
            item.setEmbedding(embeddingService.vectorToJson(vector));
            log.info("Embedding stored for found item: {} dims", vector.size());
        }

        foundItemRepository.save(item);
        
        // 4. Trigger Match Engine
        matchingEngineService.runMatchingEngine(item);

        return ResponseEntity.ok(BaseResponse.success("Found item reported and indexed successfully", item));
    }

    @GetMapping
    public ResponseEntity<?> getAllFoundItems() {
        return ResponseEntity.ok(BaseResponse.success("Fetched all found items", foundItemRepository.findAll()));
    }

    private boolean isValidCategory(String cat) {
        if (cat == null) return false;
        for (LostItem.Category c : LostItem.Category.values()) {
            if (c.name().equalsIgnoreCase(cat)) return true;
        }
        return false;
    }
}
