package com.petlifecycle.petlifecycle_backend.service;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.model.ApplicationEventType;
import com.petlifecycle.petlifecycle_backend.model.ApplicationStatus;
import com.petlifecycle.petlifecycle_backend.repository.ApplicationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminApplicationService {

    private static final List<ApplicationStatus> PENDING_STATES = List.of(
            ApplicationStatus.SUBMITTED,
            ApplicationStatus.UNDER_REVIEW,
            ApplicationStatus.WAITING_INFO);

    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService workflowService;

    public AdminApplicationService(
            ApplicationRepository applicationRepository,
            ApplicationWorkflowService workflowService) {
        this.applicationRepository = applicationRepository;
        this.workflowService = workflowService;
    }

    public List<Application> listPendingApplications() {
        return applicationRepository.findByBasvuruDurumuIn(PENDING_STATES);
    }

    @Transactional
    public ApplicationWorkflowService.TransitionResult approve(ApprovalCommand command) {
        Application application = loadApplication(command.applicationId());
        var transitionCommand = new ApplicationWorkflowService.ApplicationEventCommand(
                ApplicationEventType.APPROVE,
                command.adminId(),
                "ADMIN",
                Map.of(
                        "checklistComplete", command.checklistComplete(),
                        "petAvailable", command.petAvailable()),
                command.idempotencyKey());

        var result = workflowService.handleEvent(application, transitionCommand);
        applicationRepository.save(application);
        return result;
    }

    @Transactional
    public ApplicationWorkflowService.TransitionResult reject(RejectCommand command) {
        Application application = loadApplication(command.applicationId());
        var transitionCommand = new ApplicationWorkflowService.ApplicationEventCommand(
                ApplicationEventType.REJECT,
                command.adminId(),
                "ADMIN",
                Map.of("reason", command.reason()),
                command.idempotencyKey());

        var result = workflowService.handleEvent(application, transitionCommand);
        applicationRepository.save(application);
        return result;
    }

    private Application loadApplication(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Başvuru bulunamadı: " + applicationId));
    }

    public record ApprovalCommand(
            Long applicationId,
            Long adminId,
            boolean checklistComplete,
            boolean petAvailable,
            String idempotencyKey) {
    }

    public record RejectCommand(
            Long applicationId,
            Long adminId,
            String reason,
            String idempotencyKey) {
    }
}
