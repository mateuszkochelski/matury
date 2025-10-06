package agh.matury.graduate;

import agh.matury.graduate.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


@Service
@RequiredArgsConstructor
public class GraduateService {

    private final GraduateRepository graduateRepository;
    private final FieldOfStudyMonthlyIndexRepository monthlyIndexRepository;

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(((Number) value).toString());
        }
        return null;
    }

    // Podstawowe filtry
    public List<String> getAllWojewodztwa() {
        return graduateRepository.findAllWojewodztwa();
    }

    public List<String> getAllPoziomy() {
        return graduateRepository.findAllPoziomy();
    }

    public List<Integer> getAllRokiDyplomu() {
        return graduateRepository.findAllRokiDyplomu();
    }

    public List<String> getAllDziedziny() {
        return graduateRepository.findAllDziedziny();
    }

    // Agregacje wskaźników zatrudnienia
    public List<EmploymentStatsDTO> getEmploymentStatsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findEmploymentStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new EmploymentStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // p1
                        toBigDecimal(result[2]),  // p2
                        toBigDecimal(result[3]),  // p3
                        toBigDecimal(result[4]),  // p4
                        toBigDecimal(result[5])   // p5
                ))
                .collect(Collectors.toList());
    }

    public List<AdvancedEmploymentStatsDTO> getAdvancedEmploymentStatsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        // Ta metoda zakłada, że pobieramy surowe dane i liczymy statystyki w aplikacji.
        // To jest mniej wydajne dla prostych statystyk jak średnia, ale bardziej elastyczne.
        List<Object[]> results = graduateRepository.findRawEmploymentData(wojewodztwo, poziom, rokDyplomu);

        // Grupujemy wyniki po województwie
        Map<String, List<Object[]>> groupedByWojewodztwo = results.stream()
                .collect(Collectors.groupingBy(res -> (String) res[0]));

        // Dla każdego województwa obliczamy statystyki
        return groupedByWojewodztwo.entrySet().stream()
                .map(entry -> {
                    String woj = entry.getKey();
                    List<Object[]> data = entry.getValue();

                    Map<String, AdvancedEmploymentStatsDTO.AdvancedStats> yearlyStats = Stream.of("p1", "p2", "p3", "p4", "p5")
                            .collect(Collectors.toMap(
                                    year -> year,
                                    year -> {
                                        int
                                                index =
                                                Integer.parseInt(year.substring(1));
                                        double[] values = data.stream()
                                                .map(row -> toBigDecimal(row[index]))
                                                .filter(java.util.Objects::nonNull)
                                                .mapToDouble(BigDecimal::doubleValue)
                                                .toArray();

                                        if (values.length == 0) {
                                            return new AdvancedEmploymentStatsDTO.AdvancedStats();
                                        }

                                        DescriptiveStatistics stats = new DescriptiveStatistics(values);
                                        return new AdvancedEmploymentStatsDTO.AdvancedStats(
                                                stats.getMean(),
                                                stats.getPercentile(50), // Mediana
                                                stats.getStandardDeviation(),
                                                stats.getMin(),
                                                stats.getMax()
                                        );
                                    }
                            ));

                    return new AdvancedEmploymentStatsDTO(woj, yearlyStats);
                })
                .collect(Collectors.toList());
    }

    // Agregacje wskaźników bezrobocia
    public List<UnemploymentStatsDTO> getUnemploymentStatsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findUnemploymentStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new UnemploymentStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // bezr1
                        toBigDecimal(result[2]),  // bezr2
                        toBigDecimal(result[3]),  // bezr3
                        toBigDecimal(result[4]),  // bezr4
                        toBigDecimal(result[5])   // bezr5
                ))
                .collect(Collectors.toList());
    }

    public List<UnemploymentStatsDTO> getRelativeUnemploymentIndexByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findRelativeUnemploymentIndexByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new UnemploymentStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // wwb1
                        toBigDecimal(result[2]),  // wwb2
                        toBigDecimal(result[3]),  // wwb3
                        toBigDecimal(result[4]),  // wwb4
                        toBigDecimal(result[5]),  // wwb5
                        true  // isWwb flag
                ))
                .collect(Collectors.toList());
    }

    // Agregacje wskaźników zarobków
    public List<SalaryStatsDTO> getSalaryStatsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findSalaryStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new SalaryStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // zar1
                        toBigDecimal(result[2]),  // zar2
                        toBigDecimal(result[3]),  // zar3
                        toBigDecimal(result[4]),  // zar4
                        toBigDecimal(result[5])   // zar5
                ))
                .collect(Collectors.toList());
    }

    public List<SalaryStatsDTO> getRelativeSalaryIndexByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findRelativeSalaryIndexByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new SalaryStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // wwz1
                        toBigDecimal(result[2]),  // wwz2
                        toBigDecimal(result[3]),  // wwz3
                        toBigDecimal(result[4]),  // wwz4
                        toBigDecimal(result[5]),  // wwz5
                        true  // isWwz flag
                ))
                .collect(Collectors.toList());
    }

    // Agregacje geograficzne
    public GeographicStatsDTO getGeographicStats(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findGeographicStats(wojewodztwo, poziom, rokDyplomu);
        
        if (results.isEmpty()) {
            return new GeographicStatsDTO();
        }
        
        Object[] result = results.get(0);
        return new GeographicStatsDTO(
                toBigDecimal(result[0]),  // zarKmz1
                toBigDecimal(result[1]),  // zarKmz2
                toBigDecimal(result[2]),  // zarKmz3
                toBigDecimal(result[3]),  // wwzKmz1
                toBigDecimal(result[4]),  // wwzKmz2
                toBigDecimal(result[5])   // wwzKmz3
        );
    }

    // Agregacje kariery akademickiej
    public List<AcademicCareerStatsDTO> getAcademicCareerStatsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findAcademicCareerStatsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new AcademicCareerStatsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // procStudia
                        toBigDecimal(result[2]),  // procUkon
                        toBigDecimal(result[3]),  // procDyplom
                        toBigDecimal(result[4]),  // procDoktoranckie
                        toBigDecimal(result[5])   // procDoktorat
                ))
                .collect(Collectors.toList());
    }

    // Agregacje per dziedzina nauki
    public List<DziedzinaStatsDTO> getStatsByDziedzina(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findStatsByDziedzina(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new DziedzinaStatsDTO(
                        (String) result[0],      // dziedzina
                        toBigDecimal(result[1]),  // wwz
                        toBigDecimal(result[2]),  // wwb
                        toBigDecimal(result[3])   // zatrudnienie
                ))
                .collect(Collectors.toList());
    }

    // Agregacje per uczelnia
    public List<UczelniaStatsDTO> getStatsByUczelnia(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findStatsByUczelnia(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new UczelniaStatsDTO(
                        (String) result[0],      // nazwaUczelni
                        toBigDecimal(result[1]),  // wwz
                        toBigDecimal(result[2]),  // wwb
                        toBigDecimal(result[3]),  // zatrudnienie
                        (Long) result[4]         // liczbaAbsolwentow
                ))
                .collect(Collectors.toList());
    }
    
    public List<UczelniaKierunekStatsDTO> getStatsByUczelniaAndKierunek(String wojewodztwo, String poziom, Integer rokDyplomu, Long minCount) {
        if (minCount == null || minCount < 1) {
            minCount = 10L;
        }
        
        List<Object[]> results = graduateRepository.getStatsByUczelniaAndKierunek(wojewodztwo, poziom, rokDyplomu, minCount);
        return results.stream()
                .map(result -> new UczelniaKierunekStatsDTO(
                        (String) result[0],
                        (String) result[1],
                        toBigDecimal(result[2]),
                        toBigDecimal(result[3]),
                        toBigDecimal(result[4]),
                        (Long) result[5]
                ))
                .collect(Collectors.toList());
    }

    // ==================== TOP KIERUNKI ====================

    public List<KierunekStatsDTO> getTopKierunkiByWwz(String wojewodztwo, String poziom, Integer rokDyplomu, Long minCount) {
        if (minCount == null) {
            minCount = 10L; // domyślnie minimum 10 absolwentów
        }
        
        List<Object[]> results = graduateRepository.findTopKierunkiByWwz(wojewodztwo, poziom, rokDyplomu, minCount);
        
        return results.stream()
                .map(result -> new KierunekStatsDTO(
                        (String) result[0],      // nazwaKierunku
                        toBigDecimal(result[1]),  // wwz
                        toBigDecimal(result[2]),  // zarobki
                        (Long) result[3]         // liczbaAbsolwentow
                ))
                .collect(Collectors.toList());
    }

    // Podstawowe CRUD operations
    public List<Graduate> getAllGraduates() {
        return graduateRepository.findAll();
    }

    public Graduate getGraduateById(Long id) {
        return graduateRepository.findById(id).orElse(null);
    }

    public Graduate saveGraduate(Graduate graduate) {
        return graduateRepository.save(graduate);
    }

    public void deleteGraduate(Long id) {
        graduateRepository.deleteById(id);
    }

    // ==================== WSKAŹNIKI PRACODAWCÓW I ROTACJI ZATRUDNIENIA ====================

    public List<EmploymentMetricsDTO> getEmploymentMetricsByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findEmploymentMetricsByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new EmploymentMetricsDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // srednia miesięczna liczba pracodawców
                        toBigDecimal(result[2]),  // q1 pracodawcy
                        toBigDecimal(result[3]),  // q2 pracodawcy
                        toBigDecimal(result[4]),  // q3 pracodawcy
                        toBigDecimal(result[5]),  // q4 pracodawcy
                        toBigDecimal(result[6]),  // srednia roczna liczba zakończeń etatów
                        toBigDecimal(result[7]),  // raz na ile lat
                        toBigDecimal(result[8]),  // q1 zakończenia
                        toBigDecimal(result[9]),  // q2 zakończenia
                        toBigDecimal(result[10]), // q3 zakończenia
                        toBigDecimal(result[11]), // q4 zakończenia
                        (Long) result[12]        // liczba absolwentów z etatem
                ))
                .collect(Collectors.toList());
    }

    // ==================== CZAS DO PODJĘCIA PRACY ====================

    public List<TimeToEmploymentDTO> getTimeToEmploymentStats(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> basicResults = graduateRepository.findTimeToEmploymentStats(wojewodztwo, poziom, rokDyplomu);
        List<Object[]> quintileResults = graduateRepository.findTimeToEmploymentQuintiles(wojewodztwo, poziom, rokDyplomu);
        
        List<TimeToEmploymentDTO> result = new ArrayList<>();
        
        if (!basicResults.isEmpty()) {
            Object[] basic = basicResults.get(0);
            TimeToEmploymentDTO dto = new TimeToEmploymentDTO(
                    "Ogółem",
                    toBigDecimal(basic[0]), // czas pracy ogółem
                    toBigDecimal(basic[1]), // czas etatu ogółem
                    toBigDecimal(basic[2]), // czas pracy z doświadczeniem
                    toBigDecimal(basic[3]), // czas pracy bez doświadczenia
                    toBigDecimal(basic[4]), // czas etatu z doświadczeniem
                    toBigDecimal(basic[5]), // czas etatu bez doświadczenia
                    null, null, null, null, // kwintyle pracy (ustawione później)
                    null, null, null, null  // kwintyle etatu (ustawione później)
            );
            
            // Dodaj kwintyle jeśli dostępne
            if (!quintileResults.isEmpty()) {
                Object[] quintiles = quintileResults.get(0);
                dto.setCzasPracaQ1(toBigDecimal(quintiles[0]));
                dto.setCzasPracaQ2(toBigDecimal(quintiles[1]));
                dto.setCzasPracaQ3(toBigDecimal(quintiles[2]));
                dto.setCzasPracaQ4(toBigDecimal(quintiles[3]));
                dto.setCzasEtatQ1(toBigDecimal(quintiles[4]));
                dto.setCzasEtatQ2(toBigDecimal(quintiles[5]));
                dto.setCzasEtatQ3(toBigDecimal(quintiles[6]));
                dto.setCzasEtatQ4(toBigDecimal(quintiles[7]));
            }
            
            result.add(dto);
        }
        
        return result;
    }

    // ==================== KONTYNUACJA STUDIÓW PO I STOPNIU ====================

    public List<ContinuationStudiesDTO> getContinuationStudiesByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findContinuationStudiesByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new ContinuationStudiesDTO(
                        (String) result[0],      // wojewodztwo
                        toBigDecimal(result[1]),  // p1
                        toBigDecimal(result[2]),  // p2
                        toBigDecimal(result[3]),  // p3
                        toBigDecimal(result[4]),  // p4
                        toBigDecimal(result[5]),  // p5
                        toBigDecimal(result[6]),  // ogółem
                        toBigDecimal(result[7])   // ta sama uczelnia
                ))
                .collect(Collectors.toList());
    }

    // ==================== PODSTAWOWE INFORMACJE O ABSOLWENTACH ====================

    public List<BasicGraduateInfoDTO> getBasicGraduateInfo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findBasicGraduateInfo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new BasicGraduateInfoDTO(
                        (String) result[0],      // wojewodztwo
                        (Integer) result[1],     // rok dyplomu
                        (String) result[2],      // poziom
                        (String) result[3],      // forma
                        toBigDecimal(result[4]).intValue(),     // suma liczby absolwentów
                        (Long) result[5]         // liczba rekordów
                ))
                .collect(Collectors.toList());
    }

    public List<BasicGraduateInfoDTO> getBasicGraduateSummaryByWojewodztwo(String wojewodztwo, String poziom, Integer rokDyplomu) {
        List<Object[]> results = graduateRepository.findBasicGraduateSummaryByWojewodztwo(wojewodztwo, poziom, rokDyplomu);
        
        return results.stream()
                .map(result -> new BasicGraduateInfoDTO(
                        (String) result[0],      // wojewodztwo
                        result[3] != null ? Math.round(((Double) result[3]).floatValue()) : null, // średni rok (zaokrąglony)
                        "Wszystkie",             // poziom
                        "Wszystkie",             // forma
                        toBigDecimal(result[2]).intValue(),     // suma liczby absolwentów
                        (Long) result[1]         // liczba rekordów
                ))
                .collect(Collectors.toList());
    }

    // ==================== SZCZEGÓŁY KIERUNKU/PROGRAMU ====================

    public FieldOfStudyDetailDTO getFieldOfStudyDetail(String uczelniaId, String kierunekId, String poziom, Integer rokDyplomu) {
        List<Object[]> detailRes = graduateRepository.findFieldOfStudyDetail(uczelniaId, kierunekId, poziom, rokDyplomu);
        if (detailRes.isEmpty()) {
            return new FieldOfStudyDetailDTO();
        }

        Object[] r = detailRes.get(0);
        FieldOfStudyDetailDTO.FieldOfStudyDetailDTOBuilder b = FieldOfStudyDetailDTO.builder()
                .uczelniaId((String) r[0])
                .kierunekId((String) r[1])
                .poziom((String) r[2])
                .rokDyplomu((Integer) r[3])
                .eZarP1(toBigDecimal(r[4]))
                .eZarP2(toBigDecimal(r[5]))
                .eZarP3(toBigDecimal(r[6]))
                .eZarP4(toBigDecimal(r[7]))
                .eZarP5(toBigDecimal(r[8]))
                .eZarEtatP1(toBigDecimal(r[9]))
                .eZarEtatP2(toBigDecimal(r[10]))
                .eZarEtatP3(toBigDecimal(r[11]))
                .eZarEtatP4(toBigDecimal(r[12]))
                .eZarEtatP5(toBigDecimal(r[13]))
                .meZar(toBigDecimal(r[14]))
                .meZarEtat(toBigDecimal(r[15]))
                .zarQ1(toBigDecimal(r[16]))
                .zarQ2(toBigDecimal(r[17]))
                .zarQ3(toBigDecimal(r[18]))
                .zarQ4(toBigDecimal(r[19]))
                .wwzP1(toBigDecimal(r[20]))
                .wwzP2(toBigDecimal(r[21]))
                .wwzP3(toBigDecimal(r[22]))
                .wwzP4(toBigDecimal(r[23]))
                .wwzP5(toBigDecimal(r[24]))
                .wwbP1(toBigDecimal(r[25]))
                .wwbP2(toBigDecimal(r[26]))
                .wwbP3(toBigDecimal(r[27]))
                .wwbP4(toBigDecimal(r[28]))
                .wwbP5(toBigDecimal(r[29]))
                .czasPraca(toBigDecimal(r[30]))
                .czasPracaQ1(toBigDecimal(r[31]))
                .czasPracaQ2(toBigDecimal(r[32]))
                .czasPracaQ3(toBigDecimal(r[33]))
                .czasPracaQ4(toBigDecimal(r[34]))
                .czasEtat(toBigDecimal(r[35]))
                .czasEtatQ1(toBigDecimal(r[36]))
                .czasEtatQ2(toBigDecimal(r[37]))
                .czasEtatQ3(toBigDecimal(r[38]))
                .czasEtatQ4(toBigDecimal(r[39]))
                .procMiesPraca(toBigDecimal(r[40]))
                .procMiesEtat(toBigDecimal(r[41]))
                .procMiesSamoz(toBigDecimal(r[42]));

        List<Object[]> monthly = monthlyIndexRepository.findMonthlySeries(uczelniaId, kierunekId, poziom, rokDyplomu);
        if (!monthly.isEmpty()) {
            List<Integer> mies = monthly.stream().map(o -> (Integer) o[0]).collect(Collectors.toList());
            List<BigDecimal> wwz = monthly.stream().map(o -> toBigDecimal(o[1])).collect(Collectors.toList());
            List<BigDecimal> wwb = monthly.stream().map(o -> toBigDecimal(o[2])).collect(Collectors.toList());
            b.miesiace(mies).wwzMies(wwz).wwbMies(wwb);
        }

        return b.build();
    }
}