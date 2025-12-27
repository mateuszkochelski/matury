package agh.matury.fieldOfStudy;

import agh.matury.department.dto.DepartmentShortDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyExtendedDTO;
import agh.matury.fieldOfStudy.dto.GraduateDataDTO;
import agh.matury.university.dto.UniversityShortDTO;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.jpa.domain.Specification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FieldOfStudyService {

  private final FieldOfStudyRepository fieldOfStudyRepository;

  public FieldOfStudyService(FieldOfStudyRepository fieldOfStudyRepository) {
    this.fieldOfStudyRepository = fieldOfStudyRepository;
  }

  public Page<FieldOfStudyDTO> getAllFieldsOfStudy(Pageable pageable) {
    return fieldOfStudyRepository.findAll(pageable).map(this::toDTO);
  }

  public FieldOfStudyDTO getFieldOfStudyById(Long id) {
    if (fieldOfStudyRepository.findById(id).isPresent()) {
      return toDTO(fieldOfStudyRepository.findById(id).get());
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study not found");
  }

  public GraduateDataDTO getFieldOfStudyGraduateDataById(Long id) {
    FieldOfStudy fieldOfStudy = fieldOfStudyRepository.findById(id)
            .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study not found")
            );

    FieldOfStudyDTO dto = toDTO(fieldOfStudy);

      return loadGraduateDataFromCsv(dto);
  }

  public Page<FieldOfStudyExtendedDTO> getFieldsOfStudyByUniversityId(Long universityId, Pageable pageable) {
    // Filter out null departments when sorting by department-related fields
    String sortField = getSortFieldFromPageable(pageable);
    if (isDepartmentSortField(sortField)) {
      Specification<FieldOfStudy> spec = (root, query, cb) -> cb.and(
          cb.equal(root.get("university").get("id"), universityId),
          cb.isNotNull(root.get("department")));
      return fieldOfStudyRepository.findAll(spec, pageable).map(this::toDTO).map(this::toExtendedDTO);
    }
    return fieldOfStudyRepository.findByUniversityId(universityId, pageable).map(this::toDTO).map(this::toExtendedDTO);
  }

  public Page<FieldOfStudyExtendedDTO> getFieldsOfStudyByDepartmentId(Long departmentId, Pageable pageable) {
    return fieldOfStudyRepository.findByDepartmentId(departmentId, pageable).map(this::toDTO).map(this::toExtendedDTO);
  }

  public Page<FieldOfStudyExtendedDTO> search(FieldOfStudyFilter filter, Pageable pageable) {
    if (filter.getDegrees() != null && !filter.getDegrees().isEmpty()) {
      filter.setDegrees(
          filter.getDegrees().stream()
              .filter(Objects::nonNull)
              .map(FieldOfStudyMapper::toPolish)
              .collect(Collectors.toSet()) // ✅ ZAMIANA NA SET
      );
    }
    Specification<FieldOfStudy> spec = FieldOfStudySpecifications.byFilter(filter);

    // Check if sorting by department-related field and filter out null departments
    String sortField = getSortFieldFromPageable(pageable);
    if (isDepartmentSortField(sortField)) {
      spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("department")));
    }

    return fieldOfStudyRepository
        .findAll(spec, pageable)
        .map(this::toDTO).map(this::toExtendedDTO);
  }

  private String getSortFieldFromPageable(Pageable pageable) {
    if (pageable.getSort().isSorted()) {
      return pageable.getSort().iterator().next().getProperty();
    }
    return null;
  }

  private boolean isDepartmentSortField(String sortField) {
    return sortField != null && (sortField.equals("department") || sortField.startsWith("department."));
  }

  private GraduateDataDTO loadGraduateDataFromCsv(FieldOfStudyDTO dto) {

    ClassPathResource resource =
            new ClassPathResource("graduate/graduate-data.csv");

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

      String line;
      boolean isHeader = true;

      while ((line = reader.readLine()) != null) {

        if (isHeader) {
          isHeader = false;
          continue;
        }

        String[] columns = line.split(";");

        String fieldId = columns[0].trim();

        if (fieldId.equals(Long.toString(dto.id()))) {
            return new GraduateDataDTO(
                  dto.id(),
                  parseFloatOrZero(columns[1]),
                  parseFloatOrZero(columns[2]),
                  parseFloatOrZero(columns[3]),
                  parseFloatOrZero(columns[4]),
                  parseFloatOrZero(columns[5]),
                  parseFloatOrZero(columns[6]),
                  parseFloatOrZero(columns[7])
          );
        }
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study data not found");
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading graduate data");
    }
  }

  private static float parseFloatOrZero(String value) {
    if (value == null || value.trim().isEmpty()) {
      return 0.0f;
    }
    try {
      return Float.parseFloat(value.trim().replace(',', '.'));
    } catch (NumberFormatException e) {
      return 0.0f;
    }
  }

  private FieldOfStudyDTO toDTO(FieldOfStudy fieldOfStudy) {
    var university = fieldOfStudy.getUniversity();
    var department = fieldOfStudy.getDepartment();

    return new FieldOfStudyDTO(
            fieldOfStudy.getId(),
            fieldOfStudy.getName(),
            fieldOfStudy.getLevel(),
            fieldOfStudy.getDuration(),
            fieldOfStudy.getLanguage(),
            university != null
                    ? new UniversityShortDTO(
                    university.getId(),
                    university.getName(),
                    university.getAcronym(),
                    university.getCity())
                    : null,
            department != null
                    ? new DepartmentShortDTO(
                    department.getId(),
                    department.getName())
                    : null);
  }

  private FieldOfStudyExtendedDTO toExtendedDTO(FieldOfStudyDTO fieldOfStudy) {
    var university = fieldOfStudy.university();
    var department = fieldOfStudy.department();

    GraduateDataDTO graduateDataDTO = new GraduateDataDTO(
            fieldOfStudy.id(),
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f
    );
    try {
      graduateDataDTO = loadGraduateDataFromCsv(fieldOfStudy);
    }
    catch (ResponseStatusException ignored) {

    }
    return new FieldOfStudyExtendedDTO(
            fieldOfStudy.id(),
            fieldOfStudy.name(),
            fieldOfStudy.level(),
            fieldOfStudy.duration(),
            fieldOfStudy.language(),
            university != null
                    ? new UniversityShortDTO(
                    university.id(),
                    university.name(),
                    university.acronym(),
                    university.city())
                    : null,
            department != null
                    ? new DepartmentShortDTO(
                    department.id(),
                    department.name())
                    : null,
            graduateDataDTO.passRate(),
            graduateDataDTO.avgIncome()
    );
  }
}
