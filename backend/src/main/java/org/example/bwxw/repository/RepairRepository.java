package org.example.bwxw.repository;

import org.example.bwxw.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    
    List<Repair> findByStudentId(Long studentId);
    
    List<Repair> findByRoomId(Long roomId);
    
    List<Repair> findByStatus(Repair.RepairStatus status);
    
    List<Repair> findByType(Repair.RepairType type);
    
    List<Repair> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    List<Repair> findByStatusOrderByCreatedAtDesc(Repair.RepairStatus status);
    
    @Query("SELECT r FROM Repair r WHERE r.status = 'PENDING' ORDER BY r.urgency DESC, r.createdAt ASC")
    List<Repair> findPendingRepairsOrderedByUrgency();
    
    @Query("SELECT r.type, COUNT(r) FROM Repair r GROUP BY r.type")
    List<Object[]> countByType();
}
