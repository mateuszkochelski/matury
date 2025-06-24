package agh.matury.aptitudeTest;

import agh.matury.aptitudeTest.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AptitudeTestService {

    private final AptitudeTestQuestionRepository questionRepository;
    private final AptitudeTestResponseRepository responseRepository;

    public AptitudeTestService(AptitudeTestQuestionRepository questionRepository, 
                              AptitudeTestResponseRepository responseRepository) {
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
    }

    public List<AptitudeTestQuestionDTO> getAllQuestions() {
        return questionRepository.findAllByOrderByOrderNumberAsc()
                .stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
    }

    public AptitudeTestResultDTO submitTest(AptitudeTestResponseDTO responseDTO) {
        // Walidacja odpowiedzi
        List<AptitudeTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        
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
        AptitudeTestResponse response = new AptitudeTestResponse(
                responseDTO.sessionId(), 
                responseDTO.answers()
        );
        responseRepository.save(response);

        // Oblicz wyniki
        return calculateResults(questions, responseDTO);
    }

    public AptitudeTestResultDTO getTestResult(String sessionId) {
        AptitudeTestResponse response = responseRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Nie znaleziono wyniku testu dla podanej sesji"));

        List<AptitudeTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        AptitudeTestResponseDTO responseDTO = new AptitudeTestResponseDTO(
                response.getSessionId(), 
                response.getAnswers()
        );

        return calculateResults(questions, responseDTO);
    }

    public List<CategoryResultDTO> getDetailedResults(String sessionId) {
        AptitudeTestResponse response = responseRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Nie znaleziono wyniku testu dla podanej sesji"));

        List<AptitudeTestQuestion> questions = questionRepository.findAllByOrderByOrderNumberAsc();
        Map<HollandCategory, Integer> categoryScores = calculateCategoryScores(questions, response.getAnswers());
        Map<HollandCategory, Integer> maxScores = calculateMaxScores();

        return Arrays.stream(HollandCategory.values())
                .map(category -> new CategoryResultDTO(
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

    private AptitudeTestResultDTO calculateResults(List<AptitudeTestQuestion> questions, 
                                                  AptitudeTestResponseDTO responseDTO) {
        Map<HollandCategory, Integer> categoryScores = calculateCategoryScores(questions, responseDTO.answers());
        Map<HollandCategory, Integer> maxScores = calculateMaxScores();
        Map<HollandCategory, Double> categoryPercentages = calculatePercentages(categoryScores, maxScores);
        List<HollandCategory> dominantCategories = findDominantCategories(categoryScores, 2);
        String interpretation = generateInterpretation(dominantCategories);

        return new AptitudeTestResultDTO(
                responseDTO.sessionId(),
                categoryScores,
                categoryPercentages,
                dominantCategories,
                interpretation
        );
    }

    private Map<HollandCategory, Integer> calculateCategoryScores(List<AptitudeTestQuestion> questions, 
                                                                 List<Integer> answers) {
        Map<HollandCategory, Integer> scores = new EnumMap<>(HollandCategory.class);
        
        // Inicjalizuj wszystkie kategorie zerem
        for (HollandCategory category : HollandCategory.values()) {
            scores.put(category, 0);
        }

        // Sumuj punkty dla każdej kategorii
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            HollandCategory category = questions.get(i).getCategory();
            int currentScore = scores.get(category);
            scores.put(category, currentScore + answers.get(i));
        }

        return scores;
    }

    private Map<HollandCategory, Integer> calculateMaxScores() {
        Map<HollandCategory, Integer> maxScores = new EnumMap<>(HollandCategory.class);
        
        for (HollandCategory category : HollandCategory.values()) {
            List<AptitudeTestQuestion> categoryQuestions = questionRepository.findByCategory(category);
            maxScores.put(category, categoryQuestions.size() * 5); // maksymalnie 5 punktów za pytanie
        }
        
        return maxScores;
    }

    private Map<HollandCategory, Double> calculatePercentages(Map<HollandCategory, Integer> scores, 
                                                             Map<HollandCategory, Integer> maxScores) {
        Map<HollandCategory, Double> percentages = new EnumMap<>(HollandCategory.class);
        
        for (HollandCategory category : HollandCategory.values()) {
            double percentage = (double) scores.get(category) / maxScores.get(category) * 100;
            percentages.put(category, Math.round(percentage * 100.0) / 100.0); // zaokrąglenie do 2 miejsc po przecinku
        }
        
        return percentages;
    }

    private List<HollandCategory> findDominantCategories(Map<HollandCategory, Integer> scores, int topCount) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<HollandCategory, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String generateInterpretation(List<HollandCategory> dominantCategories) {
        if (dominantCategories.isEmpty()) {
            return "Nie udało się określić dominującego typu osobowości zawodowej.";
        }

        StringBuilder interpretation = new StringBuilder();
        interpretation.append("Twoje dominujące typy osobowości zawodowej to: ");
        
        for (int i = 0; i < dominantCategories.size(); i++) {
            HollandCategory category = dominantCategories.get(i);
            interpretation.append(category.getDisplayName());
            
            if (i < dominantCategories.size() - 1) {
                interpretation.append(" i ");
            }
        }
        
        interpretation.append(".\n\n");
        
        // Dodaj opisy dominujących kategorii
        for (HollandCategory category : dominantCategories) {
            interpretation.append(category.getDisplayName()).append(": ")
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
        
        interpretation.append("Pamiętaj, że często 2-3 typy mogą być równie mocno rozwinięte. ")
                      .append("Rozważ połączenie swoich dominujących typów przy wyborze kierunku studiów.");
        
        return interpretation.toString();
    }

    private AptitudeTestQuestionDTO toQuestionDTO(AptitudeTestQuestion question) {
        return new AptitudeTestQuestionDTO(
                question.getId(),
                question.getQuestionText(),
                question.getCategory(),
                question.getOrderNumber()
        );
    }
} 