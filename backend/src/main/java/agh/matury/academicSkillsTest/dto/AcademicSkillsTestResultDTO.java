package agh.matury.academicSkillsTest.dto;

import agh.matury.academicSkillsTest.AcademicSkillCategory;

import java.util.List;
import java.util.Map;

public record AcademicSkillsTestResultDTO(
    String sessionId,
    Map<AcademicSkillCategory, Integer> categoryScores,
    Map<AcademicSkillCategory, Double> categoryPercentages,
    List<AcademicSkillCategory> dominantCategories,
    String interpretation
) {
} 