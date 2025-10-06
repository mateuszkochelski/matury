package agh.matury.graduate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GraduateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GraduateRepository graduateRepository;

    private Graduate testGraduate1;
    private Graduate testGraduate2;

    @BeforeEach
    void setUp() {
        // Przygotuj dane testowe
        testGraduate1 = Graduate.builder()
                .wojewodztwo("mazowieckie")
                .poziom("I stopień")
                .rokDyplomu(2020)
                .forma("stacjonarne")
                .liczbaAbsolwentow(100)
                .nazwaKierunku("Informatyka")
                .nazwaUczelni("Politechnika Warszawska")
                .dziedzina("nauki techniczne")
                .wwzP3(new BigDecimal("1.25"))
                .wwbP3(new BigDecimal("0.85"))
                .czyPracaP3(new BigDecimal("85.5"))
                .eZarP3(new BigDecimal("6500.00"))
                .czasPraca(new BigDecimal("3.2"))
                .czasEtat(new BigDecimal("4.1"))
                .if2stP3(new BigDecimal("45.5"))
                .if2st(new BigDecimal("52.3"))
                .eMiesNPracodawcow(new BigDecimal("1.2"))
                .eRocznaNKoncowetatow(new BigDecimal("0.8"))
                .koniecetatuRazNaIleLat(new BigDecimal("1.25"))
                .nCzyEtat(250)
                .build();

        testGraduate2 = Graduate.builder()
                .wojewodztwo("dolnośląskie")
                .poziom("II stopień")
                .rokDyplomu(2021)
                .forma("niestacjonarne")
                .liczbaAbsolwentow(80)
                .nazwaKierunku("Zarządzanie")
                .nazwaUczelni("Uniwersytet Wrocławski")
                .dziedzina("nauki ekonomiczne")
                .wwzP3(new BigDecimal("1.15"))
                .wwbP3(new BigDecimal("0.95"))
                .czyPracaP3(new BigDecimal("82.3"))
                .eZarP3(new BigDecimal("5800.00"))
                .czasPraca(new BigDecimal("4.1"))
                .czasEtat(new BigDecimal("5.2"))
                .if2stP3(new BigDecimal("0")) // II stopień nie kontynuuje
                .if2st(new BigDecimal("0"))
                .eMiesNPracodawcow(new BigDecimal("1.1"))
                .eRocznaNKoncowetatow(new BigDecimal("0.9"))
                .koniecetatuRazNaIleLat(new BigDecimal("1.11"))
                .nCzyEtat(180)
                .build();

        entityManager.persistAndFlush(testGraduate1);
        entityManager.persistAndFlush(testGraduate2);
    }

    @Test
    void testFindAllWojewodztwa() {
        // When
        List<String> wojewodztwa = graduateRepository.findAllWojewodztwa();

        // Then
        assertEquals(2, wojewodztwa.size());
        assertTrue(wojewodztwa.contains("mazowieckie"));
        assertTrue(wojewodztwa.contains("dolnośląskie"));
    }

    @Test
    void testFindAllPoziomy() {
        // When
        List<String> poziomy = graduateRepository.findAllPoziomy();

        // Then
        assertEquals(2, poziomy.size());
        assertTrue(poziomy.contains("I stopień"));
        assertTrue(poziomy.contains("II stopień"));
    }

    @Test
    void testFindAllRokiDyplomu() {
        // When
        List<Integer> lata = graduateRepository.findAllRokiDyplomu();

        // Then
        assertEquals(2, lata.size());
        assertTrue(lata.contains(2020));
        assertTrue(lata.contains(2021));
    }

    @Test
    void testFindAllDziedziny() {
        // When
        List<String> dziedziny = graduateRepository.findAllDziedziny();

        // Then
        assertEquals(2, dziedziny.size());
        assertTrue(dziedziny.contains("nauki techniczne"));
        assertTrue(dziedziny.contains("nauki ekonomiczne"));
    }

    @Test
    void testFindByWojewodztwo() {
        // When
        List<Graduate> graduates = graduateRepository.findByWojewodztwo("mazowieckie");

        // Then
        assertEquals(1, graduates.size());
        assertEquals("mazowieckie", graduates.get(0).getWojewodztwo());
        assertEquals("Informatyka", graduates.get(0).getNazwaKierunku());
    }

    @Test
    void testFindByPoziom() {
        // When
        List<Graduate> graduates = graduateRepository.findByPoziom("I stopień");

        // Then
        assertEquals(1, graduates.size());
        assertEquals("I stopień", graduates.get(0).getPoziom());
    }

    @Test
    void testFindByRokDyplomu() {
        // When
        List<Graduate> graduates = graduateRepository.findByRokDyplomu(2020);

        // Then
        assertEquals(1, graduates.size());
        assertEquals(Integer.valueOf(2020), graduates.get(0).getRokDyplomu());
    }

    @Test
    void testFindEmploymentStatsByWojewodztwo() {
        // When
        List<Object[]> results = graduateRepository.findEmploymentStatsByWojewodztwo(null, null, null);

        // Then
        assertEquals(2, results.size());
        
        // Sprawdź czy dane są pogrupowane per województwo
        Object[] mazowieckieResult = results.stream()
                .filter(r -> "mazowieckie".equals(r[0]))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckieResult);
        assertEquals("mazowieckie", mazowieckieResult[0]);
        assertNotNull(mazowieckieResult[1]); // średnie zatrudnienie P1
    }

    @Test
    void testFindEmploymentStatsByWojewodztwo_WithFilter() {
        // When
        List<Object[]> results = graduateRepository.findEmploymentStatsByWojewodztwo("mazowieckie", null, null);

        // Then
        assertEquals(1, results.size());
        assertEquals("mazowieckie", results.get(0)[0]);
    }

    @Test
    void testFindSalaryStatsByWojewodztwo() {
        // When
        List<Object[]> results = graduateRepository.findSalaryStatsByWojewodztwo(null, null, null);

        // Then
        assertEquals(2, results.size());
        
        Object[] mazowieckieResult = results.stream()
                .filter(r -> "mazowieckie".equals(r[0]))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckieResult);
        assertEquals("mazowieckie", mazowieckieResult[0]);
        assertNotNull(mazowieckieResult[1]); // średnie zarobki P1
    }

    @Test
    void testFindEmploymentMetricsByWojewodztwo() {
        // When
        List<Object[]> results = graduateRepository.findEmploymentMetricsByWojewodztwo(null, null, null);

        // Then
        assertEquals(2, results.size());
        
        Object[] result = results.get(0);
        assertEquals(13, result.length); // 13 kolumn w wyniku
        assertNotNull(result[0]); // wojewodztwo
        assertNotNull(result[1]); // średnia miesięczna liczba pracodawców
        assertNotNull(result[12]); // suma absolwentów z etatem
    }

    @Test
    void testFindTimeToEmploymentStats() {
        // When
        List<Object[]> results = graduateRepository.findTimeToEmploymentStats(null, null, null);

        // Then
        assertEquals(1, results.size());
        Object[] result = results.get(0);
        assertEquals(6, result.length); // 6 kolumn w wyniku
        assertNotNull(result[0]); // średni czas do pracy
        assertNotNull(result[1]); // średni czas do etatu
    }

    @Test
    void testFindContinuationStudiesByWojewodztwo() {
        // When - szukamy tylko absolwentów I stopnia
        List<Object[]> results = graduateRepository.findContinuationStudiesByWojewodztwo(null, "I stopień", null);

        // Then
        assertEquals(1, results.size()); // Tylko mazowieckie ma I stopień
        Object[] result = results.get(0);
        assertEquals("mazowieckie", result[0]);
        assertEquals(8, result.length); // 8 kolumn w wyniku
    }

    @Test
    void testFindBasicGraduateInfo() {
        // When
        List<Object[]> results = graduateRepository.findBasicGraduateInfo(null, null, null);

        // Then
        assertEquals(2, results.size()); // 2 rekordy
        
        Object[] result = results.get(0);
        assertEquals(6, result.length); // 6 kolumn w wyniku
        assertNotNull(result[0]); // wojewodztwo
        assertNotNull(result[1]); // rok dyplomu
        assertNotNull(result[2]); // poziom
        assertNotNull(result[3]); // forma
        assertNotNull(result[4]); // suma absolwentów
        assertNotNull(result[5]); // liczba rekordów
    }

    @Test
    void testFindBasicGraduateSummaryByWojewodztwo() {
        // When
        List<Object[]> results = graduateRepository.findBasicGraduateSummaryByWojewodztwo(null, null, null);

        // Then
        assertEquals(2, results.size()); // 2 województwa
        
        Object[] result = results.get(0);
        assertEquals(6, result.length); // 6 kolumn w wyniku
        assertNotNull(result[0]); // wojewodztwo
        assertNotNull(result[1]); // liczba rekordów
        assertNotNull(result[2]); // suma absolwentów
        assertNotNull(result[3]); // średni rok
    }

    @Test
    void testFindStatsByDziedzina() {
        // When
        List<Object[]> results = graduateRepository.findStatsByDziedzina(null, null, null);

        // Then
        assertEquals(2, results.size()); // 2 dziedziny
        
        Object[] result = results.get(0);
        assertEquals(4, result.length); // 4 kolumny w wyniku
        assertNotNull(result[0]); // dziedzina
        assertNotNull(result[1]); // wwz
        assertNotNull(result[2]); // wwb
        assertNotNull(result[3]); // zatrudnienie
    }

    @Test
    void testFindStatsByUczelnia() {
        // When
        List<Object[]> results = graduateRepository.findStatsByUczelnia(null, null, null);

        // Then
        assertEquals(2, results.size()); // 2 uczelnie
        
        Object[] result = results.get(0);
        assertEquals(5, result.length); // 5 kolumn w wyniku
        assertNotNull(result[0]); // nazwa uczelni
        assertNotNull(result[4]); // liczba absolwentów
    }

    @Test
    void testFindTopKierunkiByWwz() {
        // When
        List<Object[]> results = graduateRepository.findTopKierunkiByWwz(null, null, null, 50L);

        // Then
        assertEquals(2, results.size()); // 2 kierunki
        
        Object[] result = results.get(0);
        assertEquals(4, result.length); // 4 kolumny w wyniku
        assertNotNull(result[0]); // nazwa kierunku
        assertNotNull(result[1]); // wwz
        assertNotNull(result[2]); // zarobki
        assertNotNull(result[3]); // liczba absolwentów
    }

    @Test
    void testComplexFiltering() {
        // When - filtrujemy po województwie i poziomie
        List<Object[]> results = graduateRepository.findEmploymentStatsByWojewodztwo("mazowieckie", "I stopień", null);

        // Then
        assertEquals(1, results.size());
        assertEquals("mazowieckie", results.get(0)[0]);
    }
}