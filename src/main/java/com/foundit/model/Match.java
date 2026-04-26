package com.foundit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    @Column(precision = 4, scale = 3)
    private BigDecimal matchScore;

    @Column(precision = 4, scale = 3)
    private BigDecimal objectTypeScore;

    @Column(precision = 4, scale = 3)
    private BigDecimal colorScore;

    @Column(precision = 4, scale = 3)
    private BigDecimal descriptionScore;

    @Column(precision = 4, scale = 3)
    private BigDecimal imageScore;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    private MatchStatus status = MatchStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    public enum MatchStatus {
        PENDING, APPROVED, REJECTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public LostItem getLostItem() { return this.lostItem; }
    public void setLostItem(LostItem lostItem) { this.lostItem = lostItem; }

    public FoundItem getFoundItem() { return this.foundItem; }
    public void setFoundItem(FoundItem foundItem) { this.foundItem = foundItem; }

    public BigDecimal getMatchScore() { return this.matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }

    public BigDecimal getObjectTypeScore() { return this.objectTypeScore; }
    public void setObjectTypeScore(BigDecimal objectTypeScore) { this.objectTypeScore = objectTypeScore; }

    public BigDecimal getColorScore() { return this.colorScore; }
    public void setColorScore(BigDecimal colorScore) { this.colorScore = colorScore; }

    public BigDecimal getDescriptionScore() { return this.descriptionScore; }
    public void setDescriptionScore(BigDecimal descriptionScore) { this.descriptionScore = descriptionScore; }

    public BigDecimal getImageScore() { return this.imageScore; }
    public void setImageScore(BigDecimal imageScore) { this.imageScore = imageScore; }

    public MatchStatus getStatus() { return this.status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public String getAdminNote() { return this.adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return this.resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
