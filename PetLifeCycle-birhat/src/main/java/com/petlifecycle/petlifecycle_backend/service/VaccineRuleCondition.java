package com.petlifecycle.petlifecycle_backend.service;

import java.time.LocalDate;

public record VaccineRuleCondition(
        Integer minAgeDays,
        Integer maxAgeDays,
        Double minWeightKg,
        Double maxWeightKg) {

    public boolean matches(long ageDays, double weightKg) {
        if (minAgeDays != null && ageDays < minAgeDays) {
            return false;
        }
        if (maxAgeDays != null && ageDays > maxAgeDays) {
            return false;
        }
        if (minWeightKg != null) {
            if (weightKg == 0 || weightKg < minWeightKg) {
                return false;
            }
        }
        if (maxWeightKg != null && weightKg > maxWeightKg) {
            return false;
        }
        return true;
    }
}
