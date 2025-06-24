package agh.matury.academicSkillsTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicSkillsTestResponseRepository extends JpaRepository<AcademicSkillsTestResponse, Long> {
    Optional<AcademicSkillsTestResponse> findBySessionId(String sessionId);
} 