package agh.matury.recruitment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecruitmentCalculationResponse(
        String universityId,
        String fieldOfStudyId,
        double totalPoints,
        List<TermBreakdownDTO> breakdown,
        Double probability
) {
}
