package com.example.firstproject.controller;

import com.example.firstproject.dto.StatusUpdateRequest;
import com.example.firstproject.model.Complaint;
import com.example.firstproject.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:3001", "https://*.vercel.app"})
@RequestMapping("/api/staff")
public class StaffController {

    private final ComplaintService complaintService;

    public StaffController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    private Long ensureStaffAndGetUserId(HttpServletRequest servletRequest) {
        String role = (String) servletRequest.getAttribute("authRole");
        Long authUserId = (Long) servletRequest.getAttribute("authUserId");
        if (!"STAFF".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff access required");
        }
        return authUserId;
    }

    @GetMapping("/complaints")
    public List<Complaint> getAssignedComplaints(HttpServletRequest servletRequest) {
        Long authUserId = ensureStaffAndGetUserId(servletRequest);
        return complaintService.getComplaintsByStaff(authUserId);
    }

    @PostMapping("/status")
    public Map<String, Object> updateStatus(@RequestBody StatusUpdateRequest request, HttpServletRequest servletRequest) {
        Long authUserId = ensureStaffAndGetUserId(servletRequest);
        request.setStaffId(authUserId);
        return complaintService.updateComplaintStatus(request);
    }
}
