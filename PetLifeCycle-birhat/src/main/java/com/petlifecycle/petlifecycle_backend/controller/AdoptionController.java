package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.*;
import com.petlifecycle.petlifecycle_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // <-- Eklendi
import org.springframework.web.bind.annotation.*;

import java.util.List; // <-- LİST HATASI İÇİN BU ŞART
import java.util.Map;

@RestController
@RequestMapping("/api/adoption") 
@CrossOrigin(origins = "*") 
public class AdoptionController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. BAŞVURU YAPMA METODU
    @PostMapping("/apply")
    public Application applyForAdoption(@RequestBody Map<String, Object> payload) {
        // 1. Verileri Al
        Long petId = Long.valueOf(payload.get("petId").toString());
        Long userId = Long.valueOf(payload.get("userId").toString());
        String details = (String) payload.get("details");

        // 2. Kullanıcı Kontrolü
        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setAd("Otomatik Kullanıcı"); 
            newUser.setRol(Role.USER);
            return userRepository.save(newUser);
        });

        // 3. Hayvanı Bul
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Hayvan bulunamadı!"));

        // 4. Başvuruyu Kaydet
        Application app = new Application();
        app.setPet(pet);
        app.setUser(user);
        app.setRequestedInfoDetails(details);
        app.setBasvuruDurumu(ApplicationStatus.DRAFT); 

        return applicationRepository.save(app);
    } 

    // 2. KULLANICI BAŞVURULARINI GETİRME METODU (ARTIK DIŞARIDA)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Application>> getUserApplications(@PathVariable Long userId) {
        List<Application> applications = applicationRepository.findByUserId(userId);
        return ResponseEntity.ok(applications);
    }
}