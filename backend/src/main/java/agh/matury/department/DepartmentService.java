package agh.matury.department;

import agh.matury.department.dto.DepartmentDTO;
import agh.matury.university.dto.UniversityShortDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository){
        this.departmentRepository = departmentRepository;
    }

    public Page<DepartmentDTO> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::toDTO);
    }

    public DepartmentDTO getDepartmentById(Long id) {
        if(departmentRepository.findById(id).isPresent()) {
            return toDTO(departmentRepository.findById(id).get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
    }

    public Page<DepartmentDTO> getDepartmentsByUniversityId(Long universityId, Pageable pageable) {
        return departmentRepository.findByUniversityId(universityId, pageable).map(this::toDTO);
    }

    public Page<DepartmentDTO> searchDepartments(String name, Pageable pageable) {
        return departmentRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toDTO);
    }

    private DepartmentDTO toDTO(Department department) {
        return new DepartmentDTO(
                department.getId(),
                department.getName(),
                department.getUrl(),
                new UniversityShortDTO(
                        department.getUniversity().getId(),
                        department.getUniversity().getName(),
                        department.getUniversity().getAcronym(),
                        department.getUniversity().getCity()
                )
        );
    }
}
