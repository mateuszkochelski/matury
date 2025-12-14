package agh.matury.recruitment.dto;

import java.util.List;

public record SubjectWithLevelsDTO(
        String code,
        String label,
        List<String> levels
) {
}
