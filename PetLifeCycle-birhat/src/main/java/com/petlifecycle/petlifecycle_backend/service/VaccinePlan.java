package com.petlifecycle.petlifecycle_backend.service;

public record VaccinePlan(
        PlanType type,
        Integer doses,
        Integer intervalDays,
        Integer restartThresholdDays,
        BoosterPlan booster) {

    public static VaccinePlan oneShot(BoosterPlan booster) {
        return new VaccinePlan(PlanType.ONE_SHOT, 1, null, null, booster);
    }

    public static VaccinePlan series(int doses, int intervalDays, Integer restartThresholdDays, BoosterPlan booster) {
        return new VaccinePlan(PlanType.SERIES, doses, intervalDays, restartThresholdDays, booster);
    }
}
