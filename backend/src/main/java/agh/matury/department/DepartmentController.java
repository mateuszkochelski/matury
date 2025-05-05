package agh.matury.department;

import agh.matury.department.dto.DepartmentDTO;
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
@RequestMapping("/department")
@Tag(name = "Department", description = "Department getters APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Get all departments.", description = "Returns a paginated list of all departments.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved departments",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(
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
        return ResponseEntity.ok(departmentService.getAllDepartments(pageable));
    }


    @Operation(summary = "Get department.", description = "Returns department with provided id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved department",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(
            @Parameter(description = "Department id", required = true, in = ParameterIn.PATH)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }


    @Operation(summary = "Get all departments from university.", description = "Returns a paginated list of all departments from provided university id.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved departments",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping("/university/{id}")
    public ResponseEntity<Page<DepartmentDTO>> getDepartmentsByUniversityId(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "University id", required = true, in = ParameterIn.PATH)
            @PathVariable Long id
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(departmentService.getDepartmentsByUniversityId(id, pageable));
    }


    @Operation(summary = "Search for departments.", description = "Returns all departments matching name.")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved departments",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping("/search")
    public ResponseEntity<Page<DepartmentDTO>> searchDepartments(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Name")
            @RequestParam(defaultValue = "") String name
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(departmentService.searchDepartments(name, pageable));
    }
}
