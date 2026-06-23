package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusBreakdownEntry {
    private AppraisalStatus status;
    private long count;
}