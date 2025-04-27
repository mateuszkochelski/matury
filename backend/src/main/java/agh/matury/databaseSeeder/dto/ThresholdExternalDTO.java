package agh.matury.databaseSeeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThresholdExternalDTO(
        @JsonProperty("id") long id,
        @JsonProperty("year") int year,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("phase") int phase,
        @JsonProperty("admission_limit") Integer admissionLimit,
        @JsonProperty("admissions") Integer admissions,
        @JsonProperty("enrolled") Integer enrolled,
        @JsonProperty("threshold") Integer threshold,
        @JsonProperty("is_official_threshold") boolean isOfficialThreshold,
        @JsonProperty("url") String url,
        @JsonProperty("special_requirements") String specialRequirements,
        @JsonProperty("last_with_formula") boolean lastWithFormula,
        @JsonProperty("last_with_threshold") boolean lastWithThreshold,
        @JsonProperty("formula_uuid") String formulaUuid,
        @JsonProperty("was_started") boolean wasStarted,
        @JsonProperty("field_of_study") long fieldOfStudy
) {
}
