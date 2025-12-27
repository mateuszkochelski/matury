package agh.matury.fieldOfStudy.dto;

public record GraduateDataDTO (
    Long fieldOfStudyId,
    float avgIncome,
    float incomeAfterYear1,
    float incomeAfterYear2,
    float incomeAfterYear3,
    float incomeAfterYear4,
    float incomeAfterYear5,
    float passRate
    ){}