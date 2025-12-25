package com.petlifecycle.petlifecycle_backend.controller;

import com.petlifecycle.petlifecycle_backend.model.*;
import com.petlifecycle.petlifecycle_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // ÖNEMLİ: Toplu silme için

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired 
    private VaccineScheduleRepository vaccineScheduleRepository;

    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository; // Bunu yeni ekledik

    // 1. TÜM HAYVANLARI LİSTELE
    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    // 2. TEK BİR HAYVANIN DETAYINI GETİR
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        return petRepository.findById(id)
                .map(pet -> ResponseEntity.ok(pet))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. YENİ HAYVAN EKLE
    @PostMapping
    public Pet createPet(@RequestBody Pet pet) {
        if(pet.getDurum() == null) {
            pet.setDurum(PetStatus.AVAILABLE);
        }
        return petRepository.save(pet);
    }

    // 4. AŞI TAKVİMİNİ GETİR
    @GetMapping("/{petId}/schedule")
    public ResponseEntity<List<VaccineSchedule>> getPetSchedule(@PathVariable Long petId) {
        List<VaccineSchedule> schedule = vaccineScheduleRepository.findByPetId(petId);
        return ResponseEntity.ok(schedule);
    }

    // --- GÜNCELLENEN SİLME METODU ---
    @DeleteMapping("/{id}")
    @Transactional // İşlem sırasında hata olursa her şeyi geri al (Rollback)
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        if(!petRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("Hayvan siliniyor ID: " + id);

        // A. Önce bu hayvana ait Aşı Takvimlerini sil
        List<VaccineSchedule> asilar = vaccineScheduleRepository.findByPetId(id);
        vaccineScheduleRepository.deleteAll(asilar);
        System.out.println(asilar.size() + " aşı kaydı silindi.");

        // B. Sonra bu hayvana ait Başvuruları sil
        List<Application> basvurular = applicationRepository.findByPetId(id);
        applicationRepository.deleteAll(basvurular);
        System.out.println(basvurular.size() + " başvuru kaydı silindi.");

        // C. En son Hayvanı sil
        petRepository.deleteById(id);
        System.out.println("Hayvan başarıyla silindi.");

        return ResponseEntity.ok("Hayvan ve ilgili tüm veriler silindi.");
    }

    // --- GÜNCELLENEN STATÜ DEĞİŞTİRME METODU ---
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePetStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        System.out.println("Statü güncelleme isteği geldi. ID: " + id);
        
        return petRepository.findById(id)
                .map(pet -> {
                    String newStatus = payload.get("status");
                    System.out.println("Yeni Statü Değeri: " + newStatus);

                    try {
                        // Gelen string değeri (örn: "ADOPTED") Enum'a çeviriyoruz
                        PetStatus statusEnum = PetStatus.valueOf(newStatus); 
                        pet.setDurum(statusEnum);
                        petRepository.save(pet);
                        System.out.println("Güncelleme Başarılı!");
                        return ResponseEntity.ok(pet);
                    } catch (Exception e) {
                        System.err.println("HATA: Statü çevrilemedi! " + e.getMessage());
                        return ResponseEntity.badRequest().body("Hatalı statü değeri: " + newStatus);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}