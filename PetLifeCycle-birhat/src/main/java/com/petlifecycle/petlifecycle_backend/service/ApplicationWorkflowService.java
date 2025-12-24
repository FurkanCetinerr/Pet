package com.petlifecycle.petlifecycle_backend.service;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.model.ApplicationEventType;
import com.petlifecycle.petlifecycle_backend.model.ApplicationStatus;
import com.petlifecycle.petlifecycle_backend.model.ApplicationTransitionLog;
import com.petlifecycle.petlifecycle_backend.repository.ApplicationTransitionLogRepository;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Başvuru durum makinesini yöneten servis. Event tabanlı geçiş ve guard
 * kontrollerini burada tanımlarız.
 */
@Service
public class ApplicationWorkflowService {

    private static final Set<ApplicationStatus> TERMINAL_STATES = EnumSet.of(
            ApplicationStatus.APPROVED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.CANCELLED,
            ApplicationStatus.EXPIRED);

    private final Map<ApplicationStatus, Map<ApplicationEventType, ApplicationStatus>> transitions =
            new EnumMap<>(ApplicationStatus.class);

    private final ApplicationTransitionLogRepository logRepository;

    public ApplicationWorkflowService(ApplicationTransitionLogRepository logRepository) {
        this.logRepository = logRepository;
        registerTransitions();
    }

    public TransitionResult handleEvent(Application application, ApplicationEventCommand command) {
        if (command.idempotencyKey() != null
                && command.idempotencyKey().equals(application.getLastIdempotencyKey())) {
            return new TransitionResult(application.getId(), application.getLastIdempotentState(), true, null);
        }

        ApplicationStatus current = application.getBasvuruDurumu() == null
                ? ApplicationStatus.DRAFT
                : application.getBasvuruDurumu();
        if (TERMINAL_STATES.contains(current)) {
            return TransitionResult.denied(application.getId(), current, "TERMINAL_STATE");
        }

        ApplicationStatus next = resolveNextState(current, command.eventType());
        if (next == null) {
            return TransitionResult.denied(application.getId(), current, "INVALID_TRANSITION");
        }

        GuardResult guard = evaluateGuards(application, command);
        if (!guard.allowed()) {
            return TransitionResult.denied(application.getId(), current, guard.reasonCode());
        }

        ApplicationStatus from = current;
        applySideEffects(application, command, next, guard);
        application.setBasvuruDurumu(next);
        application.setLastIdempotencyKey(command.idempotencyKey());
        application.setLastIdempotentState(next);

        logRepository.save(new ApplicationTransitionLog(
                application,
                from,
                next,
                command.eventType(),
                command.actorId(),
                command.actorRole(),
                guard.reasonCode(),
                guard.notes(),
                command.idempotencyKey()));

        return new TransitionResult(application.getId(), next, false, guard.reasonCode());
    }

    private ApplicationStatus resolveNextState(ApplicationStatus current, ApplicationEventType eventType) {
        Map<ApplicationEventType, ApplicationStatus> allowed = transitions.get(current);
        if (allowed == null) {
            return null;
        }
        return allowed.get(eventType);
    }

    private GuardResult evaluateGuards(Application application, ApplicationEventCommand command) {
        Map<String, Object> payload = command.payload() == null ? Map.of() : command.payload();
        return switch (command.eventType()) {
            case SUBMIT_APPLICATION -> {
                boolean valid = application.getUser() != null
                        && application.getPet() != null
                        && Boolean.TRUE.equals(payload.get("requiredFieldsComplete"));
                yield valid ? GuardResult.allow() : GuardResult.deny("MISSING_FIELDS", "Zorunlu alanlar eksik");
            }
            case START_REVIEW -> {
                boolean reviewer = "REVIEWER".equalsIgnoreCase(command.actorRole())
                        || "ADMIN".equalsIgnoreCase(command.actorRole());
                yield reviewer ? GuardResult.allow() : GuardResult.deny("ROLE_REQUIRED", "Reviewer rolü gerekli");
            }
            case REQUEST_INFO -> {
                String info = (String) payload.get("requestedInfo");
                yield (info != null && !info.isBlank())
                        ? GuardResult.withNotes("REQUEST_INFO_SENT", info)
                        : GuardResult.deny("EMPTY_REQUEST", "İstenen bilgi açıklaması boş");
            }
            case PROVIDE_INFO -> {
                boolean uploaded = Boolean.TRUE.equals(payload.get("attachmentsUploaded"));
                yield uploaded ? GuardResult.allow() : GuardResult.deny("MISSING_ATTACHMENTS", "Belgeler yüklenmedi");
            }
            case APPROVE -> {
                boolean checklist = Boolean.TRUE.equals(payload.get("checklistComplete"));
                boolean petAvailable = Boolean.TRUE.equals(payload.get("petAvailable"));
                yield (checklist && petAvailable)
                        ? GuardResult.withReason("APPROVED", "Checklist tamamlandı")
                        : GuardResult.deny("CHECKS_FAILED", "Checklist veya pet uygun değil");
            }
            case REJECT -> {
                String reason = (String) payload.get("reason");
                yield (reason != null && !reason.isBlank())
                        ? GuardResult.withReason("REJECTED", reason)
                        : GuardResult.deny("MISSING_REASON", "Reddetme gerekçesi zorunlu");
            }
            case CANCEL -> GuardResult.withReason("CANCELLED", (String) payload.get("reason"));
            case EXPIRE -> {
                boolean ttl = Boolean.TRUE.equals(payload.get("ttlExpired"));
                yield ttl ? GuardResult.allow() : GuardResult.deny("TTL_NOT_REACHED", "Süre dolmadı");
            }
            case SAVE_DRAFT -> GuardResult.allow();
        };
    }

