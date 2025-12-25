package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.*;
import com.petlifecycle.petlifecycle_backend.service.AdminApplicationService;
import com.petlifecycle.petlifecycle_backend.service.ApplicationWorkflowService;
import com.petlifecycle.petlifecycle_backend.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
@CrossOrigin(origins = "*")
public class AdminApplicationController {

    @Autowired
    private AdminApplicationService adminService;

    @Autowired
    private ApplicationRepository applicationRepository;

    // 1. BEKLEYEN BAŞVURULARI LİSTELE
    @GetMapping("/pending")
    public ResponseEntity<List<Application>> getPendingApplications() {
        // PENDING yerine SUBMITTED (Yeni gelmiş) ve UNDER_REVIEW (İnceleniyor) olanları getiriyoruz
        List<Application> pending = applicationRepository.findAll().stream()
                .filter(app -> app.getBasvuruDurumu() == ApplicationStatus.SUBMITTED 
                            || app.getBasvuruDurumu() == ApplicationStatus.UNDER_REVIEW)
                .toList();
        return ResponseEntity.ok(pending);
    }

    // 2. ONAYLA (Approve)
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long applicationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        if(idempotencyKey == null || idempotencyKey.isEmpty()) {
             return ResponseEntity.badRequest().body("Idempotency-Key header is missing!");
        }

        try {
            // Servisin beklediği ApprovalCommand nesnesini oluşturuyoruz
            // adminId: Şimdilik 1L veriyoruz (Login sisteminden dinamik alınabilir)
            // checklistComplete: true, petAvailable: true varsayıyoruz
            AdminApplicationService.ApprovalCommand command = new AdminApplicationService.ApprovalCommand(
                    applicationId,
                    1L, // Admin ID (Sabit 1 varsaydık)
                    true, // Checklist tamam mı?
                    true, // Hayvan müsait mi?
                    idempotencyKey
            );

            // evaluateApplication yerine approve metodunu çağırıyoruz
            ApplicationWorkflowService.TransitionResult result = adminService.approve(command);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. REDDET (Reject)
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
            
        String reason = body.getOrDefault("reason", "Uygun görülmedi");
        
        try {
            // Servisin beklediği RejectCommand nesnesini oluşturuyoruz
            AdminApplicationService.RejectCommand command = new AdminApplicationService.RejectCommand(
                    applicationId,
                    1L, // Admin ID
                    reason,
                    idempotencyKey
            );

            // evaluateApplication yerine reject metodunu çağırıyoruz
            ApplicationWorkflowService.TransitionResult result = adminService.reject(command);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}