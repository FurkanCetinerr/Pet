package com.petlifecycle.petlifecycle_backend.service.strategy;

import com.petlifecycle.petlifecycle_backend.model.PetType;
import com.petlifecycle.petlifecycle_backend.service.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DomesticPetStrategy implements PetHealthStrategy {

    private static final int DUE_SOON_THRESHOLD_DAYS = 7;
    private final Map<PetType, Map<String, List<VaccineRule>>> protocolBySpecies = new EnumMap<>(PetType.class);

    public DomesticPetStrategy() {
        protocolBySpecies.put(PetType.KOPEK, createDogProtocol());
        protocolBySpecies.put(PetType.KEDI, createCatProtocol());
    }

    @Override
    public List<VaccineScheduleResult> analyze(PetHealthData pet, List<VaccineEvent> history) {
        LocalDate today = LocalDate.now();
        long ageDays = ChronoUnit.DAYS.between(pet.birthDate(), today);
        Map<String, List<VaccineEvent>> historyByCode = groupEventsByCode(history);
        Map<String, List<VaccineRule>> protocol = protocolBySpecies.getOrDefault(pet.species(), Map.of());

        List<VaccineScheduleResult> results = new ArrayList<>();
        for (Map.Entry<String, List<VaccineRule>> entry : protocol.entrySet()) {
            String vaccineCode = entry.getKey();
            List<VaccineEvent> events = historyByCode.getOrDefault(vaccineCode, List.of());
            VaccineScheduleResult result = evaluateVaccine(
                    vaccineCode,
                    entry.getValue(),
                    pet,
                    events,
                    ageDays,
                    today);
            results.add(result);
        }

        results.sort(Comparator.comparing(
                r -> r.dueDate() == null ? LocalDate.MAX : r.dueDate()));
        return results;
    }

    private VaccineScheduleResult evaluateVaccine(
            String vaccineCode,
            List<VaccineRule> rules,
            PetHealthData pet,
            List<VaccineEvent> events,
            long ageDays,
            LocalDate today) {

        double currentWeight = pet.currentWeightKg() == null ? 0 : pet.currentWeightKg();
        List<VaccineRule> eligibleRules = rules.stream()
                .filter(rule -> rule.when().matches(ageDays, currentWeight))
                .collect(Collectors.toList());

        if (eligibleRules.isEmpty()) {
            LocalDate earliest = null;
            String reason = "age/weight not eligible";
            boolean ageWindowPassed = false;

            for (VaccineRule rule : rules) {
                VaccineRuleCondition condition = rule.when();
                if (condition.maxAgeDays() != null && ageDays > condition.maxAgeDays()) {
                    ageWindowPassed = true;
                    continue;
                }
                LocalDate candidate = computeEligibilityDate(condition, pet);
                if (candidate != null) {
                    if (earliest == null || candidate.isBefore(earliest)) {
                        earliest = candidate;
                    }
                }
            }

            if (ageWindowPassed && earliest == null) {
                reason = "age window passed";
            }

            return new VaccineScheduleResult(
                    vaccineCode,
                    VaccineStatus.NOT_ELIGIBLE,
                    earliest,
                    reason,
                    null);
        }

        VaccineRule selectedRule = eligibleRules.stream()
                .max(Comparator.comparingInt(VaccineRule::priority))
                .orElseThrow();
        LocalDate eligibilityDate = computeEligibilityDate(selectedRule.when(), pet);
        List<VaccineEvent> orderedEvents = new ArrayList<>(events);
        orderedEvents.sort(Comparator.comparing(VaccineEvent::date));

        return switch (selectedRule.plan().type()) {
            case ONE_SHOT ->
                evaluateOneShot(vaccineCode, orderedEvents, selectedRule, today, eligibilityDate);
            case SERIES ->
                evaluateSeries(vaccineCode, orderedEvents, selectedRule, today, eligibilityDate);
        };
    }

    private VaccineScheduleResult evaluateOneShot(
            String vaccineCode,
            List<VaccineEvent> events,
            VaccineRule selectedRule,
            LocalDate today,
            LocalDate eligibilityDate) {

        BoosterPlan booster = selectedRule.plan().booster();

        if (!events.isEmpty()) {
            if (booster == null) {
                return new VaccineScheduleResult(
                        vaccineCode,
                        VaccineStatus.COMPLETED,
                        null,
                        "completed single dose",
                        selectedRule);
            }

            if (events.size() == 1) {
                LocalDate due = events.get(0).date().plusDays(booster.firstBoosterAfterDays());
                return buildResult(
                        vaccineCode,
                        selectedRule,
                        due,
                        today,
                        "first booster scheduled");
            }

            LocalDate due = events.get(events.size() - 1).date().plusDays(booster.repeatEveryDays());
            return buildResult(
                    vaccineCode,
                    selectedRule,
                    due,
                    today,
                    "booster repeat");
        }

        LocalDate due = adjustToEligibility(today, eligibilityDate);
        return buildResult(vaccineCode, selectedRule, due, today, "initial dose");
    }

    private VaccineScheduleResult evaluateSeries(
            String vaccineCode,
            List<VaccineEvent> events,
            VaccineRule selectedRule,
            LocalDate today,
            LocalDate eligibilityDate) {

        int doses = selectedRule.plan().doses() == null ? 1 : selectedRule.plan().doses();
        int interval = selectedRule.plan().intervalDays() == null ? 30 : selectedRule.plan().intervalDays();
        Integer restartThreshold = selectedRule.plan().restartThresholdDays();
        BoosterPlan booster = selectedRule.plan().booster();
        int doseCount = events.size();

        if (doseCount == 0) {
            LocalDate due = adjustToEligibility(today, eligibilityDate);
            return buildResult(
                    vaccineCode,
                    selectedRule,
                    due,
                    today,
                    "series dose 1");
        }

        LocalDate lastDate = events.get(events.size() - 1).date();
        if (doseCount < doses) {
            LocalDate due = lastDate.plusDays(interval);
            if (restartThreshold != null && today.isAfter(due.plusDays(restartThreshold))) {
                LocalDate restartDue = adjustToEligibility(today, eligibilityDate);
                return new VaccineScheduleResult(
                        vaccineCode,
                        VaccineStatus.NEED_RESTART,
                        restartDue,
                        "series overdue -> restart",
                        selectedRule);
            }

            return buildResult(
                    vaccineCode,
                    selectedRule,
                    due,
                    today,
                    "series next dose (" + (doseCount + 1) + "/" + doses + ")");
        }

        if (doseCount >= doses) {
            if (booster == null) {
                return new VaccineScheduleResult(
                        vaccineCode,
                        VaccineStatus.COMPLETED,
                        null,
                        "series completed",
                        selectedRule);
            }

            LocalDate primaryLast = events.get(doses - 1).date();
            if (doseCount == doses) {
                LocalDate due = primaryLast.plusDays(booster.firstBoosterAfterDays());
                return buildResult(
                        vaccineCode,
                        selectedRule,
                        due,
                        today,
                        "first booster");
            }

            LocalDate due = lastDate.plusDays(booster.repeatEveryDays());
            return buildResult(
                    vaccineCode,
                    selectedRule,
                    due,
                    today,
                    "booster repeat");
        }

        return new VaccineScheduleResult(
                vaccineCode,
                VaccineStatus.COMPLETED,
                null,
                "series completed",
                selectedRule);
    }

    private VaccineScheduleResult buildResult(
            String vaccineCode,
            VaccineRule selectedRule,
            LocalDate due,
            LocalDate today,
            String reason) {

        VaccineStatus status = determineStatus(today, due);
        return new VaccineScheduleResult(vaccineCode, status, due, reason, selectedRule);
    }

    private VaccineStatus determineStatus(LocalDate today, LocalDate due) {
        if (due == null) {
            return VaccineStatus.COMPLETED;
        }
        if (today.isAfter(due)) {
            return VaccineStatus.OVERDUE;
        }
        if (today.isEqual(due)) {
            return VaccineStatus.DUE_TODAY;
        }
        if (!today.plusDays(DUE_SOON_THRESHOLD_DAYS).isBefore(due)) {
            return VaccineStatus.DUE_SOON;
        }
        return VaccineStatus.SCHEDULED;
    }

    private LocalDate adjustToEligibility(LocalDate today, LocalDate eligibilityDate) {
        if (eligibilityDate == null || !eligibilityDate.isAfter(today)) {
            return today;
        }
        return eligibilityDate;
    }

    private LocalDate computeEligibilityDate(VaccineRuleCondition when, PetHealthData pet) {
        LocalDate baseDate = pet.birthDate();

        if (when.maxAgeDays() != null) {
            LocalDate maxDate = baseDate.plusDays(when.maxAgeDays());
            if (LocalDate.now().isAfter(maxDate)) {
                return null;
            }
        }

        LocalDate ageDate = when.minAgeDays() != null ? baseDate.plusDays(when.minAgeDays()) : baseDate;
        LocalDate weightDate = null;
        if (when.minWeightKg() != null) {
            weightDate = estimateWeightDate(pet, when.minWeightKg());
            if (weightDate == null) {
                return null;
            }
        }

        if (weightDate == null) {
            return ageDate;
        }
        return weightDate.isAfter(ageDate) ? weightDate : ageDate;
    }

    private LocalDate estimateWeightDate(PetHealthData pet, double targetWeight) {
        if (pet.currentWeightKg() != null && pet.currentWeightKg() >= targetWeight) {
            return LocalDate.now();
        }

        List<WeightRecord> history = pet.weightHistory();
        if (history == null || history.size() < 2) {
            return null;
        }

        List<WeightRecord> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(WeightRecord::date));

        for (WeightRecord record : sorted) {
            if (record.weightKg() >= targetWeight) {
                return record.date();
            }
        }

        WeightRecord last = sorted.get(sorted.size() - 1);
        WeightRecord prev = sorted.get(sorted.size() - 2);
        long daysBetween = ChronoUnit.DAYS.between(prev.date(), last.date());
        if (daysBetween <= 0) {
            return null;
        }

        double dailyGain = (last.weightKg() - prev.weightKg()) / daysBetween;
        if (dailyGain <= 0) {
            return null;
        }

        double remaining = targetWeight - last.weightKg();
        long daysNeeded = (long) Math.ceil(remaining / dailyGain);
        return last.date().plusDays(daysNeeded);
    }

    private Map<String, List<VaccineEvent>> groupEventsByCode(List<VaccineEvent> history) {
        if (history == null) {
            return Map.of();
        }

        Map<String, TreeMap<LocalDate, VaccineEvent>> dedup = new LinkedHashMap<>();
        for (VaccineEvent event : history) {
            dedup.computeIfAbsent(event.vaccineCode(), code -> new TreeMap<>())
                    .putIfAbsent(event.date(), event);
        }

        Map<String, List<VaccineEvent>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, TreeMap<LocalDate, VaccineEvent>> entry : dedup.entrySet()) {
            grouped.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return grouped;
    }

    private Map<String, List<VaccineRule>> createDogProtocol() {
        Map<String, List<VaccineRule>> protocol = new LinkedHashMap<>();
        protocol.put("KUDUZ", List.of(
                new VaccineRule(
                        new VaccineRuleCondition(90, null, 2.0, null),
                        VaccinePlan.oneShot(new BoosterPlan(365, 365)),
                        100)));
        protocol.put("KARMA", List.of(
                new VaccineRule(
                        new VaccineRuleCondition(60, 365, 2.0, null),
                        VaccinePlan.series(3, 21, 30, new BoosterPlan(365, 365)),
                        80)));
        return protocol;
    }

    private Map<String, List<VaccineRule>> createCatProtocol() {
        Map<String, List<VaccineRule>> protocol = new LinkedHashMap<>();
        protocol.put("KARMA-KEDI", List.of(
                new VaccineRule(
                        new VaccineRuleCondition(60, 365, 1.5, null),
                        VaccinePlan.series(3, 28, 45, new BoosterPlan(365, 365)),
                        70)));
        protocol.put("LOSEMİ", List.of(
                new VaccineRule(
                        new VaccineRuleCondition(120, null, 2.0, null),
                        VaccinePlan.oneShot(new BoosterPlan(365, 365)),
                        60)));
        return protocol;
    }
}
