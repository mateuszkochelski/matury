package agh.matury.databaseSeeder;

import agh.matury.databaseSeeder.dto.DepartmentExternalDTO;
import agh.matury.databaseSeeder.dto.FieldOfStudyExternalDTO;
import agh.matury.databaseSeeder.dto.ThresholdExternalDTO;
import agh.matury.databaseSeeder.dto.UniversityExternalDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DatabaseSeederService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<UniversityExternalDTO> fetchAllUniversities() {
        String url = "https://dostanesie.pl/api/core/university/";
        UniversityExternalDTO[] result = restTemplate.getForObject(url, UniversityExternalDTO[].class);
        return List.of(result);
    }

    public List<DepartmentExternalDTO> fetchAllDepartments() {
        String url = "https://dostanesie.pl/api/core/department/";
        DepartmentExternalDTO[] result = restTemplate.getForObject(url, DepartmentExternalDTO[].class);
        return List.of(result);
    }

    public List<FieldOfStudyExternalDTO> fetchAllFieldsOfStudy() {
        String url = "https://dostanesie.pl/api/core/field_of_study/";
        FieldOfStudyExternalDTO[] result = restTemplate.getForObject(url, FieldOfStudyExternalDTO[].class);
        return List.of(result);
    }

    public List<ThresholdExternalDTO> fetchAllThresholds() {
        String url = "https://dostanesie.pl/api/core/recruitment/";
        ThresholdExternalDTO[] result = restTemplate.getForObject(url, ThresholdExternalDTO[].class);
        return List.of(result);
    }
}
