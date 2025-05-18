package agh.matury.fieldOfStudy;

import agh.matury.department.dto.DepartmentShortDTO;
import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import agh.matury.university.dto.UniversityShortDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        return fieldOfStudyRepository.findByUniversityId(universityId, pageable).map(this::toDTO);
    }

    public Page<FieldOfStudyDTO> getFieldsOfStudyByDepartmentId(Long departmentId, Pageable pageable) {
        return fieldOfStudyRepository.findByDepartmentId(departmentId, pageable).map(this::toDTO);
    }

    private FieldOfStudyDTO toDTO(FieldOfStudy fieldOfStudy) {
        return new FieldOfStudyDTO(
                fieldOfStudy.getId(),
                fieldOfStudy.getName(),
                fieldOfStudy.getLevel(),
                fieldOfStudy.getDuration(),
                fieldOfStudy.getLanguage(),
                new UniversityShortDTO(
                        fieldOfStudy.getUniversity().getId(),
                        fieldOfStudy.getUniversity().getName(),
                        fieldOfStudy.getUniversity().getAcronym(),
                        fieldOfStudy.getUniversity().getCity()
                ),
                new DepartmentShortDTO(
                        fieldOfStudy.getDepartment().getId(),
                        fieldOfStudy.getDepartment().getName()
                )
        );
    }
}
