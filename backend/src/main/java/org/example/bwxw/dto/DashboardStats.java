package org.example.bwxw.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.Map;

/**
 * 仪表盘统计数据
 */
@Data
@AllArgsConstructor
public class DashboardStats {
    private Long totalStudents;
    private Long totalRooms;
    private Long occupiedRooms;
    private Double occupancyRate;
    private Long pendingRepairs;
    private Long pendingLeaves;
    private Map<String, Long> repairsByType;
    private Map<String, Long> roomsByBuilding;
}
