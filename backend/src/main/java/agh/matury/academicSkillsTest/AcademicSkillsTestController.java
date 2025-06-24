package agh.matury.academicSkillsTest;

import agh.matury.academicSkillsTest.dto.*;
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
@RequestMapping("/academic-skills-test")
@Tag(name = "Academic Skills Test", description = "Test uzdolnień kierunkowych (kompetencje akademickie) oparty na teorii inteligencji wielorakiej Gardnera")
public class AcademicSkillsTestController {

    private final AcademicSkillsTestService academicSkillsTestService;

    public AcademicSkillsTestController(AcademicSkillsTestService academicSkillsTestService) {
        this.academicSkillsTestService = academicSkillsTestService;
    }

    @Operation(
        summary = "Pobierz wszystkie pytania testu uzdolnień kierunkowych",
        description = "Zwraca listę wszystkich pytań testu uzdolnień kierunkowych w odpowiedniej kolejności"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Pomyślnie pobrano pytania testu",
        content = @Content(schema = @Schema(implementation = AcademicSkillsTestQuestionDTO.class))
    )
    @GetMapping("/questions")
    public ResponseEntity<List<AcademicSkillsTestQuestionDTO>> getTestQuestions() {
        return ResponseEntity.ok(academicSkillsTestService.getAllQuestions());
    }

    @Operation(
        summary = "Prześlij odpowiedzi na test uzdolnień kierunkowych",
        description = "Przetwarza odpowiedzi użytkownika i zwraca wyniki testu uzdolnień kierunkowych"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Test został pomyślnie przetworzony",
            content = @Content(schema = @Schema(implementation = AcademicSkillsTestResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Nieprawidłowe dane wejściowe",
            content = @Content
        )
    })
    @PostMapping("/submit")
    public ResponseEntity<AcademicSkillsTestResultDTO> submitTest(
            @RequestBody AcademicSkillsTestResponseDTO responseDTO
    ) {
        return ResponseEntity.ok(academicSkillsTestService.submitTest(responseDTO));
    }

    @Operation(
        summary = "Pobierz wynik testu uzdolnień kierunkowych",
        description = "Zwraca wynik testu uzdolnień kierunkowych dla podanego identyfikatora sesji"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pomyślnie pobrano wynik testu",
            content = @Content(schema = @Schema(implementation = AcademicSkillsTestResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nie znaleziono wyniku testu dla podanej sesji",
            content = @Content
        )
    })
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<AcademicSkillsTestResultDTO> getTestResult(
            @Parameter(description = "Identyfikator sesji testu", required = true, in = ParameterIn.PATH)
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(academicSkillsTestService.getTestResult(sessionId));
    }

    @Operation(
        summary = "Pobierz szczegółowe wyniki testu uzdolnień kierunkowych",
        description = "Zwraca szczegółowe wyniki dla każdej kategorii testu uzdolnień kierunkowych"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pomyślnie pobrano szczegółowe wyniki testu",
            content = @Content(schema = @Schema(implementation = AcademicSkillCategoryResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nie znaleziono wyniku testu dla podanej sesji",
            content = @Content
        )
    })
    @GetMapping("/detailed-result/{sessionId}")
    public ResponseEntity<List<AcademicSkillCategoryResultDTO>> getDetailedTestResult(
            @Parameter(description = "Identyfikator sesji testu", required = true, in = ParameterIn.PATH)
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(academicSkillsTestService.getDetailedResults(sessionId));
    }

    @Operation(
        summary = "Pobierz informacje o kategoriach uzdolnień kierunkowych",
        description = "Zwraca informacje o wszystkich kategoriach uzdolnień kierunkowych według teorii Gardnera"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Pomyślnie pobrano informacje o kategoriach",
        content = @Content(schema = @Schema(implementation = AcademicSkillCategory.class))
    )
    @GetMapping("/categories")
    public ResponseEntity<AcademicSkillCategory[]> getAcademicSkillCategories() {
        return ResponseEntity.ok(AcademicSkillCategory.values());
    }
} 