package agh.matury.fieldOfStudy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FieldOfStudyRepository
    extends JpaRepository<FieldOfStudy, Long>, JpaSpecificationExecutor<FieldOfStudy> {

  Page<FieldOfStudy> findByUniversityId(Long universityId, Pageable pageable);

  Page<FieldOfStudy> findByDepartmentId(Long departmentId, Pageable pageable);

  Page<FieldOfStudy> findByDepartmentIdIn(List<Long> departmentIds, Pageable pageable);
}
