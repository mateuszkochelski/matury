package agh.matury.recruitment.dto;

public record TermBreakdownDTO(
        String termId,
        String description,
        String subjectCode,
        String level,
        double rawScore,
        double coefficient,
        double pointsAwarded
) {
}
