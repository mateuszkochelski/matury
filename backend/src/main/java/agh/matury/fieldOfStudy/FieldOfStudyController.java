package agh.matury.fieldOfStudy;

import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("field_of_study")
@Tag(name = "Field of study", description = "Field of study getters APIs")
public class FieldOfStudyController {

  private final FieldOfStudyService fieldOfStudyService;

  public FieldOfStudyController(FieldOfStudyService fieldOfStudyService) {
    this.fieldOfStudyService = fieldOfStudyService;
  }

  @Operation(summary = "Search fields of study", description = """
      Filtry przekazywane jako query params:
      - name (LIKE, case-insensitive)
      - semestersFrom / semestersTo (duration)
      - department / university / city (LIKE)
      - degrees (powtarzany parametr, np. degrees=Bachelors&degrees=Engineering)
      - ids (powtarzany parametr lub lista oddzielona przecinkami, np. ids=1,2,3)
      Paginacja: page, size. Sortowanie: sort, direction=asc|desc.
      """)
  @ApiResponse(responseCode = "200", description = "Successfully retrieved fields of study", content = @Content(schema = @Schema(implementation = Page.class)))
  @GetMapping
  public ResponseEntity<Page<FieldOfStudyDTO>> getAllFieldsOdStudy(
      @ParameterObject FieldOfStudyFilter filter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sort,
      @RequestParam(defaultValue = "asc") String direction) {
    var sortDirection = Sort.Direction.fromString(direction);
    // Translate department-related sort fields to "department.name" for proper
    // sorting
    String actualSort = translateDepartmentSortField(sort);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, actualSort));
    return ResponseEntity.ok(fieldOfStudyService.search(filter, pageable));
  }

  @Operation(summary = "Get field of study.", description = "Returns field of study with provided id.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved field of study", content = @Content(schema = @Schema(implementation = FieldOfStudyDTO.class))),
      @ApiResponse(responseCode = "404", description = "Field of study not found", content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<FieldOfStudyDTO> getFieldOfStudyById(
      @Parameter(description = "Field of study id", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
    return ResponseEntity.ok(fieldOfStudyService.getFieldOfStudyById(id));
  }

  @Operation(summary = "Get all fields of study from university.", description = "Returns a paginated list of all fields of study from provided university id.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved fields of study", content = @Content(schema = @Schema(implementation = Page.class)))
  @GetMapping("/university/{id}")
  public ResponseEntity<Page<FieldOfStudyDTO>> getFieldsOfStudyByUniversityId(
      @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sort,
      @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String direction,
      @Parameter(description = "University id", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
    Sort.Direction sortDirection = Sort.Direction.fromString(direction);
    // Translate department-related sort fields to "department.name" for proper
    // sorting
    String actualSort = translateDepartmentSortField(sort);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, actualSort));
    return ResponseEntity.ok(fieldOfStudyService.getFieldsOfStudyByUniversityId(id, pageable));
  }

  @Operation(summary = "Get all fields of study from department.", description = "Returns a paginated list of all fields of study from provided department id.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved fields of study", content = @Content(schema = @Schema(implementation = Page.class)))
  @GetMapping("/department/{id}")
  public ResponseEntity<Page<FieldOfStudyDTO>> getFieldsOfStudyByDepartmentId(
      @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sort,
      @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String direction,
      @Parameter(description = "Department id", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
    Sort.Direction sortDirection = Sort.Direction.fromString(direction);
    // Translate department-related sort fields to "department.name" for proper
    // sorting
    String actualSort = translateDepartmentSortField(sort);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, actualSort));
    return ResponseEntity.ok(fieldOfStudyService.getFieldsOfStudyByDepartmentId(id, pageable));
  }

  private String translateDepartmentSortField(String sort) {
    if ("department".equals(sort) || "department_name".equals(sort)) {
      return "department.name";
    }
    return sort;
  }
}
