package com.petlifecycle.petlifecycle_backend.config;

import com.petlifecycle.petlifecycle_backend.model.Pet;
import com.petlifecycle.petlifecycle_backend.model.PetStatus;
import com.petlifecycle.petlifecycle_backend.model.PetType;
import com.petlifecycle.petlifecycle_backend.repository.PetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(PetRepository repository) {
        return args -> {
            if (repository.count() == 0) { // Sadece boşsa ekle
                // 1. Kedi (Resimli)
                Pet kedi = new Pet();
                kedi.setIsim("Pamuk"); // Modeldeki alan adlarına dikkat et (isim/name?)
                kedi.setTur(PetType.KEDI);
                kedi.setYas(2);
                kedi.setKilo(4.5);
                kedi.setDurum(PetStatus.AVAILABLE);
                kedi.setResimUrl("images/gallery-1.jpg"); // Template resim yolu
                repository.save(kedi);

                // 2. Köpek
                Pet kopek = new Pet();
                kopek.setIsim("Karabaş");
                kopek.setTur(PetType.KOPEK);
                kopek.setYas(3);
                kopek.setKilo(12.0);
                kopek.setDurum(PetStatus.AVAILABLE);
                kopek.setResimUrl("images/gallery-2.jpg");
                repository.save(kopek);

                // 3. EGZOTİK (Şov kısmı burası)
                Pet yilan = new Pet();
                yilan.setIsim("Slytherin");
                yilan.setTur(PetType.EXOTIC); // <--- KRİTİK NOKTA
                yilan.setYas(1);
                yilan.setKilo(0.5);
                yilan.setDurum(PetStatus.AVAILABLE);
                yilan.setResimUrl("images/gallery-3.jpg");
                repository.save(yilan);
                
                System.out.println("--- DEMO VERİLERİ YÜKLENDİ ---");
            }
        };
    }
}