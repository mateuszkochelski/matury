package agh.matury.threshold;

import agh.matury.fieldOfStudy.dto.FieldOfStudyShortDTO;
import agh.matury.threshold.dto.ThresholdDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;

    public ThresholdService(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    public Page<ThresholdDTO> getAllThresholds(Pageable pageable) {
        return thresholdRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<ThresholdDTO> getThresholdsByFieldOfStudyId(Long fieldOfStudyId, Pageable pageable) {
        return thresholdRepository.findByFieldOfStudyId(fieldOfStudyId, pageable).map(this::toDTO);
    }

    private ThresholdDTO toDTO(Threshold threshold) {
        return new ThresholdDTO(
                threshold.getId(),
                threshold.getYear(),
                threshold.getPhase(),
                threshold.getAdmissionLimit(),
                threshold.getAdmissions(),
                threshold.getThreshold(),
                threshold.getSpecialRequirements(),
                new FieldOfStudyShortDTO(
                        threshold.getFieldOfStudy().getId(),
                        threshold.getFieldOfStudy().getName()
                )
        );
    }
}
