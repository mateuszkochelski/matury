package agh.matury.recruitment;

import agh.matury.recruitment.dto.CalculateRecruitmentPointsRequest;
import agh.matury.recruitment.dto.RecruitmentCalculationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import agh.matury.recruitment.dto.SubjectEntry;
import agh.matury.recruitment.dto.SubjectWithLevelsDTO;
import java.io.IOException;

@RestController
@RequestMapping("/recruitment-calculator")
@Tag(name = "Recruitment Calculator", description = "Obliczanie punktacji rekrutacyjnej na podstawie wyników egzaminów")
public class RecruitmentCalculatorController {

    private final RecruitmentCalculatorService recruitmentCalculatorService;

    public RecruitmentCalculatorController(RecruitmentCalculatorService recruitmentCalculatorService) {
        this.recruitmentCalculatorService = recruitmentCalculatorService;
    }

    @Operation(
            summary = "Oblicz punkty rekrutacyjne",
            description = "Zwraca końcową liczbę punktów rekrutacyjnych dla wskazanego kierunku i uczelni",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Pomyślnie obliczono wynik",
                            content = @Content(schema = @Schema(implementation = RecruitmentCalculationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Niepoprawne dane wejściowe",
                            content = @Content
                    )
            }
    )
    @PostMapping("/calculate")
    public ResponseEntity<RecruitmentCalculationResponse> calculate(
            @RequestBody CalculateRecruitmentPointsRequest request
    ) {
        return ResponseEntity.ok(recruitmentCalculatorService.calculatePoints(request));
    }

        @GetMapping("/subjects")
        public ResponseEntity<List<SubjectWithLevelsDTO>> getSubjectsWithLevels() {
                ObjectMapper mapper = new ObjectMapper();
                try {
                        ClassPathResource subjectsRes = new ClassPathResource("recruitment/subjects.json");
                        ClassPathResource formulasRes = new ClassPathResource("recruitment/formulas.json");

                        String subjectsJson = new String(subjectsRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        String formulasJson = new String(formulasRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                        Map<String, List<String>> subjectsLevels = mapper.readValue(subjectsJson, new TypeReference<Map<String, List<String>>>() {});

                        Map<String, Object> formulas = mapper.readValue(formulasJson, new TypeReference<Map<String, Object>>() {});
                        Object subjectsNode = formulas.get("subjects");
                        List<SubjectEntry> subjectEntries = mapper.convertValue(subjectsNode, new TypeReference<List<SubjectEntry>>() {});

                        List<SubjectWithLevelsDTO> result = subjectEntries.stream()
                                        .map(entry -> new SubjectWithLevelsDTO(
                                                        entry.code(),
                                                        entry.label(),
                                                        subjectsLevels.getOrDefault(entry.code(), List.of())
                                        ))
                                        .collect(Collectors.toList());

                        return ResponseEntity.ok(result);
                } catch (IOException exception) {
                        return ResponseEntity.internalServerError().build();
                }
        }
}
