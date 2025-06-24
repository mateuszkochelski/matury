package agh.matury.academicSkillsTest;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_skills_test_questions")
public class AcademicSkillsTestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicSkillCategory category;

    @Column(nullable = false)
    private int orderNumber;

    public AcademicSkillsTestQuestion() {
    }

    public AcademicSkillsTestQuestion(String questionText, AcademicSkillCategory category, int orderNumber) {
        this.questionText = questionText;
        this.category = category;
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public AcademicSkillCategory getCategory() {
        return category;
    }

    public void setCategory(AcademicSkillCategory category) {
        this.category = category;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
} 