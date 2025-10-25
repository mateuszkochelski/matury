package agh.matury.recruitment.config;

import java.util.List;
import java.util.Map;

public record RecruitmentFormulaConfig(
        List<SubjectConfig> subjects,
        List<SubjectGroupConfig> subjectGroups,
        List<UniversityConfig> universities
) {

    public record SubjectConfig(String code, String label) {
    }

    public record SubjectGroupConfig(
            String id,
            String label,
            List<String> subjectCodes,
            List<String> allowedLevels
    ) {
        public List<String> subjectCodes() {
            return subjectCodes == null ? List.of() : subjectCodes;
        }

        public List<String> allowedLevels() {
            return allowedLevels == null ? List.of() : allowedLevels;
        }
    }

    public record UniversityConfig(
            String id,
            String name,
            List<String> aliases,
            List<FieldOfStudyConfig> fieldsOfStudy
    ) {
        public List<String> aliases() {
            return aliases == null ? List.of() : aliases;
        }

        public List<FieldOfStudyConfig> fieldsOfStudy() {
            return fieldsOfStudy == null ? List.of() : fieldsOfStudy;
        }
    }

    public record FieldOfStudyConfig(
            String id,
            String name,
            List<String> aliases,
            FormulaConfig formula
    ) {
        public List<String> aliases() {
            return aliases == null ? List.of() : aliases;
        }
    }

    public record FormulaConfig(
            String type,
            List<TermConfig> terms
    ) {
        public List<TermConfig> terms() {
            return terms == null ? List.of() : terms;
        }
    }

    public record TermConfig(
            String id,
            String description,
            String type,
            String subjectCode,
            String requiredLevel,
            List<String> allowedGroupIds,
            Map<String, Double> coefficientByLevel,
            Double coefficient,
            Boolean allowVocationalTechnician,
            Boolean failIfMissing,
            Boolean fallbackToZero,
            Boolean allowExtendedToBasicConversion,
            Boolean normalizeBilingualScores,
            Boolean includeVocationalResults,
            List<String> vocationalSubjectCodes,
            Double vocationalMultiplier
    ) {
        public List<String> allowedGroupIds() {
            return allowedGroupIds == null ? List.of() : allowedGroupIds;
        }

        public Map<String, Double> coefficientByLevel() {
            return coefficientByLevel == null ? Map.of() : Map.copyOf(coefficientByLevel);
        }

        public double coefficientOrDefault(String level) {
            Map<String, Double> map = coefficientByLevel();
            if (level != null && map.containsKey(level.toUpperCase())) {
                return map.get(level.toUpperCase());
            }
            if (coefficient != null) {
                return coefficient;
            }
            if (level != null && map.containsKey(level)) {
                return map.get(level);
            }
            throw new IllegalStateException("Coefficient not defined for term: " + id);
        }

        public boolean allowVocationalTechnicianOrDefault() {
            return Boolean.TRUE.equals(allowVocationalTechnician);
        }

        public boolean failIfMissingOrDefault() {
            return Boolean.TRUE.equals(failIfMissing);
        }

        public boolean fallbackToZeroOrDefault() {
            return Boolean.TRUE.equals(fallbackToZero);
        }

        public boolean allowExtendedToBasicConversionOrDefault() {
            return Boolean.TRUE.equals(allowExtendedToBasicConversion);
        }

        public boolean normalizeBilingualScoresOrDefault() {
            return Boolean.TRUE.equals(normalizeBilingualScores);
        }

        public boolean includeVocationalResultsOrDefault() {
            return Boolean.TRUE.equals(includeVocationalResults);
        }

        public List<String> vocationalSubjectCodes() {
            return vocationalSubjectCodes == null ? List.of() : vocationalSubjectCodes;
        }

        public double vocationalMultiplierOrDefault() {
            return vocationalMultiplier == null ? 2.0 : vocationalMultiplier;
        }
    }
}
