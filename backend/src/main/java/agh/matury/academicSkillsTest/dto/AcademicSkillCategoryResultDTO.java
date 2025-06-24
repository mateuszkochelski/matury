package agh.matury.academicSkillsTest.dto;

import agh.matury.academicSkillsTest.AcademicSkillCategory;

import java.util.List;

public record AcademicSkillCategoryResultDTO(
    AcademicSkillCategory category,
    String displayName,
    String description,
    int score,
    int maxScore,
    double percentage,
    List<String> suggestedFieldsOfStudy
) {
} 