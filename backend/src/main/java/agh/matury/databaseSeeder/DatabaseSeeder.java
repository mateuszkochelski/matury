package agh.matury.databaseSeeder;

import agh.matury.databaseSeeder.dto.DepartmentExternalDTO;
import agh.matury.databaseSeeder.dto.FieldOfStudyExternalDTO;
import agh.matury.databaseSeeder.dto.UniversityExternalDTO;
import agh.matury.department.Department;
import agh.matury.department.DepartmentRepository;
import agh.matury.fieldOfStudy.FieldOfStudy;
import agh.matury.fieldOfStudy.FieldOfStudyRepository;
import agh.matury.university.University;
import agh.matury.university.UniversityRepository;
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

    public DatabaseSeeder(DatabaseSeederService databaseSeederService, UniversityRepository universityRepository, DepartmentRepository departmentRepository, FieldOfStudyRepository fieldOfStudyRepository) {
        this.service = databaseSeederService;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.fieldOfStudyRepository = fieldOfStudyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (universityRepository.count() == 0 || departmentRepository.count() == 0 || fieldOfStudyRepository.count() == 0) {
            wipeDatabase();

            System.out.println("Fetching from External API...");

            List<UniversityExternalDTO> universities = service.fetchAllUniversities();
            List<DepartmentExternalDTO> departments = service.fetchAllDepartments();
            List<FieldOfStudyExternalDTO> fieldsOfStudy = service.fetchAllFieldsOfStudy();

            System.out.println(universities.size());
            System.out.println(departments.size());
            System.out.println(fieldsOfStudy.size());
            System.out.println("Fetch success!");

            HashMap<Long, University> savedUniversities = new HashMap<>();
            HashMap<Long, Department> savedDepartments = new HashMap<>();

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
            System.out.println("Universities saved!");

            departments.forEach(dto -> {
                Department department = new Department(
                        dto.name(),
                        dto.url(),
                        savedUniversities.get(dto.university())
                );
                savedDepartments.put(dto.id(), departmentRepository.save(department));
            });

            System.out.println("Department saved!");

            fieldsOfStudy.forEach(dto -> {
                FieldOfStudy fieldOfStudy = new FieldOfStudy(
                        dto.name(),
                        dto.level(),
                        savedUniversities.get(dto.university()),
                        savedDepartments.get(dto.department()),
                        dto.duration(),
                        dto.language()
                );
                fieldOfStudyRepository.save(fieldOfStudy);
            });

            System.out.println("Fields of study saved!");
        }
    }

    private void wipeDatabase() {
        universityRepository.deleteAll();
        departmentRepository.deleteAll();
        fieldOfStudyRepository.deleteAll();
    }
}
