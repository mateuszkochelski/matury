package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedEmploymentStatsDTO {
    private String wojewodztwo;
    private Map<String, AdvancedStats> yearlyStats; // np. "rok1" -> {srednia, mediana, ...}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdvancedStats {
        private double mean;
        private double median;
        private double stdDev;
        private double min;
        private double max;
    }
}

