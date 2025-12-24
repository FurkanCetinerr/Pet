package com.petlifecycle.petlifecycle_backend.service.strategy;

import com.petlifecycle.petlifecycle_backend.service.PetHealthData;
import com.petlifecycle.petlifecycle_backend.service.VaccineEvent;
import com.petlifecycle.petlifecycle_backend.service.VaccineScheduleResult;
import java.util.List;

public interface PetHealthStrategy {
    List<VaccineScheduleResult> analyze(PetHealthData pet, List<VaccineEvent> history);
}
