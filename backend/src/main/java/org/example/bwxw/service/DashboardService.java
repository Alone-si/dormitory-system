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
    
    @Autowired
    private BuildingRepository buildingRepository;
    
    public DashboardStats getStats() {
        // 学生总数
        Long totalStudents = (long) userRepository.findByRole(User.UserRole.STUDENT).size();
        
        // 房间统计
        Long totalRooms = roomRepository.count();
        Long occupiedRooms = roomRepository.findAll().stream()
                .filter(room -> room.getOccupied() > 0)
                .count();
        
        // 入住率
        Double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0.0;
        
        // 待处理报修
        Long pendingRepairs = (long) repairRepository.findByStatus(Repair.RepairStatus.PENDING).size();
        
        // 待审批请假
        Long pendingLeaves = (long) leaveRepository.findByStatus(Leave.LeaveStatus.PENDING).size();
        
        // 报修类型分布
        List<Object[]> repairTypeData = repairRepository.countByType();
        Map<String, Long> repairsByType = repairTypeData.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        
        // 各楼栋房间数
        Map<String, Long> roomsByBuilding = new HashMap<>();
        buildingRepository.findAll().forEach(building -> {
            long count = roomRepository.findByBuildingId(building.getId()).size();
            roomsByBuilding.put(building.getName(), count);
        });
        
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
