package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class DziedzinaStatsDTO {
    private String dziedzina;
    private BigDecimal wwzTrzeciRok;      // Względny Wskaźnik Zarobków w 3. roku
    private BigDecimal wwbTrzeciRok;      // Względny Wskaźnik Bezrobocia w 3. roku
    private BigDecimal zatrudnienieTrzeciRok; // Procent zatrudnionych w 3. roku

    public DziedzinaStatsDTO(String dziedzina, BigDecimal wwz, BigDecimal wwb, BigDecimal zatrudnienie) {
        this.dziedzina = dziedzina;
        this.wwzTrzeciRok = wwz;
        this.wwbTrzeciRok = wwb;
        this.zatrudnienieTrzeciRok = zatrudnienie;
    }
}