package agh.matury.recommendation;

import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.fieldOfStudy.FieldOfStudyRepository;
import agh.matury.recommendation.similarityMatrix.SimilarityMatrixService;
import agh.matury.fieldOfStudy.FieldOfStudyService;
import agh.matury.fieldOfStudy.dto.FieldOfStudyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;

@Service
public class RecommendationService {

    private final FieldOfStudyService fieldOfStudyService;
    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final SimilarityMatrixService similarityMatrixService;

    public RecommendationService(FieldOfStudyService fieldOfStudyService, FieldOfStudyRepository fieldOfStudyRepository, SimilarityMatrixService similarityMatrixService) {
        this.fieldOfStudyService = fieldOfStudyService;
        this.fieldOfStudyRepository = fieldOfStudyRepository;
        this.similarityMatrixService = similarityMatrixService;
    }

    public List<FieldOfStudyDTO> getRecommendationsForFields(ArrayList<Long> fieldOfStudyIds, int k){
        checkForMatrixReady();
        if (fieldOfStudyIds.isEmpty() || k <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fields of study ids empty or k not positive integer");
        }
        Map<Long, Float> fieldsSimilarities = new HashMap<>();
        List<FieldOfStudy> allFields = fieldOfStudyRepository.findAll();
        for(Long fieldOfStudyId : fieldOfStudyIds) {
            if (fieldOfStudyRepository.findById(fieldOfStudyId).isPresent()) {
                for(FieldOfStudy field : allFields) {
                    if(!fieldOfStudyIds.contains(field.getId())) {
                        fieldsSimilarities.put(
                                field.getId(),
                                fieldsSimilarities.getOrDefault(field.getId(), (float) 0.0) +
                                similarityMatrixService.getSimilarity(field.getId(), fieldOfStudyId)
                        );
                    }
                }
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study not found");
            }
        }
        return getTopKFields(fieldsSimilarities, k);
    }

    public List<FieldOfStudyDTO> getRecommendationsForField(Long fieldOfStudyId, int k){
        checkForMatrixReady();
        List<FieldOfStudy> allFields = fieldOfStudyRepository.findAll();
        if (fieldOfStudyRepository.findById(fieldOfStudyId).isPresent()) {
            Map<Long, Float> fieldsSimilarities = new HashMap<>();
            for(FieldOfStudy field : allFields) {
                if(field.getId() != fieldOfStudyId) {
                    fieldsSimilarities.put(
                            field.getId(),
                            similarityMatrixService.getSimilarity(field.getId(), fieldOfStudyId)
                    );
                }
            }
            return getTopKFields(fieldsSimilarities, k);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Field of study not found");
        }
    }

    private List<FieldOfStudyDTO> getTopKFields(Map<Long, Float> fieldsSimilarities, int k) {
        List<Long> topIds = fieldsSimilarities.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
        List<FieldOfStudyDTO> topFields = new ArrayList<>();
        for (Long fieldId : topIds) {
            topFields.add(fieldOfStudyService.getFieldOfStudyById(fieldId));
        }
        return topFields;
    }

    private void checkForMatrixReady() {
        if (!similarityMatrixService.getReadyStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Similarity Matrix Not ready");
        }
    }

}
