package com.petlifecycle.petlifecycle_backend.controller;

import java.util.HashMap;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/adopt")
public class AdoptionController {

    // Mert bir butona bastığında buraya POST isteği atacak
    @PostMapping("/{petId}")
    public String applyForAdoption(@PathVariable Long petId, @RequestBody Map<String, Object> applicationDetails) {
        // Şimdilik sadece başvurunun alındığını Mert'e bildirelim
        System.out.println("Pet ID: " + petId + " için başvuru geldi.");
        return "Basvuru basariyla alindi! Pet ID: " + petId;
    }
    @GetMapping("/admin/applications")
public List<Map<String, Object>> getPendingApplications() {
    // Şimdilik Mert'e boş liste gitmesin diye örnek bir başvuru
    Map<String, Object> app1 = new HashMap<>();
    app1.put("id", 101);
    app1.put("petName", "Pamuk");
    app1.put("applicantName", "Ahmet Yılmaz");
    app1.put("status", "PENDING");
    
    return java.util.Arrays.asList(app1);
}
}
