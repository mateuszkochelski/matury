package agh.matury.fieldOfStudy.dto;

import java.util.List;

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
