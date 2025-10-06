package agh.matury.graduate;

import agh.matury.graduate.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class GraduateControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GraduateRepository graduateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/graduates";
        
        // Przygotuj dane testowe
        Graduate testGraduate1 = Graduate.builder()
                .wojewodztwo("mazowieckie")
                .poziom("I stopień")
                .rokDyplomu(2020)
                .forma("stacjonarne")
                .liczbaAbsolwentow(100)
                .nazwaKierunku("Informatyka")
                .nazwaUczelni("Politechnika Warszawska")
                .dziedzina("nauki techniczne")
                .wwzP1(new BigDecimal("1.15"))
                .wwzP2(new BigDecimal("1.20"))
                .wwzP3(new BigDecimal("1.25"))
                .wwzP4(new BigDecimal("1.30"))
                .wwzP5(new BigDecimal("1.35"))
                .wwbP1(new BigDecimal("0.95"))
                .wwbP2(new BigDecimal("0.90"))
                .wwbP3(new BigDecimal("0.85"))
                .wwbP4(new BigDecimal("0.80"))
                .wwbP5(new BigDecimal("0.75"))
                .czyPracaP1(new BigDecimal("75.5"))
                .czyPracaP2(new BigDecimal("80.2"))
                .czyPracaP3(new BigDecimal("85.5"))
                .czyPracaP4(new BigDecimal("88.1"))
                .czyPracaP5(new BigDecimal("90.3"))
                .eZarP1(new BigDecimal("5500.00"))
                .eZarP2(new BigDecimal("6200.00"))
                .eZarP3(new BigDecimal("6800.00"))
                .eZarP4(new BigDecimal("7200.00"))
                .eZarP5(new BigDecimal("7600.00"))
                .czasPraca(new BigDecimal("3.2"))
                .czasEtat(new BigDecimal("4.1"))
                .czasPracaDosw(new BigDecimal("2.8"))
                .czasPracaNdosw(new BigDecimal("4.5"))
                .czasEtatDosw(new BigDecimal("3.5"))
                .czasEtatNdosw(new BigDecimal("5.2"))
                .if2stP1(new BigDecimal("35.2"))
                .if2stP2(new BigDecimal("42.1"))
                .if2stP3(new BigDecimal("45.5"))
                .if2stP4(new BigDecimal("47.8"))
                .if2stP5(new BigDecimal("49.2"))
                .if2st(new BigDecimal("52.3"))
                .if2stUcz(new BigDecimal("68.3"))
                .eMiesNPracodawcow(new BigDecimal("1.2"))
                .eRocznaNKoncowetatow(new BigDecimal("0.8"))
                .koniecetatuRazNaIleLat(new BigDecimal("1.25"))
                .nCzyEtat(250)
                .build();

        Graduate testGraduate2 = Graduate.builder()
                .wojewodztwo("dolnośląskie")
                .poziom("II stopień")
                .rokDyplomu(2021)
                .forma("niestacjonarne")
                .liczbaAbsolwentow(80)
                .nazwaKierunku("Zarządzanie")
                .nazwaUczelni("Uniwersytet Wrocławski")
                .dziedzina("nauki ekonomiczne")
                .wwzP1(new BigDecimal("1.05"))
                .wwzP2(new BigDecimal("1.10"))
                .wwzP3(new BigDecimal("1.15"))
                .wwzP4(new BigDecimal("1.18"))
                .wwzP5(new BigDecimal("1.22"))
                .wwbP1(new BigDecimal("1.05"))
                .wwbP2(new BigDecimal("1.00"))
                .wwbP3(new BigDecimal("0.95"))
                .wwbP4(new BigDecimal("0.92"))
                .wwbP5(new BigDecimal("0.88"))
                .czyPracaP1(new BigDecimal("72.3"))
                .czyPracaP2(new BigDecimal("78.1"))
                .czyPracaP3(new BigDecimal("82.3"))
                .czyPracaP4(new BigDecimal("85.2"))
                .czyPracaP5(new BigDecimal("87.8"))
                .eZarP1(new BigDecimal("4800.00"))
                .eZarP2(new BigDecimal("5200.00"))
                .eZarP3(new BigDecimal("5800.00"))
                .eZarP4(new BigDecimal("6100.00"))
                .eZarP5(new BigDecimal("6400.00"))
                .czasPraca(new BigDecimal("4.1"))
                .czasEtat(new BigDecimal("5.2"))
                .czasPracaDosw(new BigDecimal("3.8"))
                .czasPracaNdosw(new BigDecimal("5.1"))
                .czasEtatDosw(new BigDecimal("4.5"))
                .czasEtatNdosw(new BigDecimal("6.2"))
                .if2stP1(new BigDecimal("0"))
                .if2stP2(new BigDecimal("0"))
                .if2stP3(new BigDecimal("0"))
                .if2stP4(new BigDecimal("0"))
                .if2stP5(new BigDecimal("0"))
                .if2st(new BigDecimal("0"))
                .if2stUcz(new BigDecimal("0"))
                .eMiesNPracodawcow(new BigDecimal("1.1"))
                .eRocznaNKoncowetatow(new BigDecimal("0.9"))
                .koniecetatuRazNaIleLat(new BigDecimal("1.11"))
                .nCzyEtat(180)
                .build();

        graduateRepository.save(testGraduate1);
        graduateRepository.save(testGraduate2);
    }

    @Test
    void testGetAllWojewodztwa() {
        // When
        ResponseEntity<List<String>> response = restTemplate.exchange(
                baseUrl + "/filtry/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("mazowieckie"));
        assertTrue(response.getBody().contains("dolnośląskie"));
    }

    @Test
    void testGetEmploymentStatsByWojewodztwo() {
        // When
        ResponseEntity<List<EmploymentStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zatrudnienie/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmploymentStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        EmploymentStatsDTO mazowieckie = response.getBody().stream()
                .filter(dto -> "mazowieckie".equals(dto.getWojewodztwo()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckie);
        assertEquals(new BigDecimal("75.5"), mazowieckie.getZatrudnieniePierwszyRok());
        assertEquals(new BigDecimal("85.5"), mazowieckie.getZatrudnienieTrzeciRok());
        assertNotNull(mazowieckie.getSrednieZatrudnienie());
    }

    @Test
    void testGetEmploymentStatsByWojewodztwo_WithFilter() {
        // When
        ResponseEntity<List<EmploymentStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zatrudnienie/wojewodztwa?wojewodztwo=mazowieckie",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmploymentStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("mazowieckie", response.getBody().get(0).getWojewodztwo());
    }

    @Test
    void testGetSalaryStatsByWojewodztwo() {
        // When
        ResponseEntity<List<SalaryStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zarobki/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SalaryStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        SalaryStatsDTO mazowieckie = response.getBody().stream()
                .filter(dto -> "mazowieckie".equals(dto.getWojewodztwo()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckie);
        assertEquals(new BigDecimal("5500.00"), mazowieckie.getZarobkiPierwszyRok());
        assertEquals(new BigDecimal("6800.00"), mazowieckie.getZarobkiTrzeciRok());
    }

    @Test
    void testGetRelativeSalaryIndexByWojewodztwo() {
        // When
        ResponseEntity<List<SalaryStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zarobki/wwz/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SalaryStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        SalaryStatsDTO mazowieckie = response.getBody().stream()
                .filter(dto -> "mazowieckie".equals(dto.getWojewodztwo()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckie);
        assertEquals(new BigDecimal("1.15"), mazowieckie.getWwzPierwszyRok());
        assertEquals(new BigDecimal("1.25"), mazowieckie.getWwzTrzeciRok());
    }

    @Test
    void testGetEmploymentMetricsByWojewodztwo() {
        // When
        ResponseEntity<List<EmploymentMetricsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zatrudnienie/pracodawcy/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmploymentMetricsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        EmploymentMetricsDTO mazowieckie = response.getBody().stream()
                .filter(dto -> "mazowieckie".equals(dto.getWojewodztwo()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckie);
        assertEquals(new BigDecimal("1.2"), mazowieckie.getSredniaMiesięcznaLiczbaPracodawcow());
        assertEquals(new BigDecimal("0.8"), mazowieckie.getSredniaNRocznychZakonczenEtatow());
        assertEquals(Long.valueOf(250), mazowieckie.getLiczbaAbsolwentowZEtatem());
    }

    @Test
    void testGetTimeToEmploymentStats() {
        // When
        ResponseEntity<List<TimeToEmploymentDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/czas-do-pracy",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TimeToEmploymentDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        TimeToEmploymentDTO dto = response.getBody().get(0);
        assertEquals("Ogółem", dto.getKategoria());
        assertNotNull(dto.getCzasDoPracyOgolem());
        assertNotNull(dto.getCzasDoEtatuOgolem());
        assertNotNull(dto.getCzasDoPracyZDoswiadczeniem());
        assertNotNull(dto.getCzasDoPracyBezDoswiadczenia());
    }

    @Test
    void testGetContinuationStudiesByWojewodztwo() {
        // When - testujemy tylko dla absolwentów I stopnia
        ResponseEntity<List<ContinuationStudiesDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/kontynuacja-studiow/wojewodztwa?poziom=I stopień",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ContinuationStudiesDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size()); // Tylko mazowieckie ma I stopień
        
        ContinuationStudiesDTO dto = response.getBody().get(0);
        assertEquals("mazowieckie", dto.getWojewodztwo());
        assertEquals(new BigDecimal("35.2"), dto.getKontynuacjaPierwszyRok());
        assertEquals(new BigDecimal("52.3"), dto.getKontynuacjaOgolem());
        assertEquals(new BigDecimal("68.3"), dto.getKontynuacjaNaTejSamejUczelni());
    }

    @Test
    void testGetBasicGraduateInfo() {
        // When
        ResponseEntity<List<BasicGraduateInfoDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/podstawowe-info",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BasicGraduateInfoDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        BasicGraduateInfoDTO mazowieckie = response.getBody().stream()
                .filter(dto -> "mazowieckie".equals(dto.getWojewodztwo()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(mazowieckie);
        assertEquals(Integer.valueOf(2020), mazowieckie.getRokUkonczeniaStudiow());
        assertEquals("I stopień", mazowieckie.getStopienStudiow());
        assertEquals("stacjonarne", mazowieckie.getFormaStudiow());
        assertEquals(Integer.valueOf(100), mazowieckie.getLiczbaAbsolwentow());
    }

    @Test
    void testGetBasicGraduateSummaryByWojewodztwo() {
        // When
        ResponseEntity<List<BasicGraduateInfoDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/podsumowanie/wojewodztwa",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BasicGraduateInfoDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        // Sprawdź czy dane są posortowane po liczbie absolwentów (DESC)
        BasicGraduateInfoDTO first = response.getBody().get(0);
        BasicGraduateInfoDTO second = response.getBody().get(1);
        
        assertTrue(first.getLiczbaAbsolwentow() >= second.getLiczbaAbsolwentow());
    }

    @Test
    void testGetStatsByDziedzina() {
        // When
        ResponseEntity<List<DziedzinaStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/dziedziny",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DziedzinaStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        DziedzinaStatsDTO naukiTechniczne = response.getBody().stream()
                .filter(dto -> "nauki techniczne".equals(dto.getDziedzina()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(naukiTechniczne);
        assertEquals(new BigDecimal("1.25"), naukiTechniczne.getWwzTrzeciRok());
        assertEquals(new BigDecimal("0.85"), naukiTechniczne.getWwbTrzeciRok());
        assertEquals(new BigDecimal("85.5"), naukiTechniczne.getZatrudnienieTrzeciRok());
    }

    @Test
    void testCreateAndRetrieveGraduate() throws Exception {
        // Given
        Graduate newGraduate = Graduate.builder()
                .wojewodztwo("śląskie")
                .poziom("I stopień")
                .rokDyplomu(2022)
                .forma("stacjonarne")
                .liczbaAbsolwentow(120)
                .nazwaKierunku("Mechanika")
                .nazwaUczelni("Politechnika Śląska")
                .dziedzina("nauki techniczne")
                .build();

        // When - Create
        ResponseEntity<Graduate> createResponse = restTemplate.postForEntity(
                baseUrl,
                newGraduate,
                Graduate.class
        );

        // Then - Create
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        Long createdId = createResponse.getBody().getId();
        assertNotNull(createdId);

        // When - Retrieve
        ResponseEntity<Graduate> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + createdId,
                Graduate.class
        );

        // Then - Retrieve
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("śląskie", getResponse.getBody().getWojewodztwo());
        assertEquals("Mechanika", getResponse.getBody().getNazwaKierunku());
    }

    @Test
    void testGetGraduateById_NotFound() {
        // When
        ResponseEntity<Graduate> response = restTemplate.getForEntity(
                baseUrl + "/999999",
                Graduate.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testComplexFiltering() {
        // When - filtruj po województwie i poziomie
        ResponseEntity<List<EmploymentStatsDTO>> response = restTemplate.exchange(
                baseUrl + "/statystyki/zatrudnienie/wojewodztwa?wojewodztwo=mazowieckie&poziom=I stopień",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmploymentStatsDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("mazowieckie", response.getBody().get(0).getWojewodztwo());
    }

    @Test
    void testErrorHandling_InvalidParameter() {
        // When - nieprawidłowy parametr
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/statystyki/zatrudnienie/wojewodztwa?rokDyplomu=invalid",
                String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}