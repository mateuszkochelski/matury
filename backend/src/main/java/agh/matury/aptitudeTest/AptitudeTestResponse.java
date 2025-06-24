package agh.matury.aptitudeTest;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "aptitude_test_responses")
public class AptitudeTestResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @ElementCollection
    @CollectionTable(
        name = "aptitude_test_answers",
        joinColumns = @JoinColumn(name = "response_id")
    )
    private List<Integer> answers;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    public AptitudeTestResponse() {
    }

    public AptitudeTestResponse(String sessionId, List<Integer> answers) {
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