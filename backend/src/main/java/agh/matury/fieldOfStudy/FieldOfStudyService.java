package agh.matury.fieldOfStudy;

import agh.matury.department.dto.DepartmentShortDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyExtendedDTO;
import agh.matury.fieldOfStudy.dto.GraduateDataDTO;
import agh.matury.university.dto.UniversityShortDTO;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.jpa.domain.Specification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FieldOfStudyService {

  private final FieldOfStudyRepository fieldOfStudyRepository;
  private Map<Long, GraduateDataDTO> graduateDataMap;

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
    if (isGraduateSortField(sortField)) {
      List<FieldOfStudyExtendedDTO> sorted = fieldOfStudyRepository
          .findByUniversityId(universityId, Pageable.unpaged())
          .getContent()
          .stream()
          .map(this::toDTO)
          .map(this::toExtendedDTO)
          .sorted(buildGraduateSortComparator(pageable))
          .collect(Collectors.toList());
      return paginate(sorted, pageable);
    }
    if (isDepartmentSortField(sortField)) {
      Specification<FieldOfStudy> spec = (root, query, cb) -> cb.and(
          cb.equal(root.get("university").get("id"), universityId),
          cb.isNotNull(root.get("department")));
      return fieldOfStudyRepository.findAll(spec, pageable).map(this::toDTO).map(this::toExtendedDTO);
    }
    return fieldOfStudyRepository.findByUniversityId(universityId, pageable).map(this::toDTO).map(this::toExtendedDTO);
  }

  public Page<FieldOfStudyExtendedDTO> getFieldsOfStudyByDepartmentId(Long departmentId, Pageable pageable) {
    String sortField = getSortFieldFromPageable(pageable);
    if (isGraduateSortField(sortField)) {
      List<FieldOfStudyExtendedDTO> sorted = fieldOfStudyRepository
          .findByDepartmentId(departmentId, Pageable.unpaged())
          .getContent()
          .stream()
          .map(this::toDTO)
          .map(this::toExtendedDTO)
          .sorted(buildGraduateSortComparator(pageable))
          .collect(Collectors.toList());
      return paginate(sorted, pageable);
    }
    return fieldOfStudyRepository.findByDepartmentId(departmentId, pageable).map(this::toDTO).map(this::toExtendedDTO);
  }

  public Page<FieldOfStudyExtendedDTO> getFieldsOfStudyByDepartmentIds(List<Long> departmentIds, Pageable pageable) {
    List<Long> distinctIds = departmentIds.stream().distinct().collect(Collectors.toList());
    String sortField = getSortFieldFromPageable(pageable);
    if (isGraduateSortField(sortField)) {
      List<FieldOfStudyExtendedDTO> sorted = fieldOfStudyRepository
          .findByDepartmentIdIn(distinctIds, Pageable.unpaged())
          .getContent()
          .stream()
          .map(this::toDTO)
          .map(this::toExtendedDTO)
          .sorted(buildGraduateSortComparator(pageable))
          .collect(Collectors.toList());
      return paginate(sorted, pageable);
    }
    return fieldOfStudyRepository
        .findByDepartmentIdIn(distinctIds, pageable)
        .map(this::toDTO)
        .map(this::toExtendedDTO);
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

    boolean graduateSort = isGraduateSortField(sortField);
    if (!hasGraduateFilters(filter) && !graduateSort) {
      return fieldOfStudyRepository
          .findAll(spec, pageable)
          .map(this::toDTO)
          .map(this::toExtendedDTO);
    }

    List<FieldOfStudyExtendedDTO> results = (graduateSort
        ? fieldOfStudyRepository.findAll(spec)
        : fieldOfStudyRepository.findAll(spec, pageable.getSort()))
        .stream()
        .map(this::toDTO)
        .map(this::toExtendedDTO)
        .collect(Collectors.toList());

    if (hasGraduateFilters(filter)) {
      results = results.stream()
          .filter(dto -> matchesGraduateFilters(dto, filter))
          .collect(Collectors.toList());
    }

    if (graduateSort) {
      results.sort(buildGraduateSortComparator(pageable));
    }

    return paginate(results, pageable);
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

  private boolean isGraduateSortField(String sortField) {
    return sortField != null && ("passRate".equals(sortField) || "avgIncome".equals(sortField));
  }

  private GraduateDataDTO loadGraduateDataFromCsv(FieldOfStudyDTO dto) {
    GraduateDataDTO data = getGraduateDataMap().get(dto.id());
    if (data != null) {
      return data;
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study data not found");
  }

  private synchronized Map<Long, GraduateDataDTO> getGraduateDataMap() {
    if (graduateDataMap == null) {
      graduateDataMap = loadGraduateDataMapFromCsv();
    }
    return graduateDataMap;
  }

  private Map<Long, GraduateDataDTO> loadGraduateDataMapFromCsv() {

    ClassPathResource resource =
            new ClassPathResource("graduate/graduate-data.csv");

    Map<Long, GraduateDataDTO> data = new HashMap<>();

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

        if (columns.length < 8) {
          continue;
        }

        String fieldId = columns[0].trim();
        if (fieldId.isEmpty()) {
          continue;
        }

        try {
          long parsedId = Long.parseLong(fieldId);
          data.put(
              parsedId,
              new GraduateDataDTO(
                  parsedId,
                  parseFloatOrZero(columns[1]),
                  parseFloatOrZero(columns[2]),
                  parseFloatOrZero(columns[3]),
                  parseFloatOrZero(columns[4]),
                  parseFloatOrZero(columns[5]),
                  parseFloatOrZero(columns[6]),
                  parseFloatOrZero(columns[7])
              )
          );
        } catch (NumberFormatException ignored) {
        }
      }
      return data;
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading graduate data");
    }
  }

  private boolean hasGraduateFilters(FieldOfStudyFilter filter) {
    return filter.getPassRateFrom() != null
        || filter.getPassRateTo() != null
        || filter.getAvgSalaryFrom() != null
        || filter.getAvgSalaryTo() != null;
  }

  private boolean matchesGraduateFilters(FieldOfStudyExtendedDTO dto, FieldOfStudyFilter filter) {
    float passRate = dto.passRate() != null ? dto.passRate() : 0.0f;
    float avgIncome = dto.avgIncome() != null ? dto.avgIncome() : 0.0f;

    if (filter.getPassRateFrom() != null
        && filter.getPassRateFrom().compareTo(java.math.BigDecimal.valueOf(passRate)) > 0) {
      return false;
    }
    if (filter.getPassRateTo() != null
        && filter.getPassRateTo().compareTo(java.math.BigDecimal.valueOf(passRate)) < 0) {
      return false;
    }
    if (filter.getAvgSalaryFrom() != null
        && filter.getAvgSalaryFrom().compareTo(java.math.BigDecimal.valueOf(avgIncome)) > 0) {
      return false;
    }
    if (filter.getAvgSalaryTo() != null
        && filter.getAvgSalaryTo().compareTo(java.math.BigDecimal.valueOf(avgIncome)) < 0) {
      return false;
    }

    return true;
  }

  private Comparator<FieldOfStudyExtendedDTO> buildGraduateSortComparator(Pageable pageable) {
    String sortField = getSortFieldFromPageable(pageable);
    Sort.Direction direction = Sort.Direction.ASC;
    if (sortField != null) {
      Sort.Order order = pageable.getSort().getOrderFor(sortField);
      if (order != null) {
        direction = order.getDirection();
      }
    }

    Comparator<FieldOfStudyExtendedDTO> comparator;
    if ("passRate".equals(sortField)) {
      comparator = Comparator.comparing(dto -> dto.passRate() != null ? dto.passRate() : 0.0f);
    } else if ("avgIncome".equals(sortField)) {
      comparator = Comparator.comparing(dto -> dto.avgIncome() != null ? dto.avgIncome() : 0.0f);
    } else {
      comparator = Comparator.comparing(FieldOfStudyExtendedDTO::id);
    }

    if (direction.isDescending()) {
      comparator = comparator.reversed();
    }

    Comparator<FieldOfStudyExtendedDTO> idComparator = Comparator.comparing(FieldOfStudyExtendedDTO::id);
    if (direction.isDescending()) {
      idComparator = idComparator.reversed();
    }

    return comparator.thenComparing(idComparator);
  }

  private Page<FieldOfStudyExtendedDTO> paginate(List<FieldOfStudyExtendedDTO> items, Pageable pageable) {
    int totalElements = items.size();
    int start = Math.toIntExact(pageable.getOffset());
    if (start >= totalElements) {
      return new PageImpl<>(Collections.emptyList(), pageable, totalElements);
    }
    int end = Math.min(start + pageable.getPageSize(), totalElements);
    return new PageImpl<>(items.subList(start, end), pageable, totalElements);
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
