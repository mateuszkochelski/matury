package agh.matury.graduate;

import agh.matury.graduate.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraduateServiceTest {

    @Mock
    private GraduateRepository graduateRepository;

    @InjectMocks
    private GraduateService graduateService;

    private Graduate testGraduate;

    @BeforeEach
    void setUp() {
        testGraduate = Graduate.builder()
                .id(1L)
                .wojewodztwo("mazowieckie")
                .poziom("I stopień")
                .rokDyplomu(2020)
                .liczbaAbsolwentow(100)
                .wwzP3(new BigDecimal("1.25"))
                .wwbP3(new BigDecimal("0.85"))
                .czyPracaP3(new BigDecimal("85.5"))
                .czasPraca(new BigDecimal("3.2"))
                .czasEtat(new BigDecimal("4.1"))
                .if2stP3(new BigDecimal("45.5"))
                .build();
    }

    @Test
    void testGetAllWojewodztwa() {
        // Given
        List<String> expectedWojewodztwa = Arrays.asList("dolnośląskie", "mazowieckie", "śląskie");
        when(graduateRepository.findAllWojewodztwa()).thenReturn(expectedWojewodztwa);

        // When
        List<String> result = graduateService.getAllWojewodztwa();

        // Then
        assertEquals(expectedWojewodztwa, result);
        verify(graduateRepository).findAllWojewodztwa();
    }

    @Test
    void testGetEmploymentStatsByWojewodztwo() {
        // Given
        Object[] mockResult = {
                "mazowieckie", 
                new BigDecimal("85.5"), 
                new BigDecimal("87.2"), 
                new BigDecimal("89.1"), 
                new BigDecimal("90.5"), 
                new BigDecimal("91.8")
        };
        when(graduateRepository.findEmploymentStatsByWojewodztwo(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<EmploymentStatsDTO> result = graduateService.getEmploymentStatsByWojewodztwo("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        EmploymentStatsDTO dto = result.get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(new BigDecimal("85.5"), dto.getZatrudnieniePierwszyRok());
        assertNotNull(dto.getSrednieZatrudnienie());
        verify(graduateRepository).findEmploymentStatsByWojewodztwo("mazowieckie", null, null);
    }

    @Test
    void testGetSalaryStatsByWojewodztwo() {
        // Given
        Object[] mockResult = {
                "mazowieckie",
                new BigDecimal("5500.00"),
                new BigDecimal("6200.00"),
                new BigDecimal("6800.00"),
                new BigDecimal("7200.00"),
                new BigDecimal("7600.00")
        };
        when(graduateRepository.findSalaryStatsByWojewodztwo(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<SalaryStatsDTO> result = graduateService.getSalaryStatsByWojewodztwo("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        SalaryStatsDTO dto = result.get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(new BigDecimal("5500.00"), dto.getZarobkiPierwszyRok());
        assertNotNull(dto.getSrednieZarobki());
    }

    @Test
    void testGetEmploymentMetricsByWojewodztwo() {
        // Given
        Object[] mockResult = {
                "mazowieckie",        // wojewodztwo
                new BigDecimal("1.2"), // średnia miesięczna liczba pracodawców
                new BigDecimal("1.0"), // q1
                new BigDecimal("1.1"), // q2
                new BigDecimal("1.3"), // q3
                new BigDecimal("1.5"), // q4
                new BigDecimal("0.8"), // średnia roczna liczba zakończeń etatów
                new BigDecimal("1.25"), // raz na ile lat
                new BigDecimal("0.5"), // q1 zakończenia
                new BigDecimal("0.7"), // q2 zakończenia
                new BigDecimal("0.9"), // q3 zakończenia
                new BigDecimal("1.2"), // q4 zakończenia
                250L               // liczba absolwentów z etatem
        };
        when(graduateRepository.findEmploymentMetricsByWojewodztwo(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<EmploymentMetricsDTO> result = graduateService.getEmploymentMetricsByWojewodztwo("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        EmploymentMetricsDTO dto = result.get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(new BigDecimal("1.2"), dto.getSredniaMiesięcznaLiczbaPracodawcow());
        assertEquals(new BigDecimal("0.8"), dto.getSredniaNRocznychZakonczenEtatow());
        assertEquals(250L, dto.getLiczbaAbsolwentowZEtatem());
    }

    @Test
    void testGetTimeToEmploymentStats() {
        // Given
        Object[] basicResult = {
                new BigDecimal("3.2"), // czas pracy ogółem
                new BigDecimal("4.1"), // czas etatu ogółem
                new BigDecimal("2.8"), // czas pracy z doświadczeniem
                new BigDecimal("4.5"), // czas pracy bez doświadczenia
                new BigDecimal("3.5"), // czas etatu z doświadczeniem
                new BigDecimal("5.2")  // czas etatu bez doświadczenia
        };
        Object[] quintileResult = {
                new BigDecimal("1.0"), // czas praca q1
                new BigDecimal("2.0"), // czas praca q2
                new BigDecimal("3.0"), // czas praca q3
                new BigDecimal("5.0"), // czas praca q4
                new BigDecimal("1.5"), // czas etat q1
                new BigDecimal("2.5"), // czas etat q2
                new BigDecimal("4.0"), // czas etat q3
                new BigDecimal("6.0")  // czas etat q4
        };
        
        when(graduateRepository.findTimeToEmploymentStats(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(basicResult));
        when(graduateRepository.findTimeToEmploymentQuintiles(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(quintileResult));

        // When
        List<TimeToEmploymentDTO> result = graduateService.getTimeToEmploymentStats("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        TimeToEmploymentDTO dto = result.get(0);
        assertEquals("Ogółem", dto.getKategoria());
        assertEquals(new BigDecimal("3.2"), dto.getCzasDoPracyOgolem());
        assertEquals(new BigDecimal("4.1"), dto.getCzasDoEtatuOgolem());
        assertEquals(new BigDecimal("1.0"), dto.getCzasPracaQ1());
        assertEquals(new BigDecimal("6.0"), dto.getCzasEtatQ4());
    }

    @Test
    void testGetContinuationStudiesByWojewodztwo() {
        // Given
        Object[] mockResult = {
                "mazowieckie",
                new BigDecimal("35.2"), // p1
                new BigDecimal("42.1"), // p2
                new BigDecimal("45.5"), // p3
                new BigDecimal("47.8"), // p4
                new BigDecimal("49.2"), // p5
                new BigDecimal("52.5"), // ogółem
                new BigDecimal("68.3")  // ta sama uczelnia
        };
        when(graduateRepository.findContinuationStudiesByWojewodztwo(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<ContinuationStudiesDTO> result = graduateService.getContinuationStudiesByWojewodztwo("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        ContinuationStudiesDTO dto = result.get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(new BigDecimal("35.2"), dto.getKontynuacjaPierwszyRok());
        assertEquals(new BigDecimal("52.5"), dto.getKontynuacjaOgolem());
        assertEquals(new BigDecimal("68.3"), dto.getKontynuacjaNaTejSamejUczelni());
    }

    @Test
    void testGetBasicGraduateInfo() {
        // Given
        Object[] mockResult = {
                "mazowieckie", // wojewodztwo
                2020,          // rok dyplomu
                "I stopień",   // poziom
                "stacjonarne", // forma
                1500,          // suma liczby absolwentów
                25L            // liczba rekordów
        };
        when(graduateRepository.findBasicGraduateInfo(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<BasicGraduateInfoDTO> result = graduateService.getBasicGraduateInfo("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        BasicGraduateInfoDTO dto = result.get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(Integer.valueOf(2020), dto.getRokUkonczeniaStudiow());
        assertEquals("I stopień", dto.getStopienStudiow());
        assertEquals("stacjonarne", dto.getFormaStudiow());
        assertEquals(Integer.valueOf(1500), dto.getLiczbaAbsolwentow());
        assertEquals(25L, dto.getLiczbaRekordow());
    }

    @Test
    void testGetGraduateById_WhenExists() {
        // Given
        when(graduateRepository.findById(1L)).thenReturn(Optional.of(testGraduate));

        // When
        Graduate result = graduateService.getGraduateById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testGraduate.getId(), result.getId());
        assertEquals(testGraduate.getWojewodztwo(), result.getWojewodztwo());
        verify(graduateRepository).findById(1L);
    }

    @Test
    void testGetGraduateById_WhenNotExists() {
        // Given
        when(graduateRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Graduate result = graduateService.getGraduateById(999L);

        // Then
        assertNull(result);
        verify(graduateRepository).findById(999L);
    }

    @Test
    void testSaveGraduate() {
        // Given
        when(graduateRepository.save(testGraduate)).thenReturn(testGraduate);

        // When
        Graduate result = graduateService.saveGraduate(testGraduate);

        // Then
        assertNotNull(result);
        assertEquals(testGraduate.getId(), result.getId());
        verify(graduateRepository).save(testGraduate);
    }

    @Test
    void testDeleteGraduate() {
        // Given
        Long graduateId = 1L;

        // When
        graduateService.deleteGraduate(graduateId);

        // Then
        verify(graduateRepository).deleteById(graduateId);
    }

    @Test
    void testGetStatsByDziedzina() {
        // Given
        Object[] mockResult = {
                "nauki techniczne",
                new BigDecimal("1.35"), // wwz
                new BigDecimal("0.75"), // wwb
                new BigDecimal("88.5")  // zatrudnienie
        };
        when(graduateRepository.findStatsByDziedzina(anyString(), anyString(), any()))
                .thenReturn(Collections.singletonList(mockResult));

        // When
        List<DziedzinaStatsDTO> result = graduateService.getStatsByDziedzina("mazowieckie", null, null);

        // Then
        assertEquals(1, result.size());
        DziedzinaStatsDTO dto = result.get(0);
        assertEquals("nauki techniczne", dto.getDziedzina());
        assertEquals(new BigDecimal("1.35"), dto.getWwzTrzeciRok());
        assertEquals(new BigDecimal("0.75"), dto.getWwbTrzeciRok());
        assertEquals(new BigDecimal("88.5"), dto.getZatrudnienieTrzeciRok());
    }
}