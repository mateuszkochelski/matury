package agh.matury.recruitment.dto;

public record ExamResultDTO(
        String subjectCode,
        String level,
        Double score
) {
    public String normalizedSubjectCode() {
        return subjectCode == null ? null : subjectCode.trim().toLowerCase();
    }

    public String normalizedLevel() {
        return level == null ? null : level.trim().toUpperCase();
    }
}
