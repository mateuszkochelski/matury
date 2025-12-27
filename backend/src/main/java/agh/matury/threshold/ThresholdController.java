package agh.matury.threshold;

import agh.matury.threshold.dto.ThresholdDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/threshold")
@Tag(name = "Threshold", description = "Threshold getters APIs")
public class ThresholdController {

    private final ThresholdService thresholdService;

    public ThresholdController(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    @Operation(summary = "Get all thresholds.", description = "Returns a paginated list of all thresholds.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved thresholds",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<ThresholdDTO>> getAllThresholds(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "year") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortOrder = Sort.by(sortDirection, sort);
        if (!"id".equals(sort)) {
            sortOrder = sortOrder.and(Sort.by(sortDirection, "id"));
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return ResponseEntity.ok(thresholdService.getAllThresholds(pageable));
    }


    @Operation(summary = "Get all thresholds from field of study.", description = "Returns a paginated list of all thresholds from provided field of study id.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved thresholds",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping("/fieldOfStudy/{id}")
    public ResponseEntity<Page<ThresholdDTO>> getThresholdsByFieldOfStudyId(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "year") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Field of study id", required = true, in = ParameterIn.PATH)
            @PathVariable Long id
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortOrder = Sort.by(sortDirection, sort);
        if (!"id".equals(sort)) {
            sortOrder = sortOrder.and(Sort.by(sortDirection, "id"));
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return ResponseEntity.ok(thresholdService.getThresholdsByFieldOfStudyId(id, pageable));
    }
}
