package agh.matury.recruitment.dto;

import java.util.List;

public record RecruitmentCalculationResponse(
        String universityId,
        String fieldOfStudyId,
        double totalPoints,
        List<TermBreakdownDTO> breakdown
) {
}
