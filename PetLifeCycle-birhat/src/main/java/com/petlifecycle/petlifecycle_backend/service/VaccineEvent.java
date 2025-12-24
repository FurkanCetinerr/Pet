package com.petlifecycle.petlifecycle_backend.service;

import java.time.LocalDate;

public record VaccineEvent(
        String vaccineCode,
        LocalDate date,
        Integer doseNo) {
}
