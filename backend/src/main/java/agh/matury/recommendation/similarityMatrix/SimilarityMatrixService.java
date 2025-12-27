package agh.matury.recommendation.similarityMatrix;

import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.fieldOfStudy.FieldOfStudyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class SimilarityMatrixService {

    private final FieldOfStudyRepository fieldOfStudyRepository;

    private Map<Long, Map<Long, Float>> similarityMatrix = new HashMap<>();
    //    private final LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
    private final JaroWinklerSimilarity jw = new JaroWinklerSimilarity();
    private final Map<String, String> synonymMap = new HashMap<>();
    private final Map<String, Set<String>> verySimilarMap = new HashMap<>();
    private final Map<String, Set<String>> littleSimilarMap = new HashMap<>();

    private boolean readyStatus = false;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public SimilarityMatrixService(FieldOfStudyRepository fieldOfStudyRepository) {
        this.fieldOfStudyRepository = fieldOfStudyRepository;
    }

    public Float getSimilarity(Long a, Long b) {
        return similarityMatrix
                .getOrDefault(Math.min(a, b), Collections.emptyMap())
                .getOrDefault(Math.max(a, b),  (float) 0.0);
    }

    public Boolean getReadyStatus() {
        return readyStatus;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void buildMatrix() {
        System.out.println("Building Similarity Matrix...");

        List<FieldOfStudy> all = fieldOfStudyRepository.findAll();

        System.out.println("Similarity Matrix Progress = [0/" + all.size() + "]");

        Map<Long, Map<Long, Float>> matrix = new HashMap<>();

        for (FieldOfStudy f1 : all) {
            if(f1.getId() % 100 == 0) System.out.println("Similarity Matrix Progress = [" + f1.getId() + "/" + all.size() + "]");
            Map<Long, Float> row = new HashMap<>();
            for (FieldOfStudy f2 : all) {
                if (f1.getId() != f2.getId() && f1.getId() < f2.getId()) {
                    float sim = (float) getFieldsSimilarity(f1, f2);
                    row.put(f2.getId(), sim);
                }
            }
            matrix.put(f1.getId(), row);
        }

        this.similarityMatrix = matrix;

        this.readyStatus = true;
        System.out.println("Similarity Matrix Build Complete!");
    }

    private double getStringsSimilarity(String s1, String s2){
        String norm1 = normalize(s1);
        String norm2 = normalize(s2);

//        int distance = ld.apply(norm1, norm2);
//
//        double maxLen = Math.max(s1.length(), s2.length());
//        double similarity = Math.max(1 - ((double) distance / maxLen), 0);

        double similarity = jw.apply(norm1, norm2);

        String canon1 = synonymMap.getOrDefault(norm1, norm1);
        String canon2 = synonymMap.getOrDefault(norm2, norm2);

        if (verySimilarMap.containsKey(canon1) && verySimilarMap.get(canon1).contains(canon2)) {
            similarity = Math.max(similarity, 0.8);
        } else if (littleSimilarMap.containsKey(canon1) && littleSimilarMap.get(canon1).contains(canon2)) {
            similarity = Math.max(similarity, 0.5);
        }

        return similarity;
    }

    public double getFieldsSimilarity(FieldOfStudy fieldOfStudy1, FieldOfStudy fieldOfStudy2) {
        double nameSimilarity = getStringsSimilarity(fieldOfStudy1.getName(), fieldOfStudy2.getName());
        double placeSimilarity = getPlaceSimilarity(
                fieldOfStudy1.getUniversity().getLatitude(),
                fieldOfStudy1.getUniversity().getLongitude(),
                fieldOfStudy2.getUniversity().getLatitude(),
                fieldOfStudy2.getUniversity().getLongitude());
        double typeSimilarity = fieldOfStudy1.getLevel().equals(fieldOfStudy2.getLevel()) ? 1.0 : 0.0;
        double languageSimilarity = fieldOfStudy1.getLanguage().equals(fieldOfStudy2.getLanguage()) ? 1.0 : 0.0;

        return (nameSimilarity * 0.4) + (placeSimilarity * 0.4) + (typeSimilarity * 0.1) + (languageSimilarity * 0.1);
    }



    private String normalize(String input) {
        String lowerInput = input.toLowerCase();
        for (String key : synonymMap.keySet()) {
            if (lowerInput.contains(key)) {
                lowerInput = lowerInput.replace(key, synonymMap.get(key));
            }
        }
        return lowerInput;
    }


    private double getPlaceSimilarity(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance_km = EARTH_RADIUS_KM * c;
        return 1 - (distance_km / 800);
    }

    @PostConstruct
    private void loadSynonyms() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/fields-synonyms.json")) {
            if (is != null) {
                Map<String, Map<String, List<String>>> rawMap = mapper.readValue(is, new TypeReference<>() {});
                for (Map.Entry<String, Map<String, List<String>>> entry : rawMap.entrySet()) {
                    String canonical = entry.getKey().toLowerCase();
                    synonymMap.put(canonical, canonical);

                    Map<String, List<String>> details = entry.getValue();

                    List<String> synonyms = details.get("synonyms");
                    if (synonyms != null) {
                        for (String synonym : synonyms) {
                            synonymMap.put(synonym.toLowerCase(), canonical);
                        }
                    }

                    List<String> verySimilar = details.get("very_similar");
                    if (verySimilar != null) {
                        verySimilarMap.put(canonical, new HashSet<>());
                        for (String vs : verySimilar) {
                            verySimilarMap.get(canonical).add(vs.toLowerCase());
                        }
                    }

                    List<String> littleSimilar = details.get("little_similar");
                    if (littleSimilar != null) {
                        littleSimilarMap.put(canonical, new HashSet<>());
                        for (String ls : littleSimilar) {
                            littleSimilarMap.get(canonical).add(ls.toLowerCase());
                        }
                    }
                }
            } else {
                System.err.println("Nie znaleziono pliku fields-synonyms.json w resources!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
