package com.petlifecycle.petlifecycle_backend.repository;

import com.petlifecycle.petlifecycle_backend.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    // Hazır metodlar (findAll, save, findById) otomatik gelir.
}