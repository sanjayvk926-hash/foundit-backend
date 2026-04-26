package com.foundit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "found_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FoundItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finder_user_id", nullable = false)
    private User finderUser;

    @Column(length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Electronics','Bag','IDCARD','Keys','Clothing','Stationery','Other')")
    private LostItem.Category category;

    @Column(length = 50)
    private String color;

    @Column(nullable = false)
    private LocalDate dateFound;

    @Column(nullable = false, length = 200)
    private String locationFound;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer securityDeskId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('UNMATCHED','MATCHED','COLLECTED','DISPOSED') DEFAULT 'UNMATCHED'")
    private FoundStatus status = FoundStatus.UNMATCHED;

    @Column(columnDefinition = "JSON")
    private String aiTags;

    /** 384-dim vector from all-MiniLM-L6-v2, stored as JSON array for cosine similarity */
    @Column(columnDefinition = "LONGTEXT")
    private String embedding;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum FoundStatus {
        UNMATCHED, MATCHED, COLLECTED, DISPOSED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public User getFinderUser() { return this.finderUser; }
    public void setFinderUser(User finderUser) { this.finderUser = finderUser; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public LostItem.Category getCategory() { return this.category; }
    public void setCategory(LostItem.Category category) { this.category = category; }

    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }

    public LocalDate getDateFound() { return this.dateFound; }
    public void setDateFound(LocalDate dateFound) { this.dateFound = dateFound; }

    public String getLocationFound() { return this.locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }

    public String getImageUrl() { return this.imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getSecurityDeskId() { return this.securityDeskId; }
    public void setSecurityDeskId(Integer securityDeskId) { this.securityDeskId = securityDeskId; }

    public FoundStatus getStatus() { return this.status; }
    public void setStatus(FoundStatus status) { this.status = status; }

    public String getAiTags() { return this.aiTags; }
    public void setAiTags(String aiTags) { this.aiTags = aiTags; }

    public String getEmbedding() { return this.embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
