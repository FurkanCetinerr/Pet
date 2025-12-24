package com.petlifecycle.petlifecycle_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlifecycle.petlifecycle_backend.model.PetType;
import com.petlifecycle.petlifecycle_backend.service.*;
import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PetVaccineSchedulerTest {

    private static final Path TEST_DATA_PATH = Path.of(
            "src/main/java/com/petlifecycle/petlifecycle_backend/service/PetVaccineSchudulerTestDatas.json");

    private final com.petlifecycle.petlifecycle_backend.service.strategy.DomesticPetStrategy domestic = new com.petlifecycle.petlifecycle_backend.service.strategy.DomesticPetStrategy();
    private final com.petlifecycle.petlifecycle_backend.service.strategy.ExoticPetStrategy exotic = new com.petlifecycle.petlifecycle_backend.service.strategy.ExoticPetStrategy();
    private final PetVaccineScheduler scheduler = new PetVaccineScheduler(domestic, exotic);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void schedule_shouldLoadJsonCases() throws IOException {
        VaccineTestData data = loadTestData();
        System.out.printf("Yüklendi: %d adet aşı tanımı%n", data.vaccines().size());
        Map<PetType, Map<String, List<VaccineRule>>> protocol = convertToProtocol(data);
        assertFalse(protocol.isEmpty(), "Protokol verisi boş olmamalı");
        protocol.forEach((species, vaccines) -> System.out.printf(
                "%s türü için %d aşı kuralı yüklendi%n", species, vaccines.size()));

        PetHealthData samplePet = new PetHealthData(
                PetType.KOPEK,
                LocalDate.now().minusDays(120),
                4.2,
                List.of(
                        new WeightRecord(LocalDate.now().minusDays(90), 2.8),
                        new WeightRecord(LocalDate.now().minusDays(30), 4.0)));

        List<VaccineEvent> history = List.of(
                new VaccineEvent("KUDUZ", LocalDate.now().minusDays(400), null));

        List<VaccineScheduleResult> results = scheduler.buildSchedule(samplePet, history);

        assertFalse(results.isEmpty(), "En az bir aşı planı dönmeli");
        results.forEach(result -> System.out.printf(
                "%s -> %s (%s)%n",
                result.vaccineCode(),
                result.status(),
                result.reason()));
        assertNotNull(results.get(0).status());
    }

    private VaccineTestData loadTestData() throws IOException {
        try (var reader = Files.newBufferedReader(TEST_DATA_PATH)) {
            return objectMapper.readValue(reader, VaccineTestData.class);
        }
    }

    private Map<PetType, Map<String, List<VaccineRule>>> convertToProtocol(VaccineTestData data) {
        Map<PetType, Map<String, List<VaccineRule>>> protocol = new EnumMap<>(PetType.class);

        for (VaccineCase vaccineCase : data.vaccines()) {
            PetType species = mapSpecies(vaccineCase.species());
            protocol.computeIfAbsent(species, s -> new java.util.LinkedHashMap<>());
            List<VaccineRule> rules = new ArrayList<>();

            for (RuleCase ruleCase : vaccineCase.rules()) {
                VaccineRuleCondition condition = new VaccineRuleCondition(
                        ruleCase.when().minAgeDays(),
                        ruleCase.when().maxAgeDays(),
                        ruleCase.when().minWeightKg(),
                        ruleCase.when().maxWeightKg());

                BoosterPlan booster = null;
                if (ruleCase.plan().firstBoosterAfterDays() != null || ruleCase.plan().repeatEveryDays() != null) {
                    booster = new BoosterPlan(
                            ruleCase.plan().firstBoosterAfterDays(),
                            ruleCase.plan().repeatEveryDays());
                }

                VaccinePlan plan;
                if ("ONE_SHOT".equalsIgnoreCase(ruleCase.plan().type())) {
                    plan = VaccinePlan.oneShot(booster);
                } else {
                    plan = VaccinePlan.series(
                            ruleCase.plan().doses(),
                            ruleCase.plan().intervalDays(),
                            ruleCase.plan().restartThresholdDays(),
                            booster);
                }

                rules.add(new VaccineRule(condition, plan, ruleCase.priority()));
            }

            protocol.get(species).put(vaccineCase.code(), rules);
        }

        return protocol;
    }

    private PetType mapSpecies(String species) {
        return switch (species.toUpperCase()) {
            case "DOG" ->
                PetType.KOPEK;
            case "CAT" ->
                PetType.KEDI;
            default ->
                throw new IllegalArgumentException("Desteklenmeyen tür: " + species);
        };
    }

    private record VaccineTestData(List<VaccineCase> vaccines) {
    }

    private record VaccineCase(String code, String species, List<RuleCase> rules) {
    }

    private record RuleCase(int priority, WhenCase when, PlanCase plan) {
    }

    private record WhenCase(Integer minAgeDays, Integer maxAgeDays, Double minWeightKg, Double maxWeightKg) {
    }

    private record PlanCase(
            String type,
            Integer doses,
            Integer intervalDays,
            Integer restartThresholdDays,
            Integer firstBoosterAfterDays,
            Integer repeatEveryDays) {
    }
}
