package agh.matury.department.dto;

import agh.matury.university.dto.UniversityShortDTO;

public record DepartmentDTO(
        long id,
        String name,
        String url,
        UniversityShortDTO university
) {
}
