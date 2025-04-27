package agh.matury.databaseSeeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DepartmentExternalDTO(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("url") String url,
        @JsonProperty("description") String description,
        @JsonProperty("address") String address,
        @JsonProperty("logo") String logo,
        @JsonProperty("slug") String slug,
        @JsonProperty("longitude") Double longitude,
        @JsonProperty("latitude") Double latitude,
        @JsonProperty("university") long university
) {
}
