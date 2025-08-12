package agh.matury.graduate.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class ContinuationStudiesDTO {
    private String wojewodztwo;
    
    // Procent osób kontynuujących studia II stopnia per rok
    private BigDecimal kontynuacjaPierwszyRok;
    private BigDecimal kontynuacjaDrugiRok;
    private BigDecimal kontynuacjaTrzeciRok;
    private BigDecimal kontynuacjaCzwartyRok;
    private BigDecimal kontynuacjaPiatyRok;
    
    // Ogólny procent kontynuujących studia II stopnia
    private BigDecimal kontynuacjaOgolem;
    
    // Procent kontynuujących na tej samej uczelni
    private BigDecimal kontynuacjaNaTejSamejUczelni;

    public ContinuationStudiesDTO(String wojewodztwo, BigDecimal p1, BigDecimal p2, BigDecimal p3, 
                                 BigDecimal p4, BigDecimal p5, BigDecimal ogolem, BigDecimal taSamaUczelnia) {
        this.wojewodztwo = wojewodztwo;
        this.kontynuacjaPierwszyRok = p1;
        this.kontynuacjaDrugiRok = p2;
        this.kontynuacjaTrzeciRok = p3;
        this.kontynuacjaCzwartyRok = p4;
        this.kontynuacjaPiatyRok = p5;
        this.kontynuacjaOgolem = ogolem;
        this.kontynuacjaNaTejSamejUczelni = taSamaUczelnia;
    }
}