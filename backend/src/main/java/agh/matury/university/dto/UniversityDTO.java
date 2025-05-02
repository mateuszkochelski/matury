package agh.matury.university.dto;

public record UniversityDTO (
    long id,
    String name,
    String city,
    String acronym,
    String url,
    String description,
    String address,
    double longitude,
    double latitude
) {

}
