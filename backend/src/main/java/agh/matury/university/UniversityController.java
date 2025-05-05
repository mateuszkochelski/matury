package agh.matury.university;

import agh.matury.university.dto.UniversityDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/university")
@Tag(name = "University", description = "University getters APIs")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }


    @Operation(summary = "Get all universities.", description = "Returns a paginated list of all universities.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved universities",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<UniversityDTO>> getAllUniversities(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(universityService.getAllUniversities(pageable));
    }


    @Operation(summary = "Get university.", description = "Returns university with provided id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved university",
                    content = @Content(schema = @Schema(implementation = UniversityDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "University not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(
            @Parameter(description = "University id", required = true, in = ParameterIn.PATH)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(universityService.getUniversityById(id));
    }


    @Operation(summary = "Search for universities.", description = "Returns all universities matching name or acronym and city.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved universities",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping("/search")
    public ResponseEntity<Page<UniversityDTO>> searchUniversities(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Search term")
            @RequestParam(defaultValue = "") String searchTerm,
            @Parameter(description = "City")
            @RequestParam(defaultValue = "") String city
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(universityService.searchUniversities(searchTerm, city, pageable));
    }
}
