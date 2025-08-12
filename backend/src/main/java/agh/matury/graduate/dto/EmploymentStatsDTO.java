package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentStatsDTO {
    private String wojewodztwo;
    private BigDecimal zatrudnieniePierwszyRok;
    private BigDecimal zatrudnienieDrugiRok;
    private BigDecimal zatrudnienieTrzeciRok;
    private BigDecimal zatrudnienieCzwartyRok;
    private BigDecimal zatrudnieniePiatyRok;
    private BigDecimal srednieZatrudnienie;

    // Konstruktor dla wyników zapytań agregacyjnych
    public EmploymentStatsDTO(String wojewodztwo, BigDecimal p1, BigDecimal p2, 
                             BigDecimal p3, BigDecimal p4, BigDecimal p5) {
        this.wojewodztwo = wojewodztwo;
        this.zatrudnieniePierwszyRok = p1;
        this.zatrudnienieDrugiRok = p2;
        this.zatrudnienieTrzeciRok = p3;
        this.zatrudnienieCzwartyRok = p4;
        this.zatrudnieniePiatyRok = p5;
        
        // Oblicz średnie zatrudnienie
        if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) {
            this.srednieZatrudnienie = p1.add(p2).add(p3).add(p4).add(p5)
                .divide(BigDecimal.valueOf(5), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}