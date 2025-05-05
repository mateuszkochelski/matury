package agh.matury.fieldOfStudy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {

    Page<FieldOfStudy> findByUniversityId(Long universityId, Pageable pageable);

    Page<FieldOfStudy> findByDepartmentId(Long departmentId, Pageable pageable);
}
