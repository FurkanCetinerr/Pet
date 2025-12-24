package com.petlifecycle.petlifecycle_backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        Map<String, Object> response = new HashMap<>();

        // Şimdilik test için basit bir kontrol:
        if ("admin@test.com".equals(email) && "123456".equals(password)) {
            response.put("success", true);
            response.put("message", "Giriş başarılı!");
            response.put("userRole", "ADMIN");
        } else {
            response.put("success", false);
            response.put("message", "Hatalı e-posta veya şifre!");
        }
        
        return response;
    }
}