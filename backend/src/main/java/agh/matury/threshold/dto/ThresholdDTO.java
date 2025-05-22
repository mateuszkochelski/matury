package agh.matury.threshold.dto;

import agh.matury.fieldOfStudy.dto.FieldOfStudyShortDTO;

public record ThresholdDTO(
        long id,
        Integer year,
        Integer phase,
        Integer admissionLimit,
        Integer admissions,
        Integer threshold,
        String specialRequirements,
        FieldOfStudyShortDTO fieldOfStudy
) {
}
