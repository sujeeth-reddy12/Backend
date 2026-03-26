package com.example.firstproject.repository;

import com.example.firstproject.enums.ComplaintStatus;
import com.example.firstproject.model.Complaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Complaint> findByStaffIdOrderByCreatedAtDesc(Long staffId);
    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);
    long countByStatus(ComplaintStatus status);
}
