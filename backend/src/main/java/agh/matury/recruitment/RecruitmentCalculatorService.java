package agh.matury.recruitment;

import agh.matury.recruitment.config.RecruitmentConfigLoader;
import agh.matury.recruitment.config.RecruitmentFormulaConfig;
import agh.matury.recruitment.dto.AcceptanceProbabilityDTO;
import agh.matury.recruitment.dto.CalculateRecruitmentPointsRequest;
import agh.matury.recruitment.dto.ExamResultDTO;
import agh.matury.recruitment.dto.RecruitmentCalculationResponse;
import agh.matury.recruitment.dto.TermBreakdownDTO;
import agh.matury.recruitment.exception.RecruitmentCalculationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecruitmentCalculatorService {

    private final RecruitmentFormulaConfig config;
    private final List<RecruitmentFormulaConfig.UniversityConfig> universities;
    private final Map<String, RecruitmentFormulaConfig.SubjectGroupConfig> subjectGroupsById;
    private final RecruitmentProbabilityLookup probabilityLookup;

    @Autowired
    public RecruitmentCalculatorService(
            RecruitmentConfigLoader configLoader,
            RecruitmentProbabilityLookup probabilityLookup
    ) {
        this.config = configLoader.getConfig();
        this.universities = Optional.ofNullable(config.universities()).orElseGet(List::of);
        List<RecruitmentFormulaConfig.SubjectGroupConfig> groups = Optional.ofNullable(config.subjectGroups()).orElseGet(List::of);
        this.subjectGroupsById = groups.stream()
                .collect(Collectors.toMap(
                        group -> group.id().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
        this.probabilityLookup = probabilityLookup;
    }

    public RecruitmentCalculatorService(RecruitmentConfigLoader configLoader) {
        this(configLoader, new RecruitmentProbabilityLookup());
    }

    public RecruitmentCalculationResponse calculatePoints(CalculateRecruitmentPointsRequest request) {
        validateRequest(request);

        RecruitmentFormulaConfig.UniversityConfig universityConfig = findUniversityConfig(request.universityId());
        String resolvedFieldOfStudy = probabilityLookup.resolveLookupName(universityConfig, request.fieldOfStudyId());
        RecruitmentFormulaConfig.FieldOfStudyConfig fieldOfStudyConfig = findFieldConfig(universityConfig, resolvedFieldOfStudy);
        RecruitmentFormulaConfig.FormulaConfig formulaConfig = fieldOfStudyConfig.formula();

        List<TermBreakdownDTO> breakdown = new ArrayList<>();
        double total = 0.0;

        for (RecruitmentFormulaConfig.TermConfig term : formulaConfig.terms()) {
            TermEvaluation evaluation = evaluateTerm(term, request.examResults());

            if (evaluation.status == TermStatus.MISSING) {
                Double defaultPoints = term.defaultPoints();
                if (defaultPoints != null) {
                    TermEvaluation fallbackEvaluation = TermEvaluation.defaulted(term.id(), defaultPoints);
                    total += fallbackEvaluation.points();
                    breakdown.add(new TermBreakdownDTO(
                            term.id(),
                            term.description(),
                            fallbackEvaluation.subjectCode(),
                            fallbackEvaluation.level(),
                            fallbackEvaluation.rawScore(),
                            fallbackEvaluation.coefficient(),
                            fallbackEvaluation.points()
                    ));
                    continue;
                }

                if (term.failIfMissingOrDefault()) {
                    throw new RecruitmentCalculationException("Missing result for required term: " + term.description());
                }

                if (term.fallbackToZeroOrDefault()) {
                    breakdown.add(new TermBreakdownDTO(
                            term.id(),
                            term.description(),
                            null,
                            null,
                            0.0,
                            0.0,
                            0.0
                    ));
                    continue;
                }

                // Missing result without fallback and not required -> treat as zero but highlight.
                breakdown.add(new TermBreakdownDTO(
                        term.id(),
                        term.description(),
                        null,
                        null,
                        0.0,
                        0.0,
                        0.0
                ));
                continue;
            }

            total += evaluation.points();
            breakdown.add(new TermBreakdownDTO(
                    term.id(),
                    term.description(),
                    evaluation.subjectCode(),
                    evaluation.level(),
                    evaluation.rawScore(),
                    evaluation.coefficient(),
                    evaluation.points()
            ));
        }

        AcceptanceProbabilityDTO acceptanceProbability = probabilityLookup.findProbability(
                universityConfig,
                resolvedFieldOfStudy,
                total
        );

        return new RecruitmentCalculationResponse(
                universityConfig.id(),
                fieldOfStudyConfig.id(),
                total,
                breakdown,
                acceptanceProbability == null ? null : acceptanceProbability.probability()
        );
    }

    private void validateRequest(CalculateRecruitmentPointsRequest request) {
        if (request == null) {
            throw new RecruitmentCalculationException("Request body cannot be null");
        }
        if (isBlank(request.universityId())) {
            throw new RecruitmentCalculationException("Missing university identifier");
        }
        if (isBlank(request.fieldOfStudyId())) {
            throw new RecruitmentCalculationException("Missing field of study identifier");
        }
        if (CollectionUtils.isEmpty(request.examResults())) {
            throw new RecruitmentCalculationException("At least one exam result is required");
        }

        boolean invalidScore = request.examResults().stream()
                .anyMatch(result -> result.score() == null || result.score() < 0);
        if (invalidScore) {
            throw new RecruitmentCalculationException("Exam scores must be provided and greater or equal to zero");
        }
    }

    private RecruitmentFormulaConfig.UniversityConfig findUniversityConfig(String universityId) {
        String normalized = universityId.trim().toLowerCase(Locale.ROOT);
        return universities.stream()
                .filter(university -> matchesIdentifier(normalized, university.id(), university.aliases()))
                .findFirst()
                .orElseThrow(() -> new RecruitmentCalculationException("Unknown university: " + universityId));
    }

    private RecruitmentFormulaConfig.FieldOfStudyConfig findFieldConfig(
            RecruitmentFormulaConfig.UniversityConfig universityConfig,
            String fieldOfStudyId
    ) {
        String normalized = fieldOfStudyId.trim().toLowerCase(Locale.ROOT);
        return universityConfig.fieldsOfStudy().stream()
                .filter(field -> matchesIdentifier(normalized, field.id(), field.aliases()))
                .findFirst()
                .orElseThrow(() -> new RecruitmentCalculationException(
                        "Unknown field of study '" + fieldOfStudyId + "' for university '" + universityConfig.name() + "'"
                ));
    }

    private boolean matchesIdentifier(String normalizedId, String primaryId, List<String> aliases) {
        if (primaryId != null && normalizedId.equals(primaryId.toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (aliases == null) {
            return false;
        }
        return aliases.stream()
                .filter(Objects::nonNull)
                .map(alias -> alias.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedId::equals);
    }

    private TermEvaluation evaluateTerm(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        String termType = Optional.ofNullable(term.type()).map(String::toUpperCase).orElse("");
        return switch (termType) {
            case "SPECIFIC_SUBJECT" -> evaluateSpecificSubject(term, examResults);
            case "BEST_OF_GROUPS" -> evaluateBestOfGroups(term, examResults);
            case "LANGUAGE_GROUP" -> evaluateLanguageGroup(term, examResults);
            case "LANGUAGE_MULTI_LEVEL" -> evaluateLanguageMultiLevel(term, examResults);
            case "POZNAN_COMPOSITE_X" -> evaluatePoznanCompositeX(term, examResults);
            case "SUM_OF_GROUPS" -> evaluateSumOfGroups(term, examResults);
            case "WEIGHTED_SUBJECT" -> evaluateWeightedSubject(term, examResults);
            case "LUBLIN_SUBJECT" -> evaluateLublinSubject(term, examResults);
            default -> throw new RecruitmentCalculationException("Unsupported term type: " + term.type());
        };
    }

    private TermEvaluation evaluateSpecificSubject(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        if (isBlank(term.subjectCode())) {
            throw new RecruitmentCalculationException("SPECIFIC_SUBJECT term requires subjectCode");
        }

        String targetSubject = term.subjectCode().trim().toLowerCase(Locale.ROOT);
        String requiredLevel = term.requiredLevel() == null ? null : term.requiredLevel().trim().toUpperCase(Locale.ROOT);

        List<ExamResultDTO> subjectResults = examResults.stream()
                .filter(result -> targetSubject.equals(result.normalizedSubjectCode()))
                .toList();

        if (subjectResults.isEmpty()) {
            return TermEvaluation.missing(term.id());
        }

        if (requiredLevel == null) {
            ExamResultDTO best = subjectResults.stream()
                    .filter(result -> result.score() != null)
                    .max(Comparator.comparingDouble(ExamResultDTO::score))
                    .orElse(null);
            if (best == null) {
                return TermEvaluation.missing(term.id());
            }
            double coefficient = term.coefficientOrDefault(best.normalizedLevel());
            return TermEvaluation.evaluated(
                    term.id(),
                    best.normalizedSubjectCode(),
                    best.normalizedLevel(),
                    best.score(),
                    coefficient
            );
        }

        double bestPoints = Double.NEGATIVE_INFINITY;
        TermEvaluation bestEvaluation = null;

        boolean basicLevelRequested = "BASIC".equalsIgnoreCase(requiredLevel);
        boolean exactLevelAvailable = subjectResults.stream()
                .anyMatch(result -> requiredLevel.equals(result.normalizedLevel()));

        for (ExamResultDTO result : subjectResults) {
            if (result.score() == null) {
                continue;
            }

            String normalizedLevel = result.normalizedLevel();
            double rawScore;
            double coefficient;
            String levelLabel;

            if (requiredLevel.equals(normalizedLevel)) {
                rawScore = result.score();
                coefficient = term.coefficientOrDefault(normalizedLevel);
                levelLabel = normalizedLevel;
            } else if (basicLevelRequested
                    && !exactLevelAvailable
                    && term.allowExtendedToBasicConversionOrDefault()
                    && "EXTENDED".equals(normalizedLevel)) {
                rawScore = convertExtendedToBasic(result.score());
                coefficient = term.coefficientOrDefault(requiredLevel);
                levelLabel = "BASIC_FROM_EXTENDED";
            } else {
                continue;
            }

            double points = rawScore * coefficient;
            if (points > bestPoints) {
                bestPoints = points;
                bestEvaluation = TermEvaluation.evaluated(
                        term.id(),
                        result.normalizedSubjectCode(),
                        levelLabel,
                        rawScore,
                        coefficient
                );
            }
        }

        return bestEvaluation != null ? bestEvaluation : TermEvaluation.missing(term.id());
    }

    private TermEvaluation evaluateBestOfGroups(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        if (CollectionUtils.isEmpty(term.allowedGroupIds())) {
            throw new RecruitmentCalculationException("BEST_OF_GROUPS term requires allowedGroupIds");
        }

        Set<String> allowedSubjects = collectSubjectCodes(term.allowedGroupIds());
        Set<String> allowedLevels = new HashSet<>();

        for (String groupId : term.allowedGroupIds()) {
            RecruitmentFormulaConfig.SubjectGroupConfig groupConfig = subjectGroupsById.get(groupId.toLowerCase(Locale.ROOT));
            if (groupConfig != null) {
                allowedLevels.addAll(
                        groupConfig.allowedLevels().stream()
                                .filter(Objects::nonNull)
                                .map(level -> level.toUpperCase(Locale.ROOT))
                                .toList()
                );
            }
        }

        return examResults.stream()
                .filter(result -> {
                    String normalizedLevel = result.normalizedLevel();
                    String normalizedSubject = result.normalizedSubjectCode();
                    if (normalizedLevel == null || normalizedSubject == null) {
                        return false;
                    }

                    boolean vocationalLevel = "VOCATIONAL_TECHNICIAN".equals(normalizedLevel);
                    boolean levelAllowed = allowedLevels.isEmpty()
                            || allowedLevels.contains(normalizedLevel)
                            || (vocationalLevel && term.allowVocationalTechnicianOrDefault());
                    boolean subjectAllowed = allowedSubjects.contains(normalizedSubject);
                    boolean vocationalAllowed = vocationalLevel && term.allowVocationalTechnicianOrDefault();

                    return levelAllowed && (subjectAllowed || vocationalAllowed);
                })
                .map(result -> toEvaluation(term, result))
                .max(Comparator.comparingDouble(TermEvaluation::points))
                .orElseGet(() -> TermEvaluation.missing(term.id()));
    }

    private TermEvaluation evaluateLanguageGroup(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        Set<String> allowedSubjects = collectSubjectCodes(term.allowedGroupIds());

        if (allowedSubjects.isEmpty()) {
            throw new RecruitmentCalculationException("LANGUAGE_GROUP term requires allowed subject groups");
        }

        double bestPoints = Double.NEGATIVE_INFINITY;
        TermEvaluation bestEvaluation = null;

        for (String subjectCode : allowedSubjects) {
            List<ExamResultDTO> subjectResults = examResults.stream()
                    .filter(result -> subjectCode.equals(result.normalizedSubjectCode()))
                    .toList();

            ScoreOption bestBasic = findBestBasicScore(
                    subjectResults,
                    term.allowExtendedToBasicConversionOrDefault(),
                    term.normalizeBilingualScoresOrDefault()
            );

            if (bestBasic == null) {
                continue;
            }

            String coefficientLevel = bestBasic.level();
            double coefficient;
            try {
                coefficient = term.coefficientOrDefault(coefficientLevel);
            } catch (IllegalStateException ex) {
                coefficient = term.coefficientOrDefault("BASIC");
            }
            double points = bestBasic.score() * coefficient;

            if (points > bestPoints) {
                bestPoints = points;
                bestEvaluation = TermEvaluation.evaluated(
                        term.id(),
                        subjectCode,
                        coefficientLevel,
                        bestBasic.score(),
                        coefficient
                );
            }
        }

        return bestEvaluation != null ? bestEvaluation : TermEvaluation.missing(term.id());
    }

    private TermEvaluation evaluateLublinSubject(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        Set<String> allowedSubjects = new HashSet<>();
        allowedSubjects.addAll(collectSubjectCodes(term.allowedGroupIds()));
        if (!isBlank(term.subjectCode())) {
            allowedSubjects.add(term.subjectCode().trim().toLowerCase(Locale.ROOT));
        }

        if (allowedSubjects.isEmpty()) {
            throw new RecruitmentCalculationException("LUBLIN_SUBJECT term requires subject code or subject group");
        }

        double bestPoints = Double.NEGATIVE_INFINITY;
        TermEvaluation bestEvaluation = null;

        for (String subjectCode : allowedSubjects) {
            List<ExamResultDTO> subjectResults = examResults.stream()
                    .filter(result -> subjectCode.equals(result.normalizedSubjectCode()))
                    .toList();

            double rawScore = calculateLublinSubjectScore(subjectResults);
            if (rawScore <= 0.0) {
                continue;
            }

            double coefficient = term.coefficientOrDefault("BASIC");
            double points = rawScore * coefficient;

            if (points > bestPoints) {
                bestPoints = points;
                bestEvaluation = TermEvaluation.evaluated(
                        term.id(),
                        subjectCode,
                        "LUBLIN_AGGREGATED",
                        rawScore,
                        coefficient
                );
            }
        }

        return bestEvaluation != null ? bestEvaluation : TermEvaluation.missing(term.id());
    }

    private TermEvaluation evaluateLanguageMultiLevel(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        Set<String> allowedSubjects = collectSubjectCodes(term.allowedGroupIds());
        if (allowedSubjects.isEmpty()) {
            throw new RecruitmentCalculationException("LANGUAGE_MULTI_LEVEL term requires allowed subject groups");
        }

        double bestPoints = Double.NEGATIVE_INFINITY;
        TermEvaluation bestEvaluation = null;
        double bilingualMultiplier = term.bilingualMultiplierOrDefault();

        for (String subjectCode : allowedSubjects) {
            List<ExamResultDTO> subjectResults = examResults.stream()
                    .filter(result -> subjectCode.equals(result.normalizedSubjectCode()))
                    .toList();

            if (subjectResults.isEmpty()) {
                continue;
            }

            double basicScore = subjectResults.stream()
                    .filter(result -> "BASIC".equals(result.normalizedLevel()))
                    .mapToDouble(ExamResultDTO::score)
                    .max()
                    .orElse(0.0);

            double extendedScore = subjectResults.stream()
                    .filter(result -> "EXTENDED".equals(result.normalizedLevel()))
                    .mapToDouble(ExamResultDTO::score)
                    .max()
                    .orElse(0.0);

            double bilingualScore = subjectResults.stream()
                    .filter(result -> "BILINGUAL".equals(result.normalizedLevel()))
                    .mapToDouble(ExamResultDTO::score)
                    .max()
                    .orElse(0.0);

            double rawScore = basicScore + extendedScore + bilingualMultiplier * bilingualScore;
            if (rawScore == 0.0) {
                continue;
            }

            double coefficient = term.coefficientOrDefault("BASIC");
            double points = rawScore * coefficient;

            if (points > bestPoints) {
                bestPoints = points;
                bestEvaluation = TermEvaluation.evaluated(
                        term.id(),
                        subjectCode,
                        "AGGREGATED",
                        rawScore,
                        coefficient
                );
            }
        }

        return bestEvaluation != null ? bestEvaluation : TermEvaluation.missing(term.id());
    }

    private TermEvaluation evaluatePoznanCompositeX(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        Set<String> allowedSubjects = collectSubjectCodes(term.allowedGroupIds());

        double bestCandidateRaw = Double.NEGATIVE_INFINITY;
        String bestSubject = null;
        String levelLabel = null;

        for (String subjectCode : allowedSubjects) {
            List<ExamResultDTO> subjectResults = examResults.stream()
                    .filter(result -> subjectCode.equals(result.normalizedSubjectCode()))
                    .toList();

            if (subjectResults.isEmpty()) {
                continue;
            }

            ScoreOption base = findBestBasicScore(
                    subjectResults,
                    term.allowExtendedToBasicConversionOrDefault(),
                    false
            );
            ScoreOption extended = findBestExtendedScore(subjectResults);

            double baseScore = base == null ? 0.0 : base.score();
            double extendedScore = extended == null ? 0.0 : extended.score();
            double combined = baseScore + extendedScore;

            if (combined > bestCandidateRaw) {
                bestCandidateRaw = combined;
                bestSubject = subjectCode;
                levelLabel = "COMBINED_SUBJECT";
            }
        }

        if (term.includeVocationalResultsOrDefault()) {
            List<String> vocationalSubjects = term.vocationalSubjectCodes();
            double multiplier = term.vocationalMultiplierOrDefault();

            for (ExamResultDTO result : examResults) {
                if (result.score() == null) {
                    continue;
                }
                String normalizedSubject = result.normalizedSubjectCode();
                String normalizedLevel = result.normalizedLevel();

                boolean subjectAllowed = vocationalSubjects.isEmpty()
                        || (normalizedSubject != null && vocationalSubjects.contains(normalizedSubject));

                if (!subjectAllowed || !"VOCATIONAL_TECHNICIAN".equals(normalizedLevel)) {
                    continue;
                }

                double candidate = multiplier * result.score();
                if (candidate > bestCandidateRaw) {
                    bestCandidateRaw = candidate;
                    bestSubject = normalizedSubject;
                    levelLabel = "VOCATIONAL";
                }
            }
        }

        if (bestCandidateRaw == Double.NEGATIVE_INFINITY) {
            return TermEvaluation.missing(term.id());
        }

        double coefficient = term.coefficientOrDefault("BASIC");
        return TermEvaluation.evaluated(
                term.id(),
                bestSubject,
                levelLabel,
                bestCandidateRaw,
                coefficient
        );
    }

    private TermEvaluation evaluateWeightedSubject(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        Map<String, Double> subjectWeights = term.subjectWeights();
        if (subjectWeights.isEmpty()) {
            throw new RecruitmentCalculationException("WEIGHTED_SUBJECT term requires subjectWeights");
        }

        String defaultRequiredLevel = term.requiredLevel() == null
                ? null
                : term.requiredLevel().trim().toUpperCase(Locale.ROOT);

        double bestPoints = Double.NEGATIVE_INFINITY;
        TermEvaluation bestEvaluation = null;

        for (Map.Entry<String, Double> entry : subjectWeights.entrySet()) {
            String subject = entry.getKey();
            double coefficient = entry.getValue();
            if (subject == null) {
                continue;
            }

            String normalizedSubject;
            String entryLevel = null;
            String rawSubject = subject.trim();
            int separatorIdx = rawSubject.indexOf('|');
            if (separatorIdx >= 0) {
                normalizedSubject = rawSubject.substring(0, separatorIdx).trim().toLowerCase(Locale.ROOT);
                entryLevel = rawSubject.substring(separatorIdx + 1).trim();
            } else {
                normalizedSubject = rawSubject.toLowerCase(Locale.ROOT);
            }

            String normalizedEntryLevel = entryLevel == null || entryLevel.isEmpty()
                    ? null
                    : entryLevel.toUpperCase(Locale.ROOT);

            for (ExamResultDTO result : examResults) {
                if (result.score() == null) {
                    continue;
                }
                if (!normalizedSubject.equals(result.normalizedSubjectCode())) {
                    continue;
                }

                String effectiveRequiredLevel = normalizedEntryLevel != null
                        ? normalizedEntryLevel
                        : defaultRequiredLevel;

                if (effectiveRequiredLevel != null && !effectiveRequiredLevel.equals(result.normalizedLevel())) {
                    continue;
                }

                double rawScore = result.score();
                double points = rawScore * coefficient;
                if (points > bestPoints) {
                    bestPoints = points;
                    bestEvaluation = TermEvaluation.evaluated(
                            term.id(),
                            result.normalizedSubjectCode(),
                            result.normalizedLevel(),
                            rawScore,
                            coefficient
                    );
                }
            }
        }

        return bestEvaluation != null ? bestEvaluation : TermEvaluation.missing(term.id());
    }

    private TermEvaluation evaluateSumOfGroups(RecruitmentFormulaConfig.TermConfig term, List<ExamResultDTO> examResults) {
        if (CollectionUtils.isEmpty(term.allowedGroupIds())) {
            throw new RecruitmentCalculationException("SUM_OF_GROUPS term requires allowedGroupIds");
        }

        Set<String> allowedSubjects = collectSubjectCodes(term.allowedGroupIds());
        String requiredLevel = term.requiredLevel() == null ? null : term.requiredLevel().trim().toUpperCase(Locale.ROOT);
        int selections = term.maxSubjectsOrDefault();

        List<ExamResultDTO> matching = examResults.stream()
                .filter(result -> result.score() != null)
                .filter(result -> {
                    String subject = result.normalizedSubjectCode();
                    if (subject == null || !allowedSubjects.contains(subject)) {
                        return false;
                    }
                    if (requiredLevel == null) {
                        return true;
                    }
                    return requiredLevel.equals(result.normalizedLevel());
                })
                .sorted(Comparator.comparingDouble((ExamResultDTO result) -> Optional.ofNullable(result.score()).orElse(0.0)).reversed())
                .toList();

        if (matching.isEmpty()) {
            return TermEvaluation.missing(term.id());
        }

        List<ExamResultDTO> selected = matching.subList(0, Math.min(selections, matching.size()));
        double rawSum = selected.stream()
                .mapToDouble(ExamResultDTO::score)
                .sum();
        String subjectLabel = selected.stream()
                .map(ExamResultDTO::normalizedSubjectCode)
                .collect(Collectors.joining("+"));

        double coefficient = term.coefficientOrDefault(requiredLevel);
        String levelLabel = requiredLevel == null ? "AGGREGATED" : requiredLevel + "_AGGREGATED";

        return TermEvaluation.evaluated(
                term.id(),
                subjectLabel,
                levelLabel,
                rawSum,
                coefficient
        );
    }

    private double calculateLublinSubjectScore(List<ExamResultDTO> results) {
        if (results.isEmpty()) {
            return 0.0;
        }

        double basicScore = results.stream()
                .filter(result -> "BASIC".equals(result.normalizedLevel()))
                .filter(result -> result.score() != null)
                .mapToDouble(ExamResultDTO::score)
                .max()
                .orElse(Double.NEGATIVE_INFINITY);

        double extendedScore = results.stream()
                .filter(result -> {
                    String level = result.normalizedLevel();
                    return "EXTENDED".equals(level) || "BILINGUAL".equals(level);
                })
                .filter(result -> result.score() != null)
                .mapToDouble(ExamResultDTO::score)
                .max()
                .orElse(Double.NEGATIVE_INFINITY);

        boolean hasBasic = basicScore != Double.NEGATIVE_INFINITY;
        boolean hasExtended = extendedScore != Double.NEGATIVE_INFINITY;

        if (hasBasic && hasExtended) {
            return basicScore + extendedScore;
        }
        if (hasBasic) {
            return basicScore;
        }
        if (hasExtended) {
            return extendedScore + convertLublinExtendedBonus(extendedScore);
        }
        return 0.0;
    }


    private TermEvaluation toEvaluation(RecruitmentFormulaConfig.TermConfig term, ExamResultDTO result) {
        String level = result.normalizedLevel();
        double coefficient = term.coefficientOrDefault(level);
        return TermEvaluation.evaluated(
                term.id(),
                result.normalizedSubjectCode(),
                level,
                result.score(),
                coefficient
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Set<String> collectSubjectCodes(List<String> groupIds) {
        Set<String> allowedSubjects = new HashSet<>();
        for (String groupId : groupIds) {
            RecruitmentFormulaConfig.SubjectGroupConfig groupConfig = subjectGroupsById.get(groupId.toLowerCase(Locale.ROOT));
            if (groupConfig == null) {
                throw new RecruitmentCalculationException("Unknown subject group: " + groupId);
            }
            allowedSubjects.addAll(
                    groupConfig.subjectCodes().stream()
                            .filter(Objects::nonNull)
                            .map(code -> code.toLowerCase(Locale.ROOT))
                            .toList()
            );
        }
        return allowedSubjects;
    }

    private ScoreOption findBestBasicScore(List<ExamResultDTO> results, boolean allowConversion, boolean allowBilingualNormalization) {
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestLevel = null;

        for (ExamResultDTO result : results) {
            if (result.score() == null) {
                continue;
            }
            String level = result.normalizedLevel();
            double rawScore;
            String levelLabel;

            if ("BASIC".equals(level)) {
                rawScore = result.score();
                levelLabel = level;
            } else if (allowConversion && "EXTENDED".equals(level)) {
                rawScore = convertExtendedToBasic(result.score());
                levelLabel = "BASIC_FROM_EXTENDED";
            } else if (allowBilingualNormalization && "BILINGUAL".equals(level)) {
                rawScore = convertBilingualToBasic(result.score());
                levelLabel = "BILINGUAL_ADJUSTED";
            } else {
                continue;
            }

            if (rawScore > bestScore) {
                bestScore = rawScore;
                bestLevel = levelLabel;
            }
        }

        if (bestLevel == null) {
            return null;
        }

        return new ScoreOption(bestLevel, bestScore);
    }

    private ScoreOption findBestExtendedScore(List<ExamResultDTO> results) {
        return results.stream()
                .filter(result -> "EXTENDED".equals(result.normalizedLevel()))
                .filter(result -> result.score() != null)
                .max(Comparator.comparingDouble(ExamResultDTO::score))
                .map(result -> new ScoreOption("EXTENDED", result.score()))
                .orElse(null);
    }

    private double convertExtendedToBasic(double extendedScore) {
        if (extendedScore <= 29.0) {
            return 2 * extendedScore;
        }
        return 0.5 * extendedScore + 50.0;
    }

    private double convertBilingualToBasic(double bilingualScore) {
        if (bilingualScore <= 29.0) {
            return 2 * bilingualScore;
        }
        return 100.0;
    }

    private double convertLublinExtendedBonus(double extendedScore) {
        if (extendedScore < 30.0) {
            return extendedScore;
        }
        return (6 * extendedScore + 100.0) / 7.0;
    }

    private enum TermStatus {
        EVALUATED,
        MISSING
    }

    private record TermEvaluation(
            TermStatus status,
            String termId,
            String subjectCode,
            String level,
            double rawScore,
            double coefficient,
            double points
    ) {
        static TermEvaluation evaluated(
                String termId,
                String subjectCode,
                String level,
                double rawScore,
                double coefficient
        ) {
            return new TermEvaluation(
                    TermStatus.EVALUATED,
                    termId,
                    subjectCode,
                    level,
                    rawScore,
                    coefficient,
                    rawScore * coefficient
            );
        }

        static TermEvaluation defaulted(String termId, double points) {
            return new TermEvaluation(
                    TermStatus.EVALUATED,
                    termId,
                    null,
                    "DEFAULT",
                    points,
                    1.0,
                    points
            );
        }

        static TermEvaluation missing(String termId) {
            return new TermEvaluation(TermStatus.MISSING, termId, null, null, 0.0, 0.0, 0.0);
        }
    }

    private record ScoreOption(String level, double score) {
    }
}
