package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.Pet;
import com.petlifecycle.petlifecycle_backend.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*") // <--- FRONTEND İÇİN KAPILARI AÇTIK
public class PetController {

    @Autowired
    private PetRepository petRepository;

    // Tüm hayvanları listele
    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    // Tek bir hayvanın detayını getir
    @GetMapping("/{id}")
    public Pet getPetById(@PathVariable Long id) {
        return petRepository.findById(id).orElse(null);
    }
    
    // Yeni hayvan ekle (Admin paneli için opsiyonel)
    @PostMapping
    public Pet createPet(@RequestBody Pet pet) {
        return petRepository.save(pet);
    }
}