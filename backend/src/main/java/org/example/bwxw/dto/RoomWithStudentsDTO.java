package org.example.bwxw.dto;

import lombok.Data;
import org.example.bwxw.entity.Building;
import org.example.bwxw.entity.Room;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoomWithStudentsDTO {
    private Long id;
    private String roomNumber;
    private Integer floor;
    private Integer capacity;
    private Integer occupied;
    private Room.RoomStatus status;
    private BuildingSimpleDTO building;
    private List<StudentSimpleDTO> students;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class BuildingSimpleDTO {
        private Long id;
        private String name;
        private Building.Gender type;
        private Integer floors;
        private String address;
    }
    
    @Data
    public static class StudentSimpleDTO {
        private Long id;
        private String name;
        private String studentId;
        private String gender;
        private String phone;
        private String className;
        private String avatar;
    }
}
