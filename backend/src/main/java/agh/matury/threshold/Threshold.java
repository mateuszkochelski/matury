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

    private int year;

    private int phase;

    private int admissionLimit;

    private int admissions;

    private int threshold;

    @Column(length = 4096)
    private String specialRequirements;

    @ManyToOne
    @JoinColumn(name = "field_of_study_id")
    @JsonBackReference
    private FieldOfStudy fieldOfStudy;

    public Threshold() {
    }

    public Threshold(int year, int phase, int admissionLimit, int admissions, int threshold, String specialRequirements, FieldOfStudy fieldOfStudy) {
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

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getAdmissions() {
        return admissions;
    }

    public void setAdmissions(int admissions) {
        this.admissions = admissions;
    }

    public int getAdmissionLimit() {
        return admissionLimit;
    }

    public void setAdmissionLimit(int admissionLimit) {
        this.admissionLimit = admissionLimit;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
