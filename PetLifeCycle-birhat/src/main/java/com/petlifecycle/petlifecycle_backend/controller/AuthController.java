package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.Role;
import com.petlifecycle.petlifecycle_backend.model.User;
import com.petlifecycle.petlifecycle_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Frontend erişimi için
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // LOGIN İŞLEMİ
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // --- CASUS KODLAR (DEBUG) ---
        System.out.println("LOGİN DENEMESİ GELDİ!");
        System.out.println("Gelen Email: " + loginRequest.getEmail());
        System.out.println("Gelen Şifre: " + loginRequest.getSifre());
        // -----------------------------
        Optional<User> user = userRepository.findByEmailAndSifre(loginRequest.getEmail(), loginRequest.getSifre());

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get()); // Kullanıcı bulundu, bilgileri dön
        } else {
            return ResponseEntity.status(401).body("Hatalı email veya şifre!");
        }
    }

    // REGISTER İŞLEMİ (Sadece Normal Kullanıcı)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu email zaten kayıtlı!");
        }

        // Yeni kayıt olan herkes USER rolünü alır.
        newUser.setRol(Role.USER);
        userRepository.save(newUser);
        
        return ResponseEntity.ok("Kayıt başarılı!");
    }
}