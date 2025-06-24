package agh.matury.aptitudeTest.dto;

import agh.matury.aptitudeTest.HollandCategory;

import java.util.List;
import java.util.Map;

public record AptitudeTestResultDTO(
    String sessionId,
    Map<HollandCategory, Integer> categoryScores,
    Map<HollandCategory, Double> categoryPercentages,
    List<HollandCategory> dominantCategories,
    String interpretation
) {
} 