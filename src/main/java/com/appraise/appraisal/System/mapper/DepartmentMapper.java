package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.DepartmentRequest;
import com.appraise.appraisal.System.dtos.DepartmentResponse;
import com.appraise.appraisal.System.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        if (request == null) {
            return null;
        }
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return department;
    }

    public DepartmentResponse toResponse(Department department) {
        if (department == null) {
            return null;
        }

        int employeeCount = department.getUsers() != null ? department.getUsers().size() : 0;

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                employeeCount,
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}