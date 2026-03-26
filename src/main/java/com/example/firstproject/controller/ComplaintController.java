package com.example.firstproject.controller;

import com.example.firstproject.dto.ComplaintRequest;
import com.example.firstproject.model.Complaint;
import com.example.firstproject.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public Complaint submitComplaint(@RequestBody ComplaintRequest request, HttpServletRequest servletRequest) {
        String role = (String) servletRequest.getAttribute("authRole");
        Long authUserId = (Long) servletRequest.getAttribute("authUserId");

        if (!"USER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only USER can submit complaints");
        }

        request.setUserId(authUserId);
        return complaintService.createComplaint(request);
    }

    @GetMapping("/my")
    public List<Complaint> getMyComplaints(HttpServletRequest servletRequest) {
        String role = (String) servletRequest.getAttribute("authRole");
        Long authUserId = (Long) servletRequest.getAttribute("authUserId");

        if (!"USER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only USER can view personal complaints");
        }

        return complaintService.getComplaintsByUser(authUserId);
    }
}
