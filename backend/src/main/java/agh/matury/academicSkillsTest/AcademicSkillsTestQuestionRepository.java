package agh.matury.academicSkillsTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicSkillsTestQuestionRepository extends JpaRepository<AcademicSkillsTestQuestion, Long> {
    List<AcademicSkillsTestQuestion> findAllByOrderByOrderNumberAsc();
    List<AcademicSkillsTestQuestion> findByCategory(AcademicSkillCategory category);
} 