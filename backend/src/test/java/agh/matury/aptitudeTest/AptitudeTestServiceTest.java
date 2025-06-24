package agh.matury.aptitudeTest;

import agh.matury.aptitudeTest.dto.AptitudeTestQuestionDTO;
import agh.matury.aptitudeTest.dto.AptitudeTestResponseDTO;
import agh.matury.aptitudeTest.dto.AptitudeTestResultDTO;
import agh.matury.aptitudeTest.dto.CategoryResultDTO;
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
class AptitudeTestServiceTest {

    @Mock
    private AptitudeTestQuestionRepository questionRepository;

    @Mock
    private AptitudeTestResponseRepository responseRepository;

    @InjectMocks
    private AptitudeTestService aptitudeTestService;

    private List<AptitudeTestQuestion> testQuestions;
    private AptitudeTestResponse testResponse;
    private String sessionId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID().toString();
        testQuestions = Arrays.asList(
            new AptitudeTestQuestion("Pytanie realistyczne 1", HollandCategory.REALISTIC, 1),
            new AptitudeTestQuestion("Pytanie badawcze 1", HollandCategory.INVESTIGATIVE, 2),
            new AptitudeTestQuestion("Pytanie artystyczne 1", HollandCategory.ARTISTIC, 3),
            new AptitudeTestQuestion("Pytanie społeczne 1", HollandCategory.SOCIAL, 4),
            new AptitudeTestQuestion("Pytanie przedsiębiorcze 1", HollandCategory.ENTERPRISING, 5),
            new AptitudeTestQuestion("Pytanie konwencjonalne 1", HollandCategory.CONVENTIONAL, 6)
        );
        
        testResponse = new AptitudeTestResponse(sessionId, Arrays.asList(5, 4, 3, 2, 1, 3));
        testResponse.setCompletedAt(LocalDateTime.now());
    }

    // ===== TESTY GET /questions =====
    @Test
    void getAllQuestions_ShouldReturnAllQuestionsInOrder() {
        // Given
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When
        List<AptitudeTestQuestionDTO> result = aptitudeTestService.getAllQuestions();

        // Then
        assertEquals(6, result.size());
        assertEquals("Pytanie realistyczne 1", result.get(0).questionText());
        assertEquals(HollandCategory.REALISTIC, result.get(0).category());
        assertEquals(1, result.get(0).orderNumber());
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
    }

    @Test
    void getAllQuestions_WhenNoQuestions_ShouldReturnEmptyList() {
        // Given
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(Collections.emptyList());

        // When
        List<AptitudeTestQuestionDTO> result = aptitudeTestService.getAllQuestions();

        // Then
        assertTrue(result.isEmpty());
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
    }

    // ===== TESTY POST /submit =====
    @Test
    void submitTest_WithValidAnswers_ShouldCalculateAndReturnResults() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.sessionId());
        assertNotNull(result.categoryScores());
        assertNotNull(result.categoryPercentages());
        assertNotNull(result.dominantCategories());
        assertNotNull(result.interpretation());
        assertEquals(6, result.categoryScores().size());
        
        verify(questionRepository).findAllByOrderByOrderNumberAsc();
        verify(responseRepository).save(any(AptitudeTestResponse.class));
    }

    @Test
    void submitTest_WithInvalidAnswerCount_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4); // Za mało odpowiedzi
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> aptitudeTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Liczba odpowiedzi nie odpowiada liczbie pytań\"", exception.getMessage());
        verify(responseRepository, never()).save(any());
    }

    @Test
    void submitTest_WithTooManyAnswers_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3, 4, 5); // Za dużo odpowiedzi
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> aptitudeTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Liczba odpowiedzi nie odpowiada liczbie pytań\"", exception.getMessage());
    }

    @Test
    void submitTest_WithAnswerValueTooHigh_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(6, 4, 3, 2, 1, 3); // 6 poza zakresem
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> aptitudeTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Wszystkie odpowiedzi muszą być w przedziale 1-5\"", exception.getMessage());
    }

    @Test
    void submitTest_WithAnswerValueTooLow_ShouldThrowBadRequestException() {
        // Given
        List<Integer> answers = Arrays.asList(0, 4, 3, 2, 1, 3); // 0 poza zakresem
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> aptitudeTestService.submitTest(responseDTO));
        
        assertEquals("400 BAD_REQUEST \"Wszystkie odpowiedzi muszą być w przedziale 1-5\"", exception.getMessage());
    }

    @Test
    void submitTest_WithNullAnswers_ShouldThrowException() {
        // Given
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, null);

        // When & Then
        assertThrows(NullPointerException.class, 
                    () -> aptitudeTestService.submitTest(responseDTO));
    }

    @Test
    void submitTest_WithEmptySessionId_ShouldStillProcess() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO("", answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

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
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));

        // When
        AptitudeTestResultDTO result = aptitudeTestService.getTestResult(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.sessionId());
        assertNotNull(result.categoryScores());
        verify(responseRepository).findBySessionId(sessionId);
    }

    @Test
    void getTestResult_WithInvalidSessionId_ShouldThrowNotFoundException() {
        // Given
        String invalidSessionId = "invalid-session-id";
        when(responseRepository.findBySessionId(invalidSessionId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                    () -> aptitudeTestService.getTestResult(invalidSessionId));
        
        assertEquals("404 NOT_FOUND \"Nie znaleziono wyniku testu dla podanej sesji\"", exception.getMessage());
    }

    @Test
    void getTestResult_WithNullSessionId_ShouldThrowException() {
        // When & Then
        assertThrows(Exception.class, 
                    () -> aptitudeTestService.getTestResult(null));
    }

    // ===== TESTY GET /detailed-result/{sessionId} =====
    @Test
    void getDetailedResults_WithValidSessionId_ShouldReturnDetailedResults() {
        // Given
        when(responseRepository.findBySessionId(sessionId)).thenReturn(Optional.of(testResponse));
        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));

        // When
        List<CategoryResultDTO> result = aptitudeTestService.getDetailedResults(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(6, result.size()); // 6 kategorii Hollanda
        
        CategoryResultDTO firstCategory = result.get(0);
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
                    () -> aptitudeTestService.getDetailedResults(invalidSessionId));
        
        assertEquals("404 NOT_FOUND \"Nie znaleziono wyniku testu dla podanej sesji\"", exception.getMessage());
    }

    // ===== TESTY EDGE CASES =====
    @Test
    void submitTest_WithAllMaximumAnswers_ShouldCalculateCorrectly() {
        // Given
        List<Integer> answers = Arrays.asList(5, 5, 5, 5, 5, 5);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        // Każda kategoria powinna mieć 5 punktów (1 pytanie × 5 punktów)
        result.categoryScores().values().forEach(score -> assertEquals(5, score));
    }

    @Test
    void submitTest_WithAllMinimumAnswers_ShouldCalculateCorrectly() {
        // Given
        List<Integer> answers = Arrays.asList(1, 1, 1, 1, 1, 1);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        // Każda kategoria powinna mieć 1 punkt (1 pytanie × 1 punkt)
        result.categoryScores().values().forEach(score -> assertEquals(1, score));
    }

    @Test
    void submitTest_WithVeryLongSessionId_ShouldProcess() {
        // Given
        String longSessionId = "a".repeat(1000);
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(longSessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result);
        assertEquals(longSessionId, result.sessionId());
    }

    // ===== TESTY OBLICZANIA WYNIKÓW =====
    @Test
    void calculateResults_ShouldGenerateCorrectInterpretation() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result.interpretation());
        assertTrue(result.interpretation().contains("dominujące typy osobowości zawodowej"));
        assertTrue(result.interpretation().length() > 100); // Sprawdź że interpretacja jest rozbudowana
    }

    @Test
    void calculateResults_ShouldIdentifyCorrectDominantCategories() {
        // Given
        List<Integer> answers = Arrays.asList(5, 4, 3, 2, 1, 3);
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(sessionId, answers);

        when(questionRepository.findAllByOrderByOrderNumberAsc()).thenReturn(testQuestions);
        when(questionRepository.findByCategory(any())).thenReturn(Arrays.asList(testQuestions.get(0)));
        when(responseRepository.save(any())).thenReturn(testResponse);

        // When
        AptitudeTestResultDTO result = aptitudeTestService.submitTest(responseDTO);

        // Then
        assertNotNull(result.dominantCategories());
        assertEquals(2, result.dominantCategories().size());
        // Pierwsza kategoria powinna być REALISTIC (najwyższy wynik = 5)
        assertEquals(HollandCategory.REALISTIC, result.dominantCategories().get(0));
    }
} 