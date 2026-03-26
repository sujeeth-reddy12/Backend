package com.example.firstproject.service;

import com.example.firstproject.dto.AssignComplaintRequest;
import com.example.firstproject.dto.AdminComplaintUpdateRequest;
import com.example.firstproject.dto.ComplaintRequest;
import com.example.firstproject.dto.StatusUpdateRequest;
import com.example.firstproject.enums.ComplaintStatus;
import com.example.firstproject.enums.Role;
import com.example.firstproject.model.Complaint;
import com.example.firstproject.model.User;
import com.example.firstproject.repository.ComplaintRepository;
import com.example.firstproject.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    public Complaint createComplaint(ComplaintRequest request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only USER can submit complaints");
        }

        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setUserId(request.getUserId());
        complaint.setStatus(ComplaintStatus.PENDING);
        return complaintRepository.save(complaint);
    }

    public Complaint createComplaintByAdmin(ComplaintRequest request) {
        return createComplaint(request);
    }

    public Complaint updateComplaintByAdmin(Long complaintId, AdminComplaintUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        if (request.getTitle() != null) {
            complaint.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            complaint.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            complaint.setCategory(request.getCategory());
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getRole() != Role.USER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complaint owner must be USER role");
            }

            complaint.setUserId(request.getUserId());
        }

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

            if (staff.getRole() != Role.STAFF) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned member must be STAFF role");
            }

            complaint.setStaffId(request.getStaffId());
        }

        if (request.getStatus() != null) {
            complaint.setStatus(request.getStatus());
        }

        if (request.getRemarks() != null) {
            complaint.setRemarks(request.getRemarks());
        }

        return complaintRepository.save(complaint);
    }

    public void deleteComplaintByAdmin(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        complaintRepository.delete(complaint);
    }

    public Complaint assignComplaint(AssignComplaintRequest request) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        if (staff.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not STAFF");
        }

        complaint.setStaffId(request.getStaffId());
        if (complaint.getStatus() == ComplaintStatus.PENDING) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        }

        return complaintRepository.save(complaint);
    }

    public Map<String, Object> updateComplaintStatus(StatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        if (complaint.getStaffId() == null || !complaint.getStaffId().equals(request.getStaffId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complaint is not assigned to this staff");
        }

        complaint.setStatus(request.getStatus());
        complaint.setRemarks(request.getRemarks());
        Complaint updated = complaintRepository.save(complaint);

        Map<String, Object> result = new HashMap<>();
        result.put("complaint", updated);

        if (request.getStatus() == ComplaintStatus.RESOLVED) {
            result.put("notification", "Notification sent to user via Email/SMS simulation");
        } else {
            result.put("notification", "Status updated");
        }

        return result;
    }

    public List<Complaint> getComplaintsByUser(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Complaint> getComplaintsByStaff(Long staffId) {
        return complaintRepository.findByStaffIdOrderByCreatedAtDesc(staffId);
    }

    public Map<String, Long> getAnalytics() {
        Map<String, Long> analytics = new HashMap<>();
        analytics.put("totalComplaints", complaintRepository.count());
        analytics.put("pendingComplaints", complaintRepository.countByStatus(ComplaintStatus.PENDING));
        analytics.put("inProgressComplaints", complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        analytics.put("resolvedComplaints", complaintRepository.countByStatus(ComplaintStatus.RESOLVED));
        return analytics;
    }
}
