package org.example.bwxw.repository;

import org.example.bwxw.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    List<Leave> findByStudentId(Long studentId);
    
    List<Leave> findByStatus(Leave.LeaveStatus status);
    
    List<Leave> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    List<Leave> findByStatusOrderByCreatedAtDesc(Leave.LeaveStatus status);
    
    List<Leave> findByStudentIdAndStatus(Long studentId, Leave.LeaveStatus status);
    
    @Query("SELECT l FROM Leave l WHERE l.status = 'PENDING' ORDER BY l.createdAt ASC")
    List<Leave> findPendingLeavesOrderedByDate();
    
    @Query("SELECT l FROM Leave l WHERE l.student.id = :studentId AND l.startDate <= :date AND l.endDate >= :date AND l.status = 'APPROVED'")
    List<Leave> findActiveLeavesByStudentAndDate(Long studentId, LocalDate date);
}
