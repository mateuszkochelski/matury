package agh.matury.recruitment.dto;

public record AcceptanceProbabilityDTO(
        Double probability,
        Double lowerBound,
        Double upperBound
) {
}
