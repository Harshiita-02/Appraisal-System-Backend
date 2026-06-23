package com.appraise.appraisal.System.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamReportResponse {
    private String cycle;
    private long teamMembers;
    private Double avgRating;
    private List<TeamReportRow> rows;
}