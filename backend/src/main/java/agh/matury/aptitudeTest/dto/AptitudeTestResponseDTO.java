package agh.matury.aptitudeTest.dto;

import java.util.List;

public record AptitudeTestResponseDTO(
    String sessionId,
    List<Integer> answers
) {
} 