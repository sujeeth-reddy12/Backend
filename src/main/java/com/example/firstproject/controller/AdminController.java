package com.example.firstproject.controller;

import com.example.firstproject.dto.AssignComplaintRequest;
import com.example.firstproject.dto.AdminComplaintUpdateRequest;
import com.example.firstproject.dto.AdminUserRequest;
import com.example.firstproject.dto.ComplaintRequest;
import com.example.firstproject.enums.ComplaintStatus;
import com.example.firstproject.enums.Role;
import com.example.firstproject.model.Complaint;
import com.example.firstproject.model.User;
import com.example.firstproject.repository.UserRepository;
import com.example.firstproject.service.ComplaintService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    public AdminController(ComplaintService complaintService, UserRepository userRepository) {
        this.complaintService = complaintService;
        this.userRepository = userRepository;
    }

    private void ensureAdmin(HttpServletRequest servletRequest) {
        String role = (String) servletRequest.getAttribute("authRole");
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    @GetMapping("/complaints")
    public List<Complaint> getAllComplaints(@RequestParam(name = "status", required = false) ComplaintStatus status, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        if (status != null) {
            return complaintService.getComplaintsByStatus(status);
        }
        return complaintService.getAllComplaints();
    }

    @PostMapping("/assign")
    public Complaint assignComplaint(@RequestBody AssignComplaintRequest request, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        return complaintService.assignComplaint(request);
    }

    @PostMapping("/complaints")
    public Complaint createComplaint(@RequestBody ComplaintRequest request, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        return complaintService.createComplaintByAdmin(request);
    }

    @PutMapping("/complaints/{complaintId}")
    public Complaint updateComplaint(
            @PathVariable Long complaintId,
            @RequestBody AdminComplaintUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        ensureAdmin(servletRequest);
        return complaintService.updateComplaintByAdmin(complaintId, request);
    }

    @DeleteMapping("/complaints/{complaintId}")
    public Map<String, String> deleteComplaint(@PathVariable Long complaintId, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        complaintService.deleteComplaintByAdmin(complaintId);
        return Map.of("message", "Complaint deleted successfully");
    }

    @GetMapping("/users")
    public List<User> getAllUsers(HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody AdminUserRequest request, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);

        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, email, password, and role are required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    @PutMapping("/users/{userId}")
    public User updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUserRequest request,
            HttpServletRequest servletRequest
    ) {
        ensureAdmin(servletRequest);

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim();
            userRepository.findByEmail(email).ifPresent(found -> {
                if (!found.getId().equals(existing.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                }
            });
            existing.setEmail(email);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(request.getPassword());
        }

        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }

        return userRepository.save(existing);
    }

    @DeleteMapping("/users/{userId}")
    public Map<String, String> deleteUser(@PathVariable Long userId, HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        Long authUserId = (Long) servletRequest.getAttribute("authUserId");

        if (authUserId != null && authUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own admin account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
        return Map.of("message", "User deleted successfully");
    }

    @GetMapping("/staff")
    public List<User> getStaffMembers(HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        return userRepository.findByRole(Role.STAFF);
    }

    @GetMapping("/analytics")
    public Map<String, Long> getAnalytics(HttpServletRequest servletRequest) {
        ensureAdmin(servletRequest);
        return complaintService.getAnalytics();
    }
}
