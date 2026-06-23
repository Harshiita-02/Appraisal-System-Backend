package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.AppraisalCycleRequest;
import com.appraise.appraisal.System.dtos.AppraisalCycleResponse;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import org.springframework.stereotype.Component;

@Component
public class AppraisalCycleMapper {

    public AppraisalCycle toEntity(AppraisalCycleRequest request) {
        if (request == null) {
            return null;
        }
        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setName(request.getName().trim());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setActive(request.getActive() != null ? request.getActive() : true);
        return cycle;
    }

    public AppraisalCycleResponse toResponse(AppraisalCycle cycle) {
        if (cycle == null) {
            return null;
        }
        return new AppraisalCycleResponse(
                cycle.getId(),
                cycle.getName(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                cycle.getActive(),
                cycle.getCreatedAt()
        );
    }
}