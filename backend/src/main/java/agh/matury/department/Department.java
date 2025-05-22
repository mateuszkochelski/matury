package agh.matury.department;

import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.university.University;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String url;

    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonBackReference
    private University university;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FieldOfStudy> fieldsOfStudy = new ArrayList<>();

    public Department() {
    }

    public Department(String name, String url, University university) {
        this.name = name;
        this.url = url;
        this.university = university;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public List<FieldOfStudy> getFieldsOfStudy() {
        return fieldsOfStudy;
    }
}