    private void applySideEffects(
            Application application,
            ApplicationEventCommand command,
            ApplicationStatus next,
            GuardResult guardResult) {

        switch (command.eventType()) {
            case SUBMIT_APPLICATION -> application.setSubmittedAt(LocalDateTime.now());
            case START_REVIEW -> application.setReviewerId(command.actorId());
            case REQUEST_INFO -> {
                application.setRequestedInfoDetails(guardResult.notes());
                application.setWaitingInfoSince(LocalDateTime.now());
            }
            case PROVIDE_INFO -> application.setWaitingInfoSince(null);
            case APPROVE -> application.setRejectionReason(null);
            case REJECT -> application.setRejectionReason(guardResult.notes());
            case CANCEL -> application.setCancelReason(guardResult.notes());
            case EXPIRE -> application.setWaitingInfoSince(null);
            case SAVE_DRAFT -> {
            }
        }
    }

    private void registerTransitions() {
        register(ApplicationStatus.DRAFT, Map.of(
                ApplicationEventType.SUBMIT_APPLICATION, ApplicationStatus.SUBMITTED,
                ApplicationEventType.CANCEL, ApplicationStatus.CANCELLED));

        register(ApplicationStatus.SUBMITTED, Map.of(
                ApplicationEventType.START_REVIEW, ApplicationStatus.UNDER_REVIEW,
                ApplicationEventType.CANCEL, ApplicationStatus.CANCELLED,
                ApplicationEventType.EXPIRE, ApplicationStatus.EXPIRED));

        register(ApplicationStatus.UNDER_REVIEW, Map.of(
                ApplicationEventType.REQUEST_INFO, ApplicationStatus.WAITING_INFO,
                ApplicationEventType.APPROVE, ApplicationStatus.APPROVED,
                ApplicationEventType.REJECT, ApplicationStatus.REJECTED,
                ApplicationEventType.CANCEL, ApplicationStatus.CANCELLED));

        register(ApplicationStatus.WAITING_INFO, Map.of(
                ApplicationEventType.PROVIDE_INFO, ApplicationStatus.UNDER_REVIEW,
                ApplicationEventType.CANCEL, ApplicationStatus.CANCELLED,
                ApplicationEventType.EXPIRE, ApplicationStatus.EXPIRED));
    }

    private void register(ApplicationStatus state, Map<ApplicationEventType, ApplicationStatus> map) {
        EnumMap<ApplicationEventType, ApplicationStatus> enumMap = new EnumMap<>(ApplicationEventType.class);
        enumMap.putAll(map);
        transitions.put(state, enumMap);
    }

    public record ApplicationEventCommand(
            ApplicationEventType eventType,
            Long actorId,
            String actorRole,
            Map<String, Object> payload,
            String idempotencyKey) {
    }

    public record TransitionResult(
            Long applicationId,
            ApplicationStatus state,
            boolean fromCache,
            String reasonCode) {

        public static TransitionResult denied(Long applicationId, ApplicationStatus state, String reason) {
            return new TransitionResult(applicationId, state, false, reason);
        }
    }

    private record GuardResult(boolean allowed, String reasonCode, String notes) {

        static GuardResult allow() {
            return new GuardResult(true, null, null);
        }

        static GuardResult withReason(String reasonCode, String notes) {
            return new GuardResult(true, reasonCode, notes);
        }

        static GuardResult withNotes(String reasonCode, String notes) {
            return new GuardResult(true, reasonCode, notes);
        }

        static GuardResult deny(String reasonCode, String notes) {
            return new GuardResult(false, reasonCode, notes);
        }
    }
}
