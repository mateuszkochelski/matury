package agh.matury.fieldOfStudy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class FieldOfStudyFilter {

  @Schema(description = "Minimalna liczba semestrów")
  @Min(0)
  private Integer semestersFrom;

  @Schema(description = "Maksymalna liczba semestrów")
  @Min(0)
  private Integer semestersTo;

  @Schema(description = "Nazwa kierunku (LIKE, case-insensitive)")
  private String name;

  @Schema(description = "Nazwa wydziału (LIKE)")
  private String department;

  @Schema(description = "Nazwa uczelni (LIKE)")
  private String university;

  @Schema(description = "Miejscowość uczelni (LIKE)")
  private String city;

  @Schema(description = "Lista poziomów oddzielona przecinkami(np. bachelor,engineer,master,long_master)")
  private Set<String> degrees;

  private BigDecimal passRateFrom;
  private BigDecimal passRateTo;
  private BigDecimal avgSalaryFrom;
  private BigDecimal avgSalaryTo;

  @Schema(description = "Lista ID kierunków (powtarzany parametr lub rozdzielone przecinkami), np. ids=1,2,3")
  private Set<Long> ids;
}
