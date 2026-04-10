package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationStatsDto {

    // KPI principales
    private long totalRegistrations;
    private long activeRegistrations;

    // Fenêtre 7 jours
    private long newLast7Days;
    private long previous7Days;
    private double growthRateWeek;

    // Fenêtre 30 jours
    private long newLast30Days;
    private long previous30Days;
    private double growthRateMonth;

    // Dernière inscription
    private Instant lastRegistrationAt;

    // Tendance journalière sur 60 jours (couvre les deux fenêtres de comparaison)
    private List<DailyRegistrationCountDto> dailyTrend;

    // Répartitions (top 5 + "Autres")
    private Map<String, Long> countByCivility;
    private Map<String, Long> countByAgeRange;
    private Map<String, Long> countByCountry;
    private Map<String, Long> countByLanguage;
    private Map<String, Long> countByProfession;

    // Champs pour le filtre dynamique (fenêtre from/to)
    private long newInPeriod;
    private long previousPeriodCount;
    private double growthRatePeriod;
}
