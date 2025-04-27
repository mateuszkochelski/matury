package agh.matury.databaseSeeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UniversityExternalDTO(
        @JsonProperty("id") long id,
        @JsonProperty("city") String city,
        @JsonProperty("name") String name,
        @JsonProperty("acronym") String acronym,
        @JsonProperty("type") String type,
        @JsonProperty("url") String url,
        @JsonProperty("official_collaboration") boolean officialCollaboration,
        @JsonProperty("has_threshold_published") boolean hasThresholdPublished,
        @JsonProperty("deny_publishing_threshold") boolean denyPublishingThreshold,
        @JsonProperty("description") String description,
        @JsonProperty("address") String address,
        @JsonProperty("is_public") boolean isPublic,
        @JsonProperty("logo") String logo,
        @JsonProperty("any_threshold_available") boolean anyThresholdAvailable,
        @JsonProperty("slug") String slug,
        @JsonProperty("formula_uuid") String formulaUuid,
        @JsonProperty("youtrack_id") String youtrackId,
        @JsonProperty("max_recruitment_year") int maxRecruitmentYear,
        @JsonProperty("longitude") double longitude,
        @JsonProperty("latitude") double latitude,
        @JsonProperty("regon") String regon,
        @JsonProperty("nip") String nip,
        @JsonProperty("krs") String krs,
        @JsonProperty("manager") String manager,
        @JsonProperty("manager_function") String managerFunction,
        @JsonProperty("esp_address") String espAddress,
        @JsonProperty("patent_count") int patentCount,
        @JsonProperty("radon_institution") String radonInstitution
) {
}
