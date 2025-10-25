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

class RecruitmentCalculatorServicePoznanTest {

    private RecruitmentCalculatorService service;

    @BeforeEach
    void setUp() {
        RecruitmentConfigLoader loader = new RecruitmentConfigLoader(new ObjectMapper());
        service = new RecruitmentCalculatorService(loader);
    }

    @Test
    void engineeringXScenarioUsesVocationalResultWhenBestOption() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-poznanska",
                "engineering-x",
                List.of(
                        exam("polish_language", "BASIC", 80.0),
                        exam("english_language", "BASIC", 85.0),
                        exam("mathematics", "BASIC", 90.0),
                        exam("mathematics", "EXTENDED", 70.0),
                        exam("informatics", "EXTENDED", 75.0),
                        exam("vocational_exam", "VOCATIONAL_TECHNICIAN", 88.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(834.5, response.totalPoints(), 0.01);
        TermBreakdownDTO composite = findTerm(response, "pp-x-composite");
        assertNotNull(composite);
        assertEquals("VOCATIONAL", composite.level());
    }

    @Test
    void scienceXGScenarioCombinesBasicAndExtendedScores() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-poznanska",
                "science-xg",
                List.of(
                        exam("polish_language", "EXTENDED", 65.0),
                        exam("english_language", "EXTENDED", 70.0),
                        exam("mathematics", "EXTENDED", 82.0),
                        exam("geography", "BASIC", 74.0),
                        exam("geography", "EXTENDED", 66.0),
                        exam("vocational_exam", "VOCATIONAL_TECHNICIAN", 70.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(814.25, response.totalPoints(), 0.01);
        TermBreakdownDTO composite = findTerm(response, "pp2-x-composite");
        assertNotNull(composite);
        assertEquals("COMBINED_SUBJECT", composite.level());
    }

    @Test
    void architectureScenarioRequiresArtExamAndNormalizesLanguage() {
        CalculateRecruitmentPointsRequest request = new CalculateRecruitmentPointsRequest(
                "politechnika-poznanska",
                "architecture",
                List.of(
                        exam("polish_language", "EXTENDED", 62.0),
                        exam("english_language", "BILINGUAL", 35.0),
                        exam("mathematics", "EXTENDED", 78.0),
                        exam("art_exam", null, 420.0)
                )
        );

        RecruitmentCalculationResponse response = service.calculatePoints(request);

        assertEquals(851.5, response.totalPoints(), 0.01);
        TermBreakdownDTO artExam = findTerm(response, "pp-arch-art");
        assertNotNull(artExam);
        assertEquals(420.0, artExam.rawScore(), 0.01);
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
