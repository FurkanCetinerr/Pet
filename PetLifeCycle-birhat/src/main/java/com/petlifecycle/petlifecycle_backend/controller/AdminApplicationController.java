package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.service.AdminApplicationService;
import com.petlifecycle.petlifecycle_backend.service.ApplicationWorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    public AdminApplicationController(AdminApplicationService adminApplicationService) {
        this.adminApplicationService = adminApplicationService;
    }

    @GetMapping("/pending")
    public List<Application> listPending() {
        return adminApplicationService.listPendingApplications();
    }

    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<ApplicationWorkflowService.TransitionResult> approve(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApprovalRequest request) {

        var result = adminApplicationService.approve(new AdminApplicationService.ApprovalCommand(
                applicationId,
                request.adminId(),
                request.checklistComplete(),
                request.petAvailable(),
                request.idempotencyKey()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<ApplicationWorkflowService.TransitionResult> reject(
            @PathVariable Long applicationId,
            @Valid @RequestBody RejectRequest request) {

        var result = adminApplicationService.reject(new AdminApplicationService.RejectCommand(
                applicationId,
                request.adminId(),
                request.reason(),
                request.idempotencyKey()));
        return ResponseEntity.ok(result);
    }

    public record ApprovalRequest(
            @NotNull Long adminId,
            boolean checklistComplete,
            boolean petAvailable,
            @NotBlank String idempotencyKey) {
    }

    public record RejectRequest(
            @NotNull Long adminId,
            @NotBlank String reason,
            @NotBlank String idempotencyKey) {
    }
}
