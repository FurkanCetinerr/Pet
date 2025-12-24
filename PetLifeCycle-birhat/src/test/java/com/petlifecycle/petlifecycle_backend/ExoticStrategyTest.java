package com.petlifecycle.petlifecycle_backend;

import com.petlifecycle.petlifecycle_backend.model.PetType;
import com.petlifecycle.petlifecycle_backend.service.PetHealthData;
import com.petlifecycle.petlifecycle_backend.service.PetVaccineScheduler;
import com.petlifecycle.petlifecycle_backend.service.VaccineScheduleResult;
import com.petlifecycle.petlifecycle_backend.service.strategy.DomesticPetStrategy;
import com.petlifecycle.petlifecycle_backend.service.strategy.ExoticPetStrategy;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExoticStrategyTest {

    private final DomesticPetStrategy domestic = new DomesticPetStrategy();
    private final ExoticPetStrategy exotic = new ExoticPetStrategy();
    private final PetVaccineScheduler scheduler = new PetVaccineScheduler(domestic, exotic);

    @Test
    void shouldReturnDietControlForExoticPet() {
        PetHealthData exoticPet = new PetHealthData(
                PetType.EXOTIC,
                LocalDate.now().minusDays(100),
                0.5,
                List.of());

        List<VaccineScheduleResult> results = scheduler.buildSchedule(exoticPet, Collections.emptyList());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DIET-CONTROL", results.get(0).vaccineCode());
        assertEquals("Exotic animal diet check required", results.get(0).reason());
    }

    @Test
    void shouldReturnDietControlForBird() {
        PetHealthData bird = new PetHealthData(
                PetType.KUS,
                LocalDate.now().minusDays(50),
                0.1,
                List.of());

        List<VaccineScheduleResult> results = scheduler.buildSchedule(bird, Collections.emptyList());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DIET-CONTROL", results.get(0).vaccineCode());
    }
}
