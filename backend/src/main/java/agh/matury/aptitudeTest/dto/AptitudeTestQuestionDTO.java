package agh.matury.aptitudeTest.dto;

import agh.matury.aptitudeTest.HollandCategory;

public record AptitudeTestQuestionDTO(
    Long id,
    String questionText,
    HollandCategory category,
    int orderNumber
) {
} 