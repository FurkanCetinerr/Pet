package com.petlifecycle.petlifecycle_backend.repository;

import com.petlifecycle.petlifecycle_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hazır metodlar (findById, save) otomatik gelir.
}