package agh.matury.aptitudeTest;

import agh.matury.aptitudeTest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aptitude-test")
@Tag(name = "Aptitude Test", description = "Psychologiczny test predyspozycji zawodowych oparty na teorii Hollanda")
public class AptitudeTestController {

    private final AptitudeTestService aptitudeTestService;

    public AptitudeTestController(AptitudeTestService aptitudeTestService) {
        this.aptitudeTestService = aptitudeTestService;
    }

    @Operation(
        summary = "Pobierz wszystkie pytania testu predyspozycji",
        description = "Zwraca listę wszystkich pytań testu predyspozycji zawodowych w odpowiedniej kolejności"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Pomyślnie pobrano pytania testu",
        content = @Content(schema = @Schema(implementation = AptitudeTestQuestionDTO.class))
    )
    @GetMapping("/questions")
    public ResponseEntity<List<AptitudeTestQuestionDTO>> getTestQuestions() {
        return ResponseEntity.ok(aptitudeTestService.getAllQuestions());
    }

    @Operation(
        summary = "Prześlij odpowiedzi na test predyspozycji",
        description = "Przetwarza odpowiedzi użytkownika i zwraca wyniki testu predyspozycji zawodowych"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Test został pomyślnie przetworzony",
            content = @Content(schema = @Schema(implementation = AptitudeTestResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Nieprawidłowe dane wejściowe",
            content = @Content
        )
    })
    @PostMapping("/submit")
    public ResponseEntity<AptitudeTestResultDTO> submitTest(
            @RequestBody AptitudeTestResponseDTO responseDTO
    ) {
        return ResponseEntity.ok(aptitudeTestService.submitTest(responseDTO));
    }

    @Operation(
        summary = "Pobierz wynik testu predyspozycji",
        description = "Zwraca wynik testu predyspozycji dla podanego identyfikatora sesji"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pomyślnie pobrano wynik testu",
            content = @Content(schema = @Schema(implementation = AptitudeTestResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nie znaleziono wyniku testu dla podanej sesji",
            content = @Content
        )
    })
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<AptitudeTestResultDTO> getTestResult(
            @Parameter(description = "Identyfikator sesji testu", required = true, in = ParameterIn.PATH)
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(aptitudeTestService.getTestResult(sessionId));
    }

    @Operation(
        summary = "Pobierz szczegółowe wyniki testu predyspozycji",
        description = "Zwraca szczegółowe wyniki dla każdej kategorii testu predyspozycji zawodowych"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pomyślnie pobrano szczegółowe wyniki testu",
            content = @Content(schema = @Schema(implementation = CategoryResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nie znaleziono wyniku testu dla podanej sesji",
            content = @Content
        )
    })
    @GetMapping("/detailed-result/{sessionId}")
    public ResponseEntity<List<CategoryResultDTO>> getDetailedTestResult(
            @Parameter(description = "Identyfikator sesji testu", required = true, in = ParameterIn.PATH)
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(aptitudeTestService.getDetailedResults(sessionId));
    }

    @Operation(
        summary = "Pobierz informacje o kategoriach testu Hollanda",
        description = "Zwraca informacje o wszystkich kategoriach osobowości zawodowej według teorii Hollanda"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Pomyślnie pobrano informacje o kategoriach",
        content = @Content(schema = @Schema(implementation = HollandCategory.class))
    )
    @GetMapping("/categories")
    public ResponseEntity<HollandCategory[]> getHollandCategories() {
        return ResponseEntity.ok(HollandCategory.values());
    }
} 