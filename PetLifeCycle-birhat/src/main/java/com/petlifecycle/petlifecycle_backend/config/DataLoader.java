package com.petlifecycle.petlifecycle_backend.config;

import com.petlifecycle.petlifecycle_backend.model.*;
import com.petlifecycle.petlifecycle_backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(PetRepository petRepository, UserRepository userRepository) {
        return args -> {
            //ADMİN Ekle (Sabit)
            if (userRepository.findByEmail("admin@pet.com").isEmpty()) {
                User admin = new User();
                admin.setAd("Sistem Yöneticisi");
                admin.setEmail("admin@pet.com");
                admin.setSifre("admin123"); // Admin şifresi
                admin.setRol(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ ADMIN Eklendi: admin@pet.com / admin123");
            }
            // 1. Önce Kullanıcı Ekle (Eğer yoksa)
            if (userRepository.count() == 0) {
                User user = new User();
                // Modelindeki alan adlarına göre ayarla (username/ad/email olabilir)
                user.setAd("Mert"); 
                user.setRol(Role.USER);
                
                userRepository.save(user);
                System.out.println("✅ Kullanıcı Eklendi: Mert (ID: 1)");
            }

            // 2. Hayvanları Ekle
            if (petRepository.count() == 0) {
                // Kedi
                Pet kedi = new Pet();
                kedi.setIsim("Pamuk");
                kedi.setTur(PetType.KEDI);
                kedi.setYas(2);
                kedi.setKilo(4.5);
                kedi.setDurum(PetStatus.AVAILABLE);
                kedi.setResimUrl("images/gallery-1.jpg");
                petRepository.save(kedi);

                // Köpek
                Pet kopek = new Pet();
                kopek.setIsim("Karabaş");
                kopek.setTur(PetType.KOPEK);
                kopek.setYas(3);
                kopek.setKilo(12.0);
                kopek.setDurum(PetStatus.AVAILABLE);
                kopek.setResimUrl("images/gallery-2.jpg");
                petRepository.save(kopek);

                // EGZOTİK (Yılan)
                Pet yilan = new Pet();
                yilan.setIsim("Slytherin");
                yilan.setTur(PetType.EXOTIC);
                yilan.setYas(1);
                yilan.setKilo(0.5);
                yilan.setDurum(PetStatus.AVAILABLE);
                yilan.setResimUrl("images/gallery-3.jpg");
                petRepository.save(yilan);
                
                System.out.println("✅ Hayvanlar Eklendi: Pamuk, Karabaş, Slytherin");
            }
        };
    }
}