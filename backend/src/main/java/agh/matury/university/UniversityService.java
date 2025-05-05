package agh.matury.university;

import agh.matury.university.dto.UniversityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityService(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    public Page<UniversityDTO> getAllUniversities(Pageable pageable) {
        return universityRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<UniversityDTO> searchUniversities(String searchTerm, String city, Pageable pageable) {
        return universityRepository.findByNameOrAcronymAndCity(searchTerm, city, pageable).map(this::toDTO);
    }

    public UniversityDTO getUniversityById(Long id) {
        if(universityRepository.findById(id).isPresent()) {
            return toDTO(universityRepository.findById(id).get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found");
    }

    private UniversityDTO toDTO(University university) {
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
