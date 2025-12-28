package agh.matury.fieldOfStudy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldOfStudySearchResponse(
    List<FieldOfStudyExtendedDTO> content,
    PageMetadata page,
    String matched
) {
  public record PageMetadata(
      int size,
      int number,
      long totalElements,
      int totalPages
  ) {}
}
