package agh.matury.databaseSeeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FieldOfStudyExternalDTO(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("specialty") String specialty,
        @JsonProperty("university") long university,
        @JsonProperty("department") long department,
        @JsonProperty("level") String level,
        @JsonProperty("duration") Integer duration,
        @JsonProperty("language") String language,
        @JsonProperty("group") Integer group,
        @JsonProperty("slug") String slug
) {
}
