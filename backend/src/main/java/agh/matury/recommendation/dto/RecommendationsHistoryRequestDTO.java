package agh.matury.recommendation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RecommendationsHistoryRequestDTO {
    private List<Long> fieldIds;
    private int k;
}
