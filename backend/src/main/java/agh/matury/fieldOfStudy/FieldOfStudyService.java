package agh.matury.fieldOfStudy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldOfStudyService {

    private final FieldOfStudyRepository fieldOfStudyRepository;

    public FieldOfStudyService(FieldOfStudyRepository fieldOfStudyRepository) {
        this.fieldOfStudyRepository = fieldOfStudyRepository;
    }

    public List<FieldOfStudy> getAllFieldsOfStudy() {
        return fieldOfStudyRepository.findAll();
    }
}
