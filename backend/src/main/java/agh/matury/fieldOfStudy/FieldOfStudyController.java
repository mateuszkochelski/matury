package agh.matury.fieldOfStudy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("field_of_study")
public class FieldOfStudyController {

    private final FieldOfStudyService fieldOfStudyService;

    public FieldOfStudyController(FieldOfStudyService fieldOfStudyService) {
        this.fieldOfStudyService = fieldOfStudyService;
    }

    @GetMapping
    public List<FieldOfStudy> getAllFieldsOdStudy() {
        return this.fieldOfStudyService.getAllFieldsOfStudy();
    }
}
