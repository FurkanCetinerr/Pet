package com.petlifecycle.petlifecycle_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ApplicationTransitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus fromState;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus toState;

    @Enumerated(EnumType.STRING)
    private ApplicationEventType eventType;

    private Long actorId;
    private String actorRole;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String reasonCode;
    private String notes;
    private String idempotencyKey;

    public ApplicationTransitionLog() {
    }

    public ApplicationTransitionLog(
            Application application,
            ApplicationStatus fromState,
            ApplicationStatus toState,
            ApplicationEventType eventType,
            Long actorId,
            String actorRole,
            String reasonCode,
            String notes,
            String idempotencyKey) {
        this.application = application;
        this.fromState = fromState;
        this.toState = toState;
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.reasonCode = reasonCode;
        this.notes = notes;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters/setters

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationStatus getFromState() {
        return fromState;
    }

    public void setFromState(ApplicationStatus fromState) {
        this.fromState = fromState;
    }

    public ApplicationStatus getToState() {
        return toState;
    }

    public void setToState(ApplicationStatus toState) {
        this.toState = toState;
    }

    public ApplicationEventType getEventType() {
        return eventType;
    }

    public void setEventType(ApplicationEventType eventType) {
        this.eventType = eventType;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
