package agh.matury.threshold;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;

    public ThresholdService(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    public List<Threshold> getAllThresholds() {
        return thresholdRepository.findAll();
    }
}
