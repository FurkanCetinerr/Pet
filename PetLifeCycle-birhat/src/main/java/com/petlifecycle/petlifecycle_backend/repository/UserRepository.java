package com.petlifecycle.petlifecycle_backend.repository;

import com.petlifecycle.petlifecycle_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Giriş işlemi için kullanıcıyı email ve şifre ile bul
    Optional<User> findByEmailAndSifre(String email, String sifre);
    
    // Kayıt olurken bu email var mı diye kontrol et
    Optional<User> findByEmail(String email);
}