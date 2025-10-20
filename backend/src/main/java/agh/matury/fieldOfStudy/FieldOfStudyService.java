package agh.matury.fieldOfStudy;

import agh.matury.department.dto.DepartmentShortDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import agh.matury.university.dto.UniversityShortDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import agh.matury.fieldOfStudy.FieldOfStudyFilter;
import org.springframework.data.jpa.domain.Specification;



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
        if(fieldOfStudyRepository.findById(id).isPresent()) {
            return toDTO(fieldOfStudyRepository.findById(id).get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study not found");
    }

    public Page<FieldOfStudyDTO> getFieldsOfStudyByUniversityId(Long universityId, Pageable pageable) {
        // Filter out null departments when sorting by department-related fields
        String sortField = getSortFieldFromPageable(pageable);
        if (isDepartmentSortField(sortField)) {
            Specification<FieldOfStudy> spec = (root, query, cb) ->
                cb.and(
                    cb.equal(root.get("university").get("id"), universityId),
                    cb.isNotNull(root.get("department"))
                );
            return fieldOfStudyRepository.findAll(spec, pageable).map(this::toDTO);
        }
        return fieldOfStudyRepository.findByUniversityId(universityId, pageable).map(this::toDTO);
    }

    public Page<FieldOfStudyDTO> getFieldsOfStudyByDepartmentId(Long departmentId, Pageable pageable) {
        return fieldOfStudyRepository.findByDepartmentId(departmentId, pageable).map(this::toDTO);
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
                            university.getCity()
                    )
                    : null,
            department != null
                    ? new DepartmentShortDTO(
                            department.getId(),
                            department.getName()
                    )
                    : null
    );
}
    public Page<FieldOfStudyDTO> search(FieldOfStudyFilter filter, Pageable pageable) {
    Specification<FieldOfStudy> spec = FieldOfStudySpecifications.byFilter(filter);

    // Check if sorting by department-related field and filter out null departments
    String sortField = getSortFieldFromPageable(pageable);
    if (isDepartmentSortField(sortField)) {
        spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("department")));
    }

    return fieldOfStudyRepository
            .findAll(spec, pageable)
            .map(this::toDTO);
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
}
