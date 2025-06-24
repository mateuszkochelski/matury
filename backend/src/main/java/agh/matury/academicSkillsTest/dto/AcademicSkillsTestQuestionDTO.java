package agh.matury.academicSkillsTest.dto;

import agh.matury.academicSkillsTest.AcademicSkillCategory;

public record AcademicSkillsTestQuestionDTO(
    Long id,
    String questionText,
    AcademicSkillCategory category,
    int orderNumber
) {
} 