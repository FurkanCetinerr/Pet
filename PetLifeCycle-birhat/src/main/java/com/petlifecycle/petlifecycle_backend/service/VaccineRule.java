package com.petlifecycle.petlifecycle_backend.service;

public record VaccineRule(
        VaccineRuleCondition when,
        VaccinePlan plan,
        int priority) {
}
