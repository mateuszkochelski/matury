package agh.matury.aptitudeTest.dto;

import agh.matury.aptitudeTest.HollandCategory;

import java.util.List;

public record CategoryResultDTO(
    HollandCategory category,
    String displayName,
    String description,
    int score,
    int maxScore,
    double percentage,
    List<String> suggestedFieldsOfStudy
) {
} 