package com.foundit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Electronics','Bag','ID Card','Keys','Clothing','Stationery','Other')")
    private Category category;

    @Column(length = 50)
    private String color;

    private LocalDate dateLost;

    @Column(length = 200)
    private String locationLost;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('OPEN','MATCHED','RESOLVED','CLOSED') DEFAULT 'OPEN'")
    private ItemStatus status = ItemStatus.OPEN;

    @Column(columnDefinition = "JSON")
    private String aiTags;

    /** 384-dim vector from all-MiniLM-L6-v2, stored as JSON array for cosine similarity */
    @Column(columnDefinition = "LONGTEXT")
    private String embedding;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Category {
        Electronics, Bag, IDCARD, Keys, Clothing, Stationery, Other
    }

    public enum ItemStatus {
        OPEN, MATCHED, RESOLVED, CLOSED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return this.category; }
    public void setCategory(Category category) { this.category = category; }

    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }

    public LocalDate getDateLost() { return this.dateLost; }
    public void setDateLost(LocalDate dateLost) { this.dateLost = dateLost; }

    public String getLocationLost() { return this.locationLost; }
    public void setLocationLost(String locationLost) { this.locationLost = locationLost; }

    public String getImageUrl() { return this.imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public ItemStatus getStatus() { return this.status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getAiTags() { return this.aiTags; }
    public void setAiTags(String aiTags) { this.aiTags = aiTags; }

    public String getEmbedding() { return this.embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
