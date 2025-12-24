package com.petlifecycle.petlifecycle_backend.service;

import java.time.LocalDate;

public record VaccineScheduleResult(
        String vaccineCode,
        VaccineStatus status,
        LocalDate dueDate,
        String reason,
        VaccineRule selectedRule) {
}
