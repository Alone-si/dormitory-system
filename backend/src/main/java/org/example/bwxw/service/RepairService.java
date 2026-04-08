package org.example.bwxw.service;

import org.example.bwxw.entity.Repair;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.RepairRepository;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairService {
    
    @Autowired
    private RepairRepository repairRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }
    
    public Repair getRepairById(Long id) {
        return repairRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("报修记录不存在"));
    }
    
    public List<Repair> getRepairsByStudent(Long studentId) {
        return repairRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
    
    public List<Repair> getPendingRepairs() {
        return repairRepository.findPendingRepairsOrderedByUrgency();
    }
    
    public List<Repair> getRepairsByStatus(Repair.RepairStatus status) {
        return repairRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Repair createRepair(Repair repair) {
        return repairRepository.save(repair);
    }
    
    @Transactional
    public Repair updateRepairStatus(Long repairId, Repair.RepairStatus status, Long handlerId, String reply) {
        Repair repair = getRepairById(repairId);
        repair.setStatus(status);
        
        if (handlerId != null) {
            User handler = userRepository.findById(handlerId)
                    .orElseThrow(() -> new RuntimeException("处理人不存在"));
            repair.setHandler(handler);
        }
        
        if (reply != null) {
            repair.setAdminReply(reply);
        }
        
        if (status == Repair.RepairStatus.COMPLETED || status == Repair.RepairStatus.REJECTED) {
            repair.setHandledAt(LocalDateTime.now());
        }
        
        return repairRepository.save(repair);
    }
    
    @Transactional
    public void deleteRepair(Long id) {
        Repair repair = getRepairById(id);
        repairRepository.delete(repair);
    }
}
