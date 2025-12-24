package com.petlifecycle.petlifecycle_backend.service;

import java.time.LocalDate;

public record WeightRecord(
        LocalDate date,
        Double weightKg) {
}
