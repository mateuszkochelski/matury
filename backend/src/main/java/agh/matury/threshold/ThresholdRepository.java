package agh.matury.threshold;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRepository extends JpaRepository<Threshold, Long> {

    Page<Threshold> findByFieldOfStudyId(Long fieldOfStudyId, Pageable pageable);
}
