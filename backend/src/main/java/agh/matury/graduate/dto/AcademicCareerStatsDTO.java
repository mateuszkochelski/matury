package agh.matury.graduate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class AcademicCareerStatsDTO {
    private String wojewodztwo;
    
    // Wskaźniki kontynuacji edukacji
    private BigDecimal procentKontynuujacychStudia;
    private BigDecimal procentUkończonychStudiowII;
    private BigDecimal procentZdobywajacychDyplom;
    private BigDecimal procentStudiowDoktoranckich;
    private BigDecimal procentZdobywajacychDoktorat;

    public AcademicCareerStatsDTO(String wojewodztwo, BigDecimal procStudia, BigDecimal procUkon,
                                 BigDecimal procDyplom, BigDecimal procDoktoranckie, BigDecimal procDoktorat) {
        this.wojewodztwo = wojewodztwo;
        this.procentKontynuujacychStudia = procStudia;
        this.procentUkończonychStudiowII = procUkon;
        this.procentZdobywajacychDyplom = procDyplom;
        this.procentStudiowDoktoranckich = procDoktoranckie;
        this.procentZdobywajacychDoktorat = procDoktorat;
    }
}