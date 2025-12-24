package com.petlifecycle.petlifecycle_backend.service;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.model.ApplicationStatus;
import com.petlifecycle.petlifecycle_backend.repository.ApplicationRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationWorkflowService workflowService;

    @InjectMocks
    private AdminApplicationService adminApplicationService;

    private Application application;

    @BeforeEach
    void init() {
        application = new Application();
        application.setBasvuruDurumu(ApplicationStatus.SUBMITTED);
    }

    @Test
    void shouldListPendingApplications() {
        when(applicationRepository.findByBasvuruDurumuIn(any()))
                .thenReturn(List.of(application));

        List<Application> result = adminApplicationService.listPendingApplications();

        assertThat(result).hasSize(1);
        verify(applicationRepository).findByBasvuruDurumuIn(any());
    }

    @Test
    void shouldApproveApplicationViaWorkflow() {
        when(applicationRepository.findById(1L)).thenReturn(java.util.Optional.of(application));
        ApplicationWorkflowService.TransitionResult transitionResult =
                new ApplicationWorkflowService.TransitionResult(1L, ApplicationStatus.APPROVED, false, null);
        when(workflowService.handleEvent(any(), any())).thenReturn(transitionResult);

        var command = new AdminApplicationService.ApprovalCommand(1L, 10L, true, true, "idem-1");
        var result = adminApplicationService.approve(command);

        assertThat(result.state()).isEqualTo(ApplicationStatus.APPROVED);
        verify(applicationRepository).save(application);
    }

    @Test
    void shouldRejectApplicationViaWorkflow() {
        when(applicationRepository.findById(2L)).thenReturn(java.util.Optional.of(application));
        when(workflowService.handleEvent(any(), any()))
                .thenReturn(new ApplicationWorkflowService.TransitionResult(2L, ApplicationStatus.REJECTED, false, null));

        var command = new AdminApplicationService.RejectCommand(2L, 11L, "Eksik evrak", "idem-2");
        var result = adminApplicationService.reject(command);

        assertThat(result.state()).isEqualTo(ApplicationStatus.REJECTED);
        verify(applicationRepository).save(application);
    }
}
