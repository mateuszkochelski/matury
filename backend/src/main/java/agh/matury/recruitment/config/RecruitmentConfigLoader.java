package agh.matury.recruitment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class RecruitmentConfigLoader {

    private final RecruitmentFormulaConfig config;

    public RecruitmentConfigLoader(ObjectMapper objectMapper) {
        this.config = loadConfig(objectMapper);
    }

    public RecruitmentFormulaConfig getConfig() {
        return config;
    }

    private RecruitmentFormulaConfig loadConfig(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("recruitment/formulas.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, RecruitmentFormulaConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load recruitment formulas configuration", e);
        }
    }
}
