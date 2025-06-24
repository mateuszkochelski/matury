package agh.matury.academicSkillsTest;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "academic_skills_test_responses")
public class AcademicSkillsTestResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @ElementCollection
    @CollectionTable(
        name = "academic_skills_test_answers",
        joinColumns = @JoinColumn(name = "response_id")
    )
    private List<Integer> answers;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    public AcademicSkillsTestResponse() {
    }

    public AcademicSkillsTestResponse(String sessionId, List<Integer> answers) {
        this.sessionId = sessionId;
        this.answers = answers;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Integer> answers) {
        this.answers = answers;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
} 