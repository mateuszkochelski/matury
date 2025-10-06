package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UczelniaKierunekStatsDTO {
    private String nazwaUczelni;
    private String nazwaKierunku;
    private BigDecimal sredniWwz;
    private BigDecimal sredniWwb;
    private BigDecimal procentZatrudnionych;
    private Long liczbaAbsolwentow;
}

