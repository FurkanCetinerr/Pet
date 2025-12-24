package com.petlifecycle.petlifecycle_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class VaccineSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi Pet (ManyToOne)
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    private String asiAdi;
    private LocalDate asiTarihi;
    private LocalDate sonrakiTarih; 

    // Getterlar ve Setterlar buraya eklenecek

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getAsiAdi() {
        return asiAdi;
    }

    public void setAsiAdi(String asiAdi) {
        this.asiAdi = asiAdi;
    }

    public LocalDate getAsiTarihi() {
        return asiTarihi;
    }

    public void setAsiTarihi(LocalDate asiTarihi) {
        this.asiTarihi = asiTarihi;
    }

    public LocalDate getSonrakiTarih() {
        return sonrakiTarih;
    }

    public void setSonrakiTarih(LocalDate sonrakiTarih) {
        this.sonrakiTarih = sonrakiTarih;
    }
}
