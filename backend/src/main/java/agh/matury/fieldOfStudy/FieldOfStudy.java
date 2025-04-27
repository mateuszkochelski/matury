package agh.matury.fieldOfStudy;

import agh.matury.department.Department;
import agh.matury.threshold.Threshold;
import agh.matury.university.University;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "field_of_study")
public class FieldOfStudy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String level;

    private int duration;

    private String language;

    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonBackReference
    private University university;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;

    @OneToMany(mappedBy = "fieldOfStudy", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Threshold> thresholds = new ArrayList<>();

    public FieldOfStudy() {
    }

    public FieldOfStudy(String name, String level, University university, Department department, int duration, String language) {
        this.name = name;
        this.level = level;
        this.university = university;
        this.department = department;
        this.duration = duration;
        this.language = language;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<Threshold> getThresholds() {
        return thresholds;
    }
}
