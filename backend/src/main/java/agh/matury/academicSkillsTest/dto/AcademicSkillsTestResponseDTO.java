package agh.matury.academicSkillsTest.dto;

import java.util.List;

public record AcademicSkillsTestResponseDTO(
    String sessionId,
    List<Integer> answers
) {
} 