package agh.matury.aptitudeTest;

import jakarta.persistence.*;

@Entity
@Table(name = "aptitude_test_questions")
public class AptitudeTestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HollandCategory category;

    @Column(nullable = false)
    private int orderNumber;

    public AptitudeTestQuestion() {
    }

    public AptitudeTestQuestion(String questionText, HollandCategory category, int orderNumber) {
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

    public HollandCategory getCategory() {
        return category;
    }

    public void setCategory(HollandCategory category) {
        this.category = category;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
} 