package agh.matury.databaseSeeder;

import agh.matury.databaseSeeder.dto.DepartmentExternalDTO;
import agh.matury.databaseSeeder.dto.FieldOfStudyExternalDTO;
import agh.matury.databaseSeeder.dto.ThresholdExternalDTO;
import agh.matury.databaseSeeder.dto.UniversityExternalDTO;
import agh.matury.department.Department;
import agh.matury.department.DepartmentRepository;
import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.fieldOfStudy.FieldOfStudyRepository;
import agh.matury.threshold.Threshold;
import agh.matury.threshold.ThresholdRepository;
import agh.matury.university.University;
import agh.matury.university.UniversityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final DatabaseSeederService service;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final ThresholdRepository thresholdRepository;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    public DatabaseSeeder(DatabaseSeederService databaseSeederService, UniversityRepository universityRepository, DepartmentRepository departmentRepository, FieldOfStudyRepository fieldOfStudyRepository, ThresholdRepository thresholdRepository) {
        this.service = databaseSeederService;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.fieldOfStudyRepository = fieldOfStudyRepository;
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public void run(String... args) {
        if (universityRepository.count() == 0 || departmentRepository.count() == 0 || fieldOfStudyRepository.count() == 0) {
            wipeDatabase();

            logger.info("Fetching from External API...");

            try {
                seedDatabase();
            } catch (Exception e) {
                logger.error("Error while seeding database {}", e.getMessage());
            }
        }
    }

    private void wipeDatabase() {
        thresholdRepository.deleteAll();
        fieldOfStudyRepository.deleteAll();
        departmentRepository.deleteAll();
        universityRepository.deleteAll();
    }

    private void seedDatabase() {
        List<UniversityExternalDTO> universities = service.fetchAllUniversities();
        List<DepartmentExternalDTO> departments = service.fetchAllDepartments();
        List<FieldOfStudyExternalDTO> fieldsOfStudy = service.fetchAllFieldsOfStudy();
        List<ThresholdExternalDTO> thresholds = service.fetchAllThresholds();

        logger.info("Fetch success!");

        HashMap<Long, University> savedUniversities = new HashMap<>();
        HashMap<Long, Department> savedDepartments = new HashMap<>();
        HashMap<Long, FieldOfStudy> savedFieldsOfStudy = new HashMap<>();

        universities.forEach(dto -> {
            University university = new University(
                    dto.name(),
                    dto.city(),
                    dto.acronym(),
                    dto.url(),
                    dto.description(),
                    dto.address(),
                    dto.longitude(),
                    dto.latitude()
            );
            savedUniversities.put(dto.id(), university);
        });

        universityRepository.saveAll(savedUniversities.values());
        logger.info("Universities saved!");

        departments.forEach(dto -> {
            Department department = new Department(
                    dto.name(),
                    dto.url(),
                    savedUniversities.get(dto.university())
            );
            savedDepartments.put(dto.id(), department);
        });

        departmentRepository.saveAll(savedDepartments.values());
        logger.info("Department saved!");

        fieldsOfStudy.forEach(dto -> {
            FieldOfStudy fieldOfStudy = new FieldOfStudy(
                    dto.name(),
                    dto.level(),
                    savedUniversities.get(dto.university()),
                    savedDepartments.get(dto.department()),
                    dto.duration(),
                    dto.language()
            );
            savedFieldsOfStudy.put(dto.id(), fieldOfStudy);
        });

        fieldOfStudyRepository.saveAll(savedFieldsOfStudy.values());
        logger.info("Fields of study saved!");

        List<Threshold> savedThresholds = thresholds.stream()
                .map(dto -> new Threshold(
                        dto.year(),
                        dto.phase(),
                        dto.admissionLimit() != null ? dto.admissionLimit() : -1,
                        dto.admissions() != null ? dto.admissions() : -1,
                        dto.threshold() != null ? dto.threshold() : -1,
                        dto.specialRequirements(),
                        savedFieldsOfStudy.get(dto.fieldOfStudy())
                ))
                .toList();

        thresholdRepository.saveAll(savedThresholds);
        logger.info("Thresholds saved!");
    }
}
