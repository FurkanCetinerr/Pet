package com.petlifecycle.petlifecycle_backend.service;

import com.petlifecycle.petlifecycle_backend.model.PetType;
import java.time.LocalDate;
import java.util.List;

public record PetHealthData(
        PetType species,
        LocalDate birthDate,
        Double currentWeightKg,
        List<WeightRecord> weightHistory) {
}
