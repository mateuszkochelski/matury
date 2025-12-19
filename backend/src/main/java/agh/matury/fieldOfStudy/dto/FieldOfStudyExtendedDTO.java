package agh.matury.fieldOfStudy.dto;

import agh.matury.department.dto.DepartmentShortDTO;
import agh.matury.university.dto.UniversityShortDTO;

public record FieldOfStudyExtendedDTO(
        long id,
        String name,
        String level,
        Integer duration,
        String language,
        UniversityShortDTO university,
        DepartmentShortDTO department,
        Float passRate,
        Float avgIncome
) {
}