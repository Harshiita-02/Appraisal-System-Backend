package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.UserRequest;
import com.appraise.appraisal.System.dtos.UserResponse;
import com.appraise.appraisal.System.entity.Department;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.Roles;
import com.appraise.appraisal.System.entity.enums.UserStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.DuplicateResourceException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.UserMapper;
import com.appraise.appraisal.System.repository.DepartmentRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User with email '" + normalizedEmail + "' already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
        }

        User user = new User();
        mapRequestToEntity(request, user, department, manager);
        user.setEmail(normalizedEmail);
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithRelationships()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!existingUser.getEmail().equalsIgnoreCase(normalizedEmail) &&
                userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email '" + normalizedEmail + "' is already claimed by another user");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        User manager = null;
        if (request.getManagerId() != null) {
            if (id.equals(request.getManagerId())) {
                throw new BadRequestException("A user cannot be assigned as their own manager");
            }
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
        }

        mapRequestToEntity(request, existingUser, department, manager);
        existingUser.setEmail(normalizedEmail);

        User saved = userRepository.save(existingUser);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (userRepository.existsByManagerId(id)) {
            throw new BadRequestException("Cannot delete this user because they are actively assigned as a manager to other employees");
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (status == null || status.trim().isBlank()) {
            throw new BadRequestException("Status value cannot be empty");
        }

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value provided: " + status + ". Must be ACTIVE or INACTIVE");
        }

        if (newStatus == UserStatus.INACTIVE && userRepository.existsByManagerId(id)) {
            throw new BadRequestException("Cannot deactivate this user because they are actively assigned as a manager to other employees");
        }

        user.setStatus(newStatus);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    private void mapRequestToEntity(UserRequest request, User user, Department department, User manager) {
        user.setName(request.getName().trim());
        user.setPassword(request.getPassword());
        user.setJobTitle(request.getJobTitle().trim());
        user.setDepartment(department);
        user.setManager(manager);

        try {
            user.setRole(Roles.valueOf(request.getRole().trim().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid system role type standard provided: " + request.getRole());
        }
    }
}