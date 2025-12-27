package agh.matury.recruitment;

import agh.matury.recruitment.config.RecruitmentConfigLoader;
import agh.matury.recruitment.dto.CalculateRecruitmentPointsRequest;
import agh.matury.recruitment.dto.ExamResultDTO;
import agh.matury.recruitment.dto.RecruitmentCalculationResponse;
import agh.matury.recruitment.dto.TermBreakdownDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecruitmentCalculatorServiceBialystokTest {

    private RecruitmentCalculatorService service;

    @BeforeEach
    void setUp() {
        RecruitmentConfigLoader loader = new RecruitmentConfigLoader(new ObjectMapper());
        service = new RecruitmentCalculatorService(loader);
    }

    @Test
    void defaultScenarioUsesBestExtendedSubject() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "default",
                List.of(
                        exam("mathematics", "BASIC", 80.0),
                        exam("mathematics", "EXTENDED", 70.0),
                        exam("english_language", "BASIC", 85.0),
                        exam("german_language", "EXTENDED", 60.0),
                        exam("physics", "EXTENDED", 75.0),
                        exam("geography", "EXTENDED", 65.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(325.0, response.totalPoints(), 0.01);
        TermBreakdownDTO additional = findTerm(response, "pb-def-additional");
        assertNotNull(additional);
        assertEquals("physics", additional.subjectCode());
    }

    @Test
    void biotechnologyScenarioAggregatesTwoAdditionalSubjects() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "biotechnology",
                List.of(
                        exam("mathematics", "BASIC", 82.0),
                        exam("mathematics", "EXTENDED", 70.0),
                        exam("english_language", "BASIC", 85.0),
                        exam("german_language", "EXTENDED", 60.0),
                        exam("physics", "EXTENDED", 78.0),
                        exam("geography", "EXTENDED", 64.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(443.25, response.totalPoints(), 0.01);
        TermBreakdownDTO aggregated = findTerm(response, "pb-bio-additional");
        assertNotNull(aggregated);
        assertEquals("physics+geography", aggregated.subjectCode());
        assertEquals(142.0, aggregated.rawScore(), 0.01);
        assertEquals(1.75, aggregated.coefficient(), 0.0001);
    }

    @Test
    void environmentalEngineeringMatchesNumericAlias() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "2498",
                List.of(
                        exam("mathematics", "BASIC", 82.0),
                        exam("mathematics", "EXTENDED", 70.0),
                        exam("english_language", "BASIC", 85.0),
                        exam("german_language", "EXTENDED", 60.0),
                        exam("physics", "EXTENDED", 78.0),
                        exam("geography", "EXTENDED", 64.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(443.25, response.totalPoints(), 0.01);
        TermBreakdownDTO aggregated = findTerm(response, "pb-env-additional");
        assertNotNull(aggregated);
        assertEquals(142.0, aggregated.rawScore(), 0.01);
    }

    @Test
    void wrScenarioHonoursWeightedSubject() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "wr-technical",
                List.of(
                        exam("mathematics", "BASIC", 78.0),
                        exam("mathematics", "EXTENDED", 82.0),
                        exam("english_language", "BASIC", 70.0),
                        exam("english_language", "EXTENDED", 68.0),
                        exam("physics", "EXTENDED", 80.0),
                        exam("chemistry", "EXTENDED", 90.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(350.0, response.totalPoints(), 0.01);
        TermBreakdownDTO weighted = findTerm(response, "pb-wr-additional");
        assertNotNull(weighted);
        assertEquals("physics", weighted.subjectCode());
        assertEquals(1.75, weighted.coefficient(), 0.0001);
    }

    @Test
    void drawingScenarioRequiresArtExam() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "drawing-required",
                List.of(
                        exam("mathematics", "BASIC", 76.0),
                        exam("mathematics", "EXTENDED", 65.0),
                        exam("english_language", "BASIC", 72.0),
                        exam("english_language", "EXTENDED", 58.0),
                        exam("chemistry", "EXTENDED", 70.0),
                        exam("art_exam", null, 150.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(903.25, response.totalPoints(), 0.01);
        TermBreakdownDTO art = findTerm(response, "pb-dr-art");
        assertNotNull(art);
        assertEquals(150.0, art.rawScore(), 0.0001);
        assertEquals(4.0, art.coefficient(), 0.0001);
    }

    @Test
    void mathScenarioAppliesHigherCoefficient() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-bialostocka",
                "math-applied",
                List.of(
                        exam("mathematics", "BASIC", 85.0),
                        exam("mathematics", "EXTENDED", 90.0),
                        exam("english_language", "BASIC", 80.0),
                        exam("english_language", "EXTENDED", 75.0),
                        exam("informatics", "EXTENDED", 88.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(386.75, response.totalPoints(), 0.01);
        TermBreakdownDTO mr = findTerm(response, "pb-math-mr");
        assertNotNull(mr);
        assertEquals(2.0, mr.coefficient(), 0.0001);
    }

    private static ExamResultDTO exam(String subject, String level, Double score) {
        return new ExamResultDTO(subject, level, score);
    }

    private static TermBreakdownDTO findTerm(RecruitmentCalculationResponse response, String termId) {
        return response.breakdown().stream()
                .filter(term -> termId.equals(term.termId()))
                .findFirst()
                .orElse(null);
    }
}
