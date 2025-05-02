package agh.matury.university;

import agh.matury.university.dto.UniversityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityService(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    public Page<UniversityDTO> getAllUniversities(Pageable pageable) {
        return universityRepository.findAll(pageable).map(this::toDTO);
    }

    public UniversityDTO toDTO(University university) {
        return new UniversityDTO(
                university.getId(),
                university.getName(),
                university.getCity(),
                university.getAcronym(),
                university.getUrl(),
                university.getDescription(),
                university.getAddress(),
                university.getLongitude(),
                university.getLatitude()
        );
    }
}
