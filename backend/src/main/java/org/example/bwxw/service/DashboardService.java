package org.example.bwxw.service;

import org.example.bwxw.dto.DashboardStats;
import org.example.bwxw.entity.Repair;
import org.example.bwxw.entity.Leave;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RepairRepository repairRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    public DashboardStats getStats() {
        long totalStudents = userRepository.countByRole(User.UserRole.STUDENT);
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByOccupiedGreaterThan(0);
        double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0.0;
        long pendingRepairs = repairRepository.countByStatus(Repair.RepairStatus.PENDING);
        long pendingLeaves = leaveRepository.countByStatus(Leave.LeaveStatus.PENDING);
        
        Map<String, Long> repairsByType = repairRepository.countByType().stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue()
                ));
        
        Map<String, Long> roomsByBuilding = new HashMap<>();
        for (Object[] row : roomRepository.countRoomsByBuilding()) {
            roomsByBuilding.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        
        return new DashboardStats(
                totalStudents,
                totalRooms,
                occupiedRooms,
                occupancyRate,
                pendingRepairs,
                pendingLeaves,
                repairsByType,
                roomsByBuilding
        );
    }
}