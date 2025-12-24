package com.petlifecycle.petlifecycle_backend;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.model.ApplicationEventType;
import com.petlifecycle.petlifecycle_backend.model.ApplicationStatus;
import com.petlifecycle.petlifecycle_backend.model.Pet;
import com.petlifecycle.petlifecycle_backend.model.User;
import com.petlifecycle.petlifecycle_backend.repository.ApplicationTransitionLogRepository;
import com.petlifecycle.petlifecycle_backend.service.ApplicationWorkflowService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApplicationWorkflowServiceTest {

    @Mock
    private ApplicationTransitionLogRepository logRepository;

    private ApplicationWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new ApplicationWorkflowService(logRepository);
    }

    @Test
    void shouldSubmitReviewAndApprove() {
        Application application = new Application();
        application.setUser(new User());
        application.setPet(new Pet());

        var submitResult = workflowService.handleEvent(
                application,
                new ApplicationWorkflowService.ApplicationEventCommand(
                        ApplicationEventType.SUBMIT_APPLICATION,
                        1L,
                        "USER",
                        Map.of("requiredFieldsComplete", true),
                        "k1"));

        assertThat(submitResult.state()).isEqualTo(ApplicationStatus.SUBMITTED);

        var reviewResult = workflowService.handleEvent(
                application,
                new ApplicationWorkflowService.ApplicationEventCommand(
                        ApplicationEventType.START_REVIEW,
                        2L,
                        "REVIEWER",
                        Map.of(),
                        "k2"));

        assertThat(reviewResult.state()).isEqualTo(ApplicationStatus.UNDER_REVIEW);

        var approveResult = workflowService.handleEvent(
                application,
                new ApplicationWorkflowService.ApplicationEventCommand(
                        ApplicationEventType.APPROVE,
                        2L,
                        "REVIEWER",
                        Map.of("checklistComplete", true, "petAvailable", true),
                        "k3"));

        assertThat(approveResult.state()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    void shouldBlockInvalidTransition() {
        Application application = new Application();
        application.setUser(new User());
        application.setPet(new Pet());
        application.setBasvuruDurumu(ApplicationStatus.UNDER_REVIEW);

        var result = workflowService.handleEvent(
                application,
                new ApplicationWorkflowService.ApplicationEventCommand(
                        ApplicationEventType.APPROVE,
                        1L,
                        "REVIEWER",
                        Map.of("checklistComplete", false, "petAvailable", true),
                        "k4"));

        assertThat(result.reasonCode()).isEqualTo("CHECKS_FAILED");
        assertThat(application.getBasvuruDurumu()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }
}
