package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.DepartmentRequest;
import com.appraise.appraisal.System.dtos.DepartmentResponse;
import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartmentById(Long id);
}