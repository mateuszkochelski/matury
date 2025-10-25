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
}
