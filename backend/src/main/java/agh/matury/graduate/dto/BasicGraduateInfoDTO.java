package agh.matury.graduate.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class BasicGraduateInfoDTO {
    private String wojewodztwo;
    private Integer rokUkonczeniaStudiow;
    private String stopienStudiow;
    private String formaStudiow;
    private Integer liczbaAbsolwentow;
    private Long liczbaRekordow; // Liczba rekordów w danej kategorii

    public BasicGraduateInfoDTO(String wojewodztwo, Integer rok, String stopien, String forma, 
                               Integer liczbaAbsolwentow, Long liczbaRekordow) {
        this.wojewodztwo = wojewodztwo;
        this.rokUkonczeniaStudiow = rok;
        this.stopienStudiow = stopien;
        this.formaStudiow = forma;
        this.liczbaAbsolwentow = liczbaAbsolwentow;
        this.liczbaRekordow = liczbaRekordow;
    }
}