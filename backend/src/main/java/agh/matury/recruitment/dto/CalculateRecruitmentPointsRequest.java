package agh.matury.recruitment.dto;

import java.util.List;

public record CalculateRecruitmentPointsRequest(
        String universityId,
        String fieldOfStudyId,
        List<ExamResultDTO> examResults
) {
}
