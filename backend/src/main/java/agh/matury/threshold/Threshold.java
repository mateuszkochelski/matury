package agh.matury.threshold;

import agh.matury.fieldOfStudy.FieldOfStudy;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "thresholds")
public class Threshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Integer year;

    private Integer phase;

    private Integer admissionLimit;

    private Integer admissions;

    private Integer threshold;

    @Column(length = 4096)
    private String specialRequirements;

    @ManyToOne
    @JoinColumn(name = "field_of_study_id")
    @JsonBackReference
    private FieldOfStudy fieldOfStudy;

    public Threshold() {
    }

    public Threshold(Integer year, Integer phase, Integer admissionLimit, Integer admissions, Integer threshold, String specialRequirements, FieldOfStudy fieldOfStudy) {
        this.year = year;
        this.phase = phase;
        this.admissionLimit = admissionLimit;
        this.admissions = admissions;
        this.threshold = threshold;
        this.specialRequirements = specialRequirements;
        this.fieldOfStudy = fieldOfStudy;
    }

    public FieldOfStudy getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(FieldOfStudy fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getAdmissions() {
        return admissions;
    }

    public void setAdmissions(Integer admissions) {
        this.admissions = admissions;
    }

    public Integer getAdmissionLimit() {
        return admissionLimit;
    }

    public void setAdmissionLimit(Integer admissionLimit) {
        this.admissionLimit = admissionLimit;
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
