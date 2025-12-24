package com.petlifecycle.petlifecycle_backend.service;

import com.petlifecycle.petlifecycle_backend.model.PetType;
import com.petlifecycle.petlifecycle_backend.service.strategy.DomesticPetStrategy;
import com.petlifecycle.petlifecycle_backend.service.strategy.ExoticPetStrategy;
import com.petlifecycle.petlifecycle_backend.service.strategy.PetHealthStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Evcil hayvanların yaş/kilo bilgelerine ve aşı geçmişine göre hangi aşıların
 * ne zaman yapılması gerektiğini hesaplar.
 * Strategy Pattern kullanılarak tür bazlı mantık ayrıştırılmıştır.
 */
@Service
public class PetVaccineScheduler {

    private final Map<PetType, PetHealthStrategy> strategies = new EnumMap<>(PetType.class);

    public PetVaccineScheduler(DomesticPetStrategy domesticPetStrategy, ExoticPetStrategy exoticPetStrategy) {
        strategies.put(PetType.KOPEK, domesticPetStrategy);
        strategies.put(PetType.KEDI, domesticPetStrategy);
        strategies.put(PetType.KUS, exoticPetStrategy);
        strategies.put(PetType.TAVSAN, exoticPetStrategy);
        strategies.put(PetType.EXOTIC, exoticPetStrategy);
    }

    /**
     * Stratejiyi seçer ve analiz isteğini iletir.
     */
    public List<VaccineScheduleResult> buildSchedule(
            PetHealthData pet,
            List<VaccineEvent> vaccineHistory) {

        PetHealthStrategy strategy = strategies.get(pet.species());
        if (strategy == null) {
            // Default behavior if type is not mapped (safe fallback: empty list)
            return List.of();
        }
        return strategy.analyze(pet, vaccineHistory);
    }
}
