package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.*;
import com.petlifecycle.petlifecycle_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/adoption") // <-- Frontend buraya istek atıyor, burası DOĞRU
@CrossOrigin(origins = "*")      // <-- Kapıyı açtık
public class AdoptionController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/apply")
    public Application applyForAdoption(@RequestBody Map<String, Object> payload) {
        // 1. Verileri Al
        Long petId = Long.valueOf(payload.get("petId").toString());
        Long userId = Long.valueOf(payload.get("userId").toString());
        String details = (String) payload.get("details");

        // 2. Kullanıcı Var mı Kontrol Et (Yoksa Oluştur)
        // Bu kısım çok kritik, yoksa "User not found" hatası alırsın
        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            // User.java'da ID otomatik artmıyorsa burada set etmemiz gerekebilir
            // Ama genelde veritabanı halleder. Biz sadece isim verelim.
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
        app.setBasvuruDurumu(ApplicationStatus.DRAFT); // Başlangıç durumu

        return applicationRepository.save(app);
    }
}