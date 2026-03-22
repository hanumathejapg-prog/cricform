package com.cricform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesDTO {
    private Long seriesId;
    private String seriesName;
    private String seriesType;
    @Builder.Default
    private List<Long> matchIds = new ArrayList<>();
}
