package agh.matury.academicSkillsTest;

import agh.matury.academicSkillsTest.dto.AcademicSkillsTestQuestionDTO;
import agh.matury.academicSkillsTest.dto.AcademicSkillsTestResponseDTO;
import agh.matury.academicSkillsTest.dto.AcademicSkillsTestResultDTO;
import agh.matury.academicSkillsTest.dto.AcademicSkillCategoryResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicSkillsTestServiceTest {

    @Mock
    private AcademicSkillsTestQuestionRepository questionRepository;

    @Mock
    private AcademicSkillsTestResponseRepository responseRepository;

    @InjectMocks
    private AcademicSkillsTestService academicSkillsTestService;

    private List<AcademicSkillsTestQuestion> testQuestions;
    private AcademicSkillsTestResponse testResponse;
    private String sessionId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID().toString();
        testQuestions = Arrays.asList(
            new AcademicSkillsTestQuestion("Pytanie matematyczne 1", AcademicSkillCategory.LOGICAL_MATHEMATICAL, 1),
            new AcademicSkillsTestQuestion("Pytanie matematyczne 2", AcademicSkillCategory.LOGICAL_MATHEMATICAL, 2),
            new AcademicSkillsTestQuestion("Pytanie językowe 1", AcademicSkillCategory.LINGUISTIC, 3),
            new AcademicSkillsTestQuestion("Pytanie językowe 2", AcademicSkillCategory.LINGUISTIC, 4),
            new AcademicSkillsTestQuestion("Pytanie artystyczne 1", AcademicSkillCategory.ARTISTIC, 5),
            new AcademicSkillsTestQuestion("Pytanie artystyczne 2", AcademicSkillCategory.ARTISTIC, 6),
            new AcademicSkillsTestQuestion("Pytanie techniczne 1", AcademicSkillCategory.TECHNICAL, 7),
            new AcademicSkillsTestQuestion("Pytanie techniczne 2", AcademicSkillCategory.TECHNICAL, 8),
            new AcademicSkillsTestQuestion("Pytanie przyrodnicze 1", AcademicSkillCategory.NATURAL_SCIENCES, 9),
            new AcademicSkillsTestQuestion("Pytanie przyrodnicze 2", AcademicSkillCategory.NATURAL_SCIENCES, 10)
        );
        
        testResponse = new AcademicSkillsTestResponse(sessionId, Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1));
        testResponse.setCompletedAt(LocalDateTime.now());
    }

    // ===== TESTY GET /questions =====
    @Test
    void getAllQuestions_ShouldReturnAllQuestionsInOrder() {
        // Given
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When
        List<AcademicSkillsTestQuestionDTO> result = academicSkillsTestService.getAllQuestions();

        // Then
        assertEquals(10, result.size());
        assertEquals("Pytanie matematyczne 1", result.get(0).questionText());
        assertEquals(AcademicSkillCategory.LOGICAL_MATHEMATICAL, result.get(0).category());
        assertEquals(1, result.get(0).orderNumber());
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
    }

    @Test
    void getAllQuestions_WhenNoQuestions_ShouldReturnEmptyList() {
        // Given
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(Collections.emptyList());

        // When
        List<AcademicSkillsTestQuestionDTO> result = academicSkillsTestService.getAllQuestions();

        // Then
        assertTrue(result.isEmpty());
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
    }

    // ===== TESTY POST /submit =====
    @Test
    void submitTest_WithValidAnswers_ShouldCalculateAndReturnResults() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.sessionId());
        assertNotNull(result.categoryScores());
        assertNotNull(result.categoryPercentages());
        assertNotNull(result.dominantCategories());
        assertNotNull(result.interpretation());
        assertEquals(5, result.categoryScores().size()); // 5 kategorii uzdolnień
        
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
        verify(responseRepository).save(any(AcademicSkillsTestResponse.class));
    }

    @Test
    void submitTest_WithInvalidAnswerCount_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4); // Za mało odpowiedzi
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Liczba odpowiedzi nie odpowiada liczbie pytań\"", exception.getMessage());
        verify(responseRepository, never()).save(any());
    }

    @Test
    void submitTest_WithTooManyAnswers_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 1, 2, 3); // Za dużo odpowiedzi
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Liczba odpowiedzi nie odpowiada liczbie pytań\"", exception.getMessage());
    }

    @Test
    void submitTest_WithAnswerValueTooHigh_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(6, 4, 3, 2, 1, 5, 4, 3, 2, 1); // 6 poza zakresem
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Wszystkie odpowiedzi muszą być w przedziale 1-5\"", exception.getMessage());
    }

    @Test
    void submitTest_WithAnswerValueTooLow_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(0, 4, 3, 2, 1, 5, 4, 3, 2, 1); // 0 poza zakresem
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Wszystkie odpowiedzi muszą być w przedziale 1-5\"", exception.getMessage());
    }

    @Test
    void submitTest_WithNullAnswers_ShouldThrowException() {
        // Given
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, null);

        // When & Then
        assertThrows(NullPointerException.class, 
                    () -> academicSkillsTestService.submitTest(responseDTO));
    }

    @Test
    void submitTest_WithEmptySessionId_ShouldStillProcess() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO("", answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        assertEquals("", result.sessionId());
    }

    // ===== TESTY GET /result/{sessionId} =====
    @Test
    void getTestResult_WithValidSessionId_ShouldReturnResult() {
        // Given
        when(responseRepository.findBySessionId(sessionId)).thenReturn(Optional.of(testResponse));
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.getTestResult(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.sessionId());
        assertNotNull(result.categoryScores());
        assertEquals(5, result.categoryScores().size());
        verify(responseRepository).findBySessionId(sessionId);
    }

    @Test
    void getTestResult_WithInvalidSessionId_ShouldThrowNotFoundException() {
        // Given
        String invalidSessionId = "invalid-session-id";
        when(responseRepository.findBySessionId(invalidSessionId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.getTestResult(invalidSessionId));
        
        assertEquals("404 NOT_FOUND \"Nie znaleziono wyniku testu dla podanej sesji\"", exception.getMessage());
    }

    @Test
    void getTestResult_WithNullSessionId_ShouldThrowException() {
        // When & Then
        assertThrows(Exception.class, 
                    () -> academicSkillsTestService.getTestResult(null));
    }

    // ===== TESTY GET /detailed-result/{sessionId} =====
    @Test
    void getDetailedResults_WithValidSessionId_ShouldReturnDetailedResults() {
        // Given
        when(responseRepository.findBySessionId(sessionId)).thenReturn(Optional.of(testResponse));
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));

        // When
        List<AcademicSkillCategoryResultDTO> result = academicSkillsTestService.getDetailedResults(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size()); // 5 kategorii uzdolnień
        
        AcademicSkillCategoryResultDTO firstCategory = result.get(0);
        assertNotNull(firstCategory.category());
        assertNotNull(firstCategory.displayName());
        assertNotNull(firstCategory.description());
        assertTrue(firstCategory.score() >= 0);
        assertTrue(firstCategory.maxScore() > 0);
        assertTrue(firstCategory.percentage() >= 0 && firstCategory.percentage() <= 100);
        assertNotNull(firstCategory.suggestedFieldsOfStudy());
        
        verify(responseRepository).findBySessionId(sessionId);
    }

    @Test
    void getDetailedResults_WithInvalidSessionId_ShouldThrowNotFoundException() {
        // Given
        String invalidSessionId = "invalid-session-id";
        when(responseRepository.findBySessionId(invalidSessionId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> academicSkillsTestService.getDetailedResults(invalidSessionId));
        
        assertEquals("404 NOT_FOUND \"Nie znaleziono wyniku testu dla podanej sesji\"", exception.getMessage());
    }

    // ===== TESTY EDGE CASES =====
    @Test
    void submitTest_WithAllMaximumAnswers_ShouldCalculateCorrectly() {
        // Given
        List<Integer> answers = Arrays.asList(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        // Każda kategoria powinna mieć 10 punktów (2 pytania × 5 punktów)
        result.categoryScores().values().forEach(score -> assertEquals(10, score));
        result.categoryPercentages().values().forEach(percentage -> assertEquals(100.0, percentage));
    }

    @Test
    void submitTest_WithAllMinimumAnswers_ShouldCalculateCorrectly() {
        // Given
        List<Integer> answers = Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        // Każda kategoria powinna mieć 2 punkty (2 pytania × 1 punkt)
        result.categoryScores().values().forEach(score -> assertEquals(2, score));
        result.categoryPercentages().values().forEach(percentage -> assertEquals(20.0, percentage));
    }

    @Test
    void submitTest_WithVeryLongSessionId_ShouldProcess() {
        // Given
        String longSessionId = "a".repeat(1000);
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(longSessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(longSessionId, result.sessionId());
    }

    // ===== TESTY OBLICZANIA WYNIKÓW =====
    @Test
    void calculateResults_ShouldGenerateCorrectInterpretation() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result.interpretation());
        assertTrue(result.interpretation().contains("najsilniejsze uzdolnienia kierunkowe"));
        assertTrue(result.interpretation().length() > 100); // Sprawdź że interpretacja jest rozbudowana
        assertTrue(result.interpretation().contains("pkt")); // Powinny być wyniki punktowe
    }

    @Test
    void calculateResults_ShouldIdentifyCorrectDominantCategories() {
        // Given
        List<Integer> answers = Arrays.asList(5, 5, 4, 4, 3, 3, 2, 2, 1, 1); // LOGICAL_MATHEMATICAL i LINGUISTIC najwyższe
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result.dominantCategories());
        assertEquals(2, result.dominantCategories().size());
        // Pierwsza kategoria powinna być LOGICAL_MATHEMATICAL (najwyższy wynik = 10)
        assertEquals(AcademicSkillCategory.LOGICAL_MATHEMATICAL, result.dominantCategories().get(0));
        // Druga kategoria powinna być LINGUISTIC (drugi najwyższy wynik = 8)  
        assertEquals(AcademicSkillCategory.LINGUISTIC, result.dominantCategories().get(1));
    }

    @Test
    void calculateResults_ShouldCalculateCorrectPercentages() {
        // Given
        List<Integer> answers = Arrays.asList(5, 3, 4, 2, 1, 1, 2, 3, 4, 5); // Różne wyniki dla kategorii
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0), testQuestions.get(1)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result.categoryPercentages());
        // LOGICAL_MATHEMATICAL: (5+3)=8, max=10, percentage=80%
        assertEquals(80.0, result.categoryPercentages().get(AcademicSkillCategory.LOGICAL_MATHEMATICAL));
        // LINGUISTIC: (4+2)=6, max=10, percentage=60%
        assertEquals(60.0, result.categoryPercentages().get(AcademicSkillCategory.LINGUISTIC));
    }

    // ===== TESTY WYDAJNOŚCI =====
    @Test
    void submitTest_WithManyQuestions_ShouldProcessEfficiently() {
        // Given
        List<AcademicSkillsTestQuestion> manyQuestions = new ArrayList<>();
        List<Integer> manyAnswers = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            manyQuestions.add(new AcademicSkillsTestQuestion("Pytanie " + i, 
                AcademicSkillCategory.LOGICAL_MATHEMATICAL, i + 1));
            manyAnswers.add(3); // średnia odpowiedź
        }
        
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(sessionId, manyAnswers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(manyQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(manyQuestions.subList(0, 20));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        long startTime = System.currentTimeMillis();
        AcademicSkillsTestResultDTO result = academicSkillsTestService.submitTest(responseDTO);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertTrue(endTime - startTime < 1000); // Powinno zakończyć się w mniej niż sekundę
    }
} 