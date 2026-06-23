package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.entity.Appraisal;

public class AppraisalMapper {

    public static AppraisalResponse toResponse(Appraisal appraisal) {
        if (appraisal == null) {
            return null;
        }

        AppraisalResponse response = new AppraisalResponse();
        response.setId(appraisal.getId());
        response.setSelfRating(appraisal.getSelfRating());
        response.setManagerRating(appraisal.getManagerRating());
        response.setWhatWentWell(appraisal.getWhatWentWell());
        response.setWhatToImprove(appraisal.getWhatToImprove());
        response.setKeyAchievements(appraisal.getKeyAchievements());
        response.setManagerComments(appraisal.getManagerComments());
        response.setStatus(appraisal.getStatus());
        response.setCreatedAt(appraisal.getCreatedAt());
        response.setUpdatedAt(appraisal.getUpdatedAt());

        if (appraisal.getEmployee() != null) {
            response.setEmployeeId(appraisal.getEmployee().getId());
            response.setEmployeeName(appraisal.getEmployee().getName());

            if (appraisal.getEmployee().getDepartment() != null) {
                response.setDepartmentId(appraisal.getEmployee().getDepartment().getId());
                response.setDepartment(appraisal.getEmployee().getDepartment().getName());
            }
        }

        if (appraisal.getManager() != null) {
            response.setManagerId(appraisal.getManager().getId());
            response.setManagerName(appraisal.getManager().getName());
        }

        if (appraisal.getCycle() != null) {
            response.setCycleId(appraisal.getCycle().getId());
            response.setCycleName(appraisal.getCycle().getName());
        }

        return response;
    }
}