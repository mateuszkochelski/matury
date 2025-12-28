package agh.matury.recruitment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.fieldOfStudy.FieldOfStudyRepository;
import agh.matury.recruitment.config.RecruitmentFormulaConfig;
import agh.matury.recruitment.dto.AcceptanceProbabilityDTO;
@Component
public class RecruitmentProbabilityLookup {

    private static final String RESOURCE_PATH = "recruitment/lookup_table.csv";
    private static final String MAPPING_RESOURCE_PATH = "recruitment/mapping_table.csv";
    private static final String POZNAN_UNIVERSITY_ID = "politechnika-poznanska";

    private final Map<String, ProgramProbability> programsByCanonicalKey;
    private final Map<String, ProgramProbability> aliasIndex;
    private final int minSupportedPoints;
    private final int maxSupportedPoints;
    private final Map<String, String> mappingByNameAndLanguage;
    private final Map<String, String> mappingByName;
    private final FieldOfStudyRepository fieldOfStudyRepository;

    @Autowired
    public RecruitmentProbabilityLookup(FieldOfStudyRepository fieldOfStudyRepository) {
        this.fieldOfStudyRepository = fieldOfStudyRepository;
        ProbabilityIndex index = loadIndex();
        MappingIndex mappingIndex = loadMappingIndex();
        this.programsByCanonicalKey = index.programsByCanonicalKey();
        this.aliasIndex = index.aliasIndex();
        this.minSupportedPoints = index.minPoints();
        this.maxSupportedPoints = index.maxPoints();
        this.mappingByNameAndLanguage = mappingIndex.byNameAndLanguage();
        this.mappingByName = mappingIndex.byName();
    }

    public RecruitmentProbabilityLookup() {
        this.fieldOfStudyRepository = null;
        ProbabilityIndex index = loadIndex();
        MappingIndex mappingIndex = loadMappingIndex();
        this.programsByCanonicalKey = index.programsByCanonicalKey();
        this.aliasIndex = index.aliasIndex();
        this.minSupportedPoints = index.minPoints();
        this.maxSupportedPoints = index.maxPoints();
        this.mappingByNameAndLanguage = mappingIndex.byNameAndLanguage();
        this.mappingByName = mappingIndex.byName();
    }

    public AcceptanceProbabilityDTO findProbability(
            RecruitmentFormulaConfig.UniversityConfig universityConfig,
            String requestedFieldOfStudy,
            double totalPoints
    ) {
        if (universityConfig == null || !POZNAN_UNIVERSITY_ID.equalsIgnoreCase(universityConfig.id())) {
            return null;
        }

        String resolvedFieldName = resolveLookupName(universityConfig, requestedFieldOfStudy);
        String normalizedField = normalize(resolvedFieldName);
        if (normalizedField == null || normalizedField.isEmpty()) {
            return null;
        }

        ProgramProbability program = programsByCanonicalKey.get(normalizedField);
        if (program == null) {
            program = aliasIndex.get(normalizedField);
        }
        if (program == null) {
            return null;
        }

        int roundedPoints = (int) Math.round(totalPoints);
        int clampedPoints = Math.min(Math.max(roundedPoints, minSupportedPoints), maxSupportedPoints);

        ProbabilityRow row = program.byPoints().get(clampedPoints);
        if (row == null) {
            return null;
        }

        return new AcceptanceProbabilityDTO(
                row.pAccept(),
                row.pAcceptLow(),
                row.pAcceptHigh()
        );
    }

    public String resolveLookupName(
            RecruitmentFormulaConfig.UniversityConfig universityConfig,
            String requestedFieldOfStudy
    ) {
        if (universityConfig == null) {
            return resolveDatabaseName(requestedFieldOfStudy);
        }

        if (POZNAN_UNIVERSITY_ID.equalsIgnoreCase(universityConfig.id())) {
            return resolvePoznanLookupName(requestedFieldOfStudy);
        }

        return resolveDatabaseName(requestedFieldOfStudy);
    }

    private String resolvePoznanLookupName(String requestedFieldOfStudy) {
        if (requestedFieldOfStudy == null || fieldOfStudyRepository == null) {
            return requestedFieldOfStudy;
        }

        Long fieldId = parseFieldOfStudyId(requestedFieldOfStudy);
        if (fieldId == null) {
            return requestedFieldOfStudy;
        }

        Optional<FieldOfStudy> fieldOfStudy = fieldOfStudyRepository.findById(fieldId);
        if (fieldOfStudy.isEmpty()) {
            return requestedFieldOfStudy;
        }

        FieldOfStudy field = fieldOfStudy.get();
        String normalizedName = normalize(field.getName());
        if (normalizedName == null || normalizedName.isEmpty()) {
            return requestedFieldOfStudy;
        }

        String language = normalizeLanguage(field.getLanguage());
        if (language != null) {
            String mapped = mappingByNameAndLanguage.get(normalizedName + "|" + language);
            if (mapped != null) {
                return mapped;
            }
        }

        String mapped = mappingByName.get(normalizedName);
        if (mapped != null) {
            return mapped;
        }

        return field.getName() == null ? requestedFieldOfStudy : field.getName();
    }

    private String resolveDatabaseName(String requestedFieldOfStudy) {
        if (requestedFieldOfStudy == null || fieldOfStudyRepository == null) {
            return requestedFieldOfStudy;
        }

        Long fieldId = parseFieldOfStudyId(requestedFieldOfStudy);
        if (fieldId == null) {
            return requestedFieldOfStudy;
        }

        Optional<FieldOfStudy> fieldOfStudy = fieldOfStudyRepository.findById(fieldId);
        if (fieldOfStudy.isEmpty()) {
            return requestedFieldOfStudy;
        }

        String name = fieldOfStudy.get().getName();
        return name == null || name.isBlank() ? requestedFieldOfStudy : name;
    }

    private Long parseFieldOfStudyId(String fieldOfStudyId) {
        if (fieldOfStudyId == null) {
            return null;
        }
        String trimmed = fieldOfStudyId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ProbabilityIndex loadIndex() {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
        Map<String, Map<Integer, ProbabilityRow>> rowsByProgram = new HashMap<>();

        int minPoints = Integer.MAX_VALUE;
        int maxPoints = Integer.MIN_VALUE;

        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) {
                throw new IllegalStateException("Probability lookup file is empty: " + RESOURCE_PATH);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length < 5) {
                    continue;
                }

                String programName = columns[0].trim();
                int points = Integer.parseInt(columns[1].trim());
                double pAccept = Double.parseDouble(columns[2].trim());
                double pAcceptLow = Double.parseDouble(columns[3].trim());
                double pAcceptHigh = Double.parseDouble(columns[4].trim());

                rowsByProgram
                        .computeIfAbsent(programName, key -> new HashMap<>())
                        .put(points, new ProbabilityRow(pAccept, pAcceptLow, pAcceptHigh));

                minPoints = Math.min(minPoints, points);
                maxPoints = Math.max(maxPoints, points);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load probability lookup from " + RESOURCE_PATH, e);
        }

        if (rowsByProgram.isEmpty()) {
            throw new IllegalStateException("No data found in probability lookup: " + RESOURCE_PATH);
        }

        Map<String, ProgramProbability> canonical = new HashMap<>();
        Map<String, ProgramProbability> aliases = new HashMap<>();
        Set<String> conflictedAliases = new HashSet<>();

        rowsByProgram.forEach((programName, probabilities) -> {
            String canonicalKey = normalize(programName);
            if (canonicalKey == null || canonicalKey.isEmpty()) {
                return;
            }

            ProgramProbability programProbability = new ProgramProbability(programName, Map.copyOf(probabilities));
            canonical.put(canonicalKey, programProbability);

            registerAlias(canonicalKey, programProbability, aliases, conflictedAliases);
            buildAliasKeys(programName).forEach(alias ->
                    registerAlias(alias, programProbability, aliases, conflictedAliases)
            );
        });

        if (minPoints == Integer.MAX_VALUE || maxPoints == Integer.MIN_VALUE) {
            throw new IllegalStateException("Probability lookup contains no point values");
        }

        return new ProbabilityIndex(
                canonical,
                aliases,
                minPoints,
                maxPoints
        );
    }

    private MappingIndex loadMappingIndex() {
        ClassPathResource resource = new ClassPathResource(MAPPING_RESOURCE_PATH);
        Map<String, String> byNameAndLanguage = new HashMap<>();
        Map<String, String> byName = new HashMap<>();
        Set<String> nameConflicts = new HashSet<>();
        Set<String> nameLanguageConflicts = new HashSet<>();

        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length < 2) {
                    continue;
                }

                String lookupName = columns[0].trim();
                String fieldName = columns[1].trim();
                String language = columns.length > 2 ? columns[2].trim() : null;

                String normalizedField = normalize(fieldName);
                if (lookupName.isEmpty() || normalizedField == null || normalizedField.isEmpty()) {
                    continue;
                }

                if (language != null && !language.isBlank()) {
                    String normalizedLanguage = normalizeLanguage(language);
                    if (normalizedLanguage != null) {
                        registerMapping(normalizedField + "|" + normalizedLanguage, lookupName, byNameAndLanguage, nameLanguageConflicts);
                    }
                    continue;
                }

                registerMapping(normalizedField, lookupName, byName, nameConflicts);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load probability mapping from " + MAPPING_RESOURCE_PATH, e);
        }

        if (byNameAndLanguage.isEmpty() && byName.isEmpty()) {
            throw new IllegalStateException("No data found in probability mapping: " + MAPPING_RESOURCE_PATH);
        }

        return new MappingIndex(
                Map.copyOf(byNameAndLanguage),
                Map.copyOf(byName)
        );
    }

    private void registerMapping(
            String key,
            String lookupName,
            Map<String, String> mappings,
            Set<String> conflicts
    ) {
        if (key == null || key.isEmpty() || conflicts.contains(key)) {
            return;
        }

        String existing = mappings.get(key);
        if (existing == null) {
            mappings.put(key, lookupName);
            return;
        }

        if (!Objects.equals(existing, lookupName)) {
            mappings.remove(key);
            conflicts.add(key);
        }
    }

    private void registerAlias(
            String alias,
            ProgramProbability probability,
            Map<String, ProgramProbability> aliases,
            Set<String> conflicts
    ) {
        if (alias == null || alias.isEmpty() || conflicts.contains(alias)) {
            return;
        }

        ProgramProbability existing = aliases.get(alias);
        if (existing == null) {
            aliases.put(alias, probability);
            return;
        }

        if (!Objects.equals(existing.programName(), probability.programName())) {
            aliases.remove(alias);
            conflicts.add(alias);
        }
    }

    private Set<String> buildAliasKeys(String programName) {
        Set<String> aliases = new HashSet<>();

        String[] slashParts = programName.split("/");
        for (String part : slashParts) {
            addAliasCandidate(part, aliases);
        }

        for (String part : slashParts) {
            int parenIndex = part.indexOf('(');
            if (parenIndex > 0) {
                addAliasCandidate(part.substring(0, parenIndex), aliases);
            }
        }

        return aliases;
    }

    private void addAliasCandidate(String rawAlias, Set<String> aliases) {
        String normalized = normalize(rawAlias);
        if (normalized != null && !normalized.isEmpty()) {
            aliases.add(normalized);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", " ").trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String normalizeLanguage(String language) {
        String normalized = normalize(language);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }

        return switch (normalized) {
            case "pl", "polski", "polish" -> "pl";
            case "en", "english", "angielski" -> "en";
            default -> normalized;
        };
    }

    private record ProbabilityRow(double pAccept, double pAcceptLow, double pAcceptHigh) {
    }

    private record ProgramProbability(String programName, Map<Integer, ProbabilityRow> byPoints) {
    }

    private record ProbabilityIndex(
            Map<String, ProgramProbability> programsByCanonicalKey,
            Map<String, ProgramProbability> aliasIndex,
            int minPoints,
            int maxPoints
    ) {
    }

    private record MappingIndex(
            Map<String, String> byNameAndLanguage,
            Map<String, String> byName
    ) {
    }
}
