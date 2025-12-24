/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petlifecycle.petlifecycle_backend.model;

/**
 *
 * @author Taha
 */
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi User (ManyToOne)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) 
    private User user;

    // Hangi Pet (ManyToOne)
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    private LocalDateTime basvuruTarihi = LocalDateTime.now();
    private LocalDateTime submittedAt;
    private LocalDateTime waitingInfoSince;

    private String requestedInfoDetails;
    private String rejectionReason;
    private String cancelReason;

    private Long reviewerId;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus basvuruDurumu = ApplicationStatus.DRAFT;
    private String lastIdempotencyKey;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus lastIdempotentState;

    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    // Getterlar ve Setterlar buraya eklenecek

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public LocalDateTime getBasvuruTarihi() {
        return basvuruTarihi;
    }

    public void setBasvuruTarihi(LocalDateTime basvuruTarihi) {
        this.basvuruTarihi = basvuruTarihi;
    }

    public ApplicationStatus getBasvuruDurumu() {
        return basvuruDurumu;
    }

    public void setBasvuruDurumu(ApplicationStatus basvuruDurumu) {
        this.basvuruDurumu = basvuruDurumu;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getWaitingInfoSince() {
        return waitingInfoSince;
    }

    public void setWaitingInfoSince(LocalDateTime waitingInfoSince) {
        this.waitingInfoSince = waitingInfoSince;
    }

    public String getRequestedInfoDetails() {
        return requestedInfoDetails;
    }

    public void setRequestedInfoDetails(String requestedInfoDetails) {
        this.requestedInfoDetails = requestedInfoDetails;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getLastIdempotencyKey() {
        return lastIdempotencyKey;
    }

    public void setLastIdempotencyKey(String lastIdempotencyKey) {
        this.lastIdempotencyKey = lastIdempotencyKey;
    }

    public ApplicationStatus getLastIdempotentState() {
        return lastIdempotentState;
    }

    public void setLastIdempotentState(ApplicationStatus lastIdempotentState) {
        this.lastIdempotentState = lastIdempotentState;
    }

    @PreUpdate
    @PrePersist
    public void touch() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
