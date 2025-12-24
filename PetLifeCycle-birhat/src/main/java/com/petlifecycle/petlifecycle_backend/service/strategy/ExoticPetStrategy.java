package com.petlifecycle.petlifecycle_backend.service.strategy;

import com.petlifecycle.petlifecycle_backend.service.PetHealthData;
import com.petlifecycle.petlifecycle_backend.service.VaccineEvent;
import com.petlifecycle.petlifecycle_backend.service.VaccineScheduleResult;
import com.petlifecycle.petlifecycle_backend.service.VaccineStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExoticPetStrategy implements PetHealthStrategy {

    @Override
    public List<VaccineScheduleResult> analyze(PetHealthData pet, List<VaccineEvent> history) {
        // Exotic animals don't have vaccines, only diet control.
        return List.of(new VaccineScheduleResult(
                "DIET-CONTROL",
                VaccineStatus.SCHEDULED,
                LocalDate.now(),
                "Exotic animal diet check required",
                null));
    }
}
