package agh.matury.academicSkillsTest;

import agh.matury.academicSkillsTest.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcademicSkillsTestService {

    private final AcademicSkillsTestQuestionRepository questionRepository;
    private final AcademicSkillsTestResponseRepository responseRepository;

    public AcademicSkillsTestService(AcademicSkillsTestQuestionRepository questionRepository, 
                                   AcademicSkillsTestResponseRepository responseRepository) {
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
    }

    public List<AcademicSkillsTestQuestionDTO> getAllQuestions() {
        return questionRepository.findAllByOrderByOrderNumberAsc()
                .stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
    }

    public AcademicSkillsTestResultDTO submitTest(AcademicSkillsTestResponseDTO responseDTO) {
        // Walidacja odpowiedzi
        List<AcademicSkillsTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        
        if (responseDTO.answers().size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Liczba odpowiedzi nie odpowiada liczbie pytań");
        }

        // Walidacja wartości odpowiedzi (1-5)
        boolean invalidAnswers = responseDTO.answers().stream()
                .anyMatch(answer -> answer < 1 || answer > 5);
        
        if (invalidAnswers) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Wszystkie odpowiedzi muszą być w przedziale 1-5");
        }

        // Zapisz odpowiedzi
        AcademicSkillsTestResponse response = new AcademicSkillsTestResponse(
                responseDTO.sessionId(), 
                responseDTO.answers()
        );
        responseRepository.save(response);

        // Oblicz wyniki
        return calculateResults(questions, responseDTO);
    }

    public AcademicSkillsTestResultDTO getTestResult(String sessionId) {
        AcademicSkillsTestResponse response = responseRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Nie znaleziono wyniku testu dla podanej sesji"));

        List<AcademicSkillsTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        AcademicSkillsTestResponseDTO responseDTO = new AcademicSkillsTestResponseDTO(
                response.getSessionId(), 
                response.getAnswers()
        );

        return calculateResults(questions, responseDTO);
    }

    public List<AcademicSkillCategoryResultDTO> getDetailedResults(String sessionId) {
        AcademicSkillsTestResponse response = responseRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Nie znaleziono wyniku testu dla podanej sesji"));

        List<AcademicSkillsTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        Map<AcademicSkillCategory, Integer> categoryScores = calculateCategoryScores(questions, response.getAnswers());
        Map<AcademicSkillCategory, Integer> maxScores = calculateMaxScores();

        return Arrays.stream(AcademicSkillCategory.values())
                .map(category -> new AcademicSkillCategoryResultDTO(
                        category,
                        category.getDisplayName(),
                        category.getDescription(),
                        categoryScores.get(category),
                        maxScores.get(category),
                        (double) categoryScores.get(category) / maxScores.get(category) * 100,
                        category.getSuggestedFieldsOfStudy()
                ))
                .collect(Collectors.toList());
    }

    private AcademicSkillsTestResultDTO calculateResults(List<AcademicSkillsTestQuestion> questions, 
                                                       AcademicSkillsTestResponseDTO responseDTO) {
        Map<AcademicSkillCategory, Integer> categoryScores = calculateCategoryScores(questions, responseDTO.answers());
        Map<AcademicSkillCategory, Integer> maxScores = calculateMaxScores();
        Map<AcademicSkillCategory, Double> categoryPercentages = calculatePercentages(categoryScores, maxScores);
        List<AcademicSkillCategory> dominantCategories = findDominantCategories(categoryScores, 2);
        String interpretation = generateInterpretation(dominantCategories, categoryScores, maxScores);

        return new AcademicSkillsTestResultDTO(
                responseDTO.sessionId(),
                categoryScores,
                categoryPercentages,
                dominantCategories,
                interpretation
        );
    }

    private Map<AcademicSkillCategory, Integer> calculateCategoryScores(List<AcademicSkillsTestQuestion> questions, 
                                                                      List<Integer> answers) {
        Map<AcademicSkillCategory, Integer> scores = new EnumMap<>(AcademicSkillCategory.class);
        
        // Inicjalizuj wszystkie kategorie zerem
        for (AcademicSkillCategory category : AcademicSkillCategory.values()) {
            scores.put(category, 0);
        }

        // Sumuj punkty dla każdej kategorii
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            AcademicSkillCategory category = questions.get(i).getCategory();
            int currentScore = scores.get(category);
            scores.put(category, currentScore + answers.get(i));
        }

        return scores;
    }

    private Map<AcademicSkillCategory, Integer> calculateMaxScores() {
        Map<AcademicSkillCategory, Integer> maxScores = new EnumMap<>(AcademicSkillCategory.class);
        
        for (AcademicSkillCategory category : AcademicSkillCategory.values()) {
            List<AcademicSkillsTestQuestion> categoryQuestions = questionRepository.findByCategory(category);
            maxScores.put(category, categoryQuestions.size() * 5); // maksymalnie 5 punktów za pytanie
        }
        
        return maxScores;
    }

    private Map<AcademicSkillCategory, Double> calculatePercentages(Map<AcademicSkillCategory, Integer> scores, 
                                                                  Map<AcademicSkillCategory, Integer> maxScores) {
        Map<AcademicSkillCategory, Double> percentages = new EnumMap<>(AcademicSkillCategory.class);
        
        for (AcademicSkillCategory category : AcademicSkillCategory.values()) {
            double percentage = (double) scores.get(category) / maxScores.get(category) * 100;
            percentages.put(category, Math.round(percentage * 100.0) / 100.0);
        }
        
        return percentages;
    }

    private List<AcademicSkillCategory> findDominantCategories(Map<AcademicSkillCategory, Integer> scores, int topCount) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<AcademicSkillCategory, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String generateInterpretation(List<AcademicSkillCategory> dominantCategories, 
                                         Map<AcademicSkillCategory, Integer> scores, 
                                         Map<AcademicSkillCategory, Integer> maxScores) {
        if (dominantCategories.isEmpty()) {
            return "Nie udało się określić dominujących obszarów uzdolnień.";
        }

        StringBuilder interpretation = new StringBuilder();
        interpretation.append("Twoje najsilniejsze uzdolnienia kierunkowe to: ");
        
        for (int i = 0; i < dominantCategories.size(); i++) {
            AcademicSkillCategory category = dominantCategories.get(i);
            interpretation.append(category.getDisplayName());
            
            if (i < dominantCategories.size() - 1) {
                interpretation.append(" i ");
            }
        }
        
        interpretation.append(".\n\n");
        
        // Dodaj szczegółowe opisy i rekomendacje dla dominujących kategorii
        for (AcademicSkillCategory category : dominantCategories) {
            int score = scores.get(category);
            int maxScore = maxScores.get(category);
            double percentage = (double) score / maxScore * 100;
            
            interpretation.append(category.getDisplayName())
                          .append(" (").append(score).append("/").append(maxScore)
                          .append(" pkt, ").append(Math.round(percentage)).append("%): ")
                          .append(category.getDescription()).append("\n\n");
            
            interpretation.append("Sugerowane kierunki studiów: ");
            List<String> suggestions = category.getSuggestedFieldsOfStudy();
            for (int i = 0; i < suggestions.size(); i++) {
                interpretation.append(suggestions.get(i));
                if (i < suggestions.size() - 1) {
                    interpretation.append(", ");
                }
            }
            interpretation.append(".\n\n");
        }
        
        interpretation.append("Jeżeli uzyskałeś porównywalnie wysoki wynik w kilku kategoriach, ")
                      .append("możesz rozważyć kierunki interdyscyplinarne lub połączenie specjalności. ")
                      .append("Najważniejsze, by wybrany kierunek studiów odpowiadał Twoim mocnym stronom ")
                      .append("i sprawiał Ci przyjemność w nauce.");
        
        return interpretation.toString();
    }

    private AcademicSkillsTestQuestionDTO toQuestionDTO(AcademicSkillsTestQuestion question) {
        return new AcademicSkillsTestQuestionDTO(
                question.getId(),
                question.getQuestionText(),
                question.getCategory(),
                question.getOrderNumber()
        );
    }
} 