package agh.matury.university;

import agh.matury.department.Department;
import agh.matury.fieldOfStudy.FieldOfStudy;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "universities")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String city;

    private String acronym;

    private String url;

    @Column(length = 4096)
    private String description;

    private String address;

    private double longitude;

    private double latitude;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FieldOfStudy> fieldsOfStudy =  new ArrayList<>();

    public University() {
    }

    public University(String name, String city, String acronym, String url, String description, String address, double longitude, double latitude) {
        this.name = name;
        this.city = city;
        this.acronym = acronym;
        this.url = url;
        this.description = description;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<FieldOfStudy> getFieldsOfStudy() {
        return fieldsOfStudy;
    }
}
