package agh.matury.recommendation;

import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import agh.matury.recommendation.dto.RecommendationRequestDTO;
import agh.matury.recommendation.dto.RecommendationsHistoryRequestDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommendation")
@Tag(name = "Recommendation", description = "Recomendation getters APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/field/{id}")
    public ResponseEntity<List<FieldOfStudyDTO>> getFieldsOfStudySimilarity(
            @Parameter(description = "Field of study id", required = true, in = ParameterIn.PATH) @PathVariable Long id,
            @RequestBody RecommendationRequestDTO request
            ) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForField(id, request.getK()));
    }

    @PostMapping("/history")
    public List<FieldOfStudyDTO> getRecommendations(
            @RequestBody RecommendationsHistoryRequestDTO request) {

        return recommendationService.getRecommendationsForFields(
                new ArrayList<>(request.getFieldIds()),
                request.getK()
        );
    }
}
