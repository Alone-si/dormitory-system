package org.example.bwxw.service;

import org.example.bwxw.dto.RoomWithStudentsDTO;
import org.example.bwxw.entity.Room;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.RoomRepository;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public List<RoomWithStudentsDTO> getAllRoomsWithStudents() {
        List<Room> rooms = roomRepository.findAll();
        
        // 一次性查询所有学生，避免N+1问题
        List<User> allStudents = userRepository.findByRole(User.UserRole.STUDENT);
        
        // 按房间ID分组学生
        var studentsByRoom = allStudents.stream()
                .filter(s -> s.getRoom() != null)
                .collect(Collectors.groupingBy(s -> s.getRoom().getId()));
        
        return rooms.stream()
                .map(room -> convertToDTO(room, studentsByRoom.getOrDefault(room.getId(), List.of())))
                .collect(Collectors.toList());
    }
    
    private RoomWithStudentsDTO convertToDTO(Room room, List<User> students) {
        RoomWithStudentsDTO dto = new RoomWithStudentsDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloor(room.getFloor());
        dto.setCapacity(room.getCapacity());
        dto.setOccupied(room.getOccupied());
        dto.setStatus(room.getStatus());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        
        // 转换Building信息
        RoomWithStudentsDTO.BuildingSimpleDTO buildingDTO = new RoomWithStudentsDTO.BuildingSimpleDTO();
        buildingDTO.setId(room.getBuilding().getId());
        buildingDTO.setName(room.getBuilding().getName());
        buildingDTO.setType(room.getBuilding().getType());
        buildingDTO.setFloors(room.getBuilding().getFloors());
        buildingDTO.setAddress(room.getBuilding().getAddress());
        dto.setBuilding(buildingDTO);
        
        // 转换学生信息
        List<RoomWithStudentsDTO.StudentSimpleDTO> studentDTOs = students.stream()
                .map(student -> {
                    RoomWithStudentsDTO.StudentSimpleDTO studentDTO = new RoomWithStudentsDTO.StudentSimpleDTO();
                    studentDTO.setId(student.getId());
                    studentDTO.setName(student.getName());
                    studentDTO.setStudentId(student.getStudentId());
                    studentDTO.setGender(student.getGender().toString());
                    studentDTO.setPhone(student.getPhone());
                    studentDTO.setClassName(student.getClassName());
                    studentDTO.setAvatar(student.getAvatar());
                    return studentDTO;
                })
                .collect(Collectors.toList());
        
        dto.setStudents(studentDTOs);
        return dto;
    }
    
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("房间不存在"));
    }
    
    public List<RoomWithStudentsDTO.StudentSimpleDTO> getRoomStudents(Long roomId) {
        // 查询该房间的所有学生
        List<User> students = userRepository.findByRoomId(roomId);
        
        // 转换为DTO
        return students.stream()
                .map(student -> {
                    RoomWithStudentsDTO.StudentSimpleDTO dto = new RoomWithStudentsDTO.StudentSimpleDTO();
                    dto.setId(student.getId());
                    dto.setName(student.getName());
                    dto.setStudentId(student.getStudentId());
                    dto.setGender(student.getGender().toString());
                    dto.setPhone(student.getPhone());
                    dto.setClassName(student.getClassName());
                    dto.setAvatar(student.getAvatar());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public List<Room> getAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }
    
    public List<Room> getRoomsByBuilding(Long buildingId) {
        return roomRepository.findByBuildingId(buildingId);
    }
    
    @Transactional
    public Room assignStudentToRoom(Long studentId, Long roomId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));
        
        // 检查房间是否已满
        if (room.getOccupied() >= room.getCapacity()) {
            throw new RuntimeException("房间已满");
        }
        
        // 检查学生是否已有宿舍
        if (user.getRoom() != null) {
            throw new RuntimeException("学生已分配宿舍");
        }
        
        // 分配宿舍
        user.setRoom(room);
        room.setOccupied(room.getOccupied() + 1);
        
        // 更新房间状态
        if (room.getOccupied() >= room.getCapacity()) {
            room.setStatus(Room.RoomStatus.FULL);
        }
        
        // 更新 User 表的状态为 ACTIVE
        user.setStatus("ACTIVE");
        
        userRepository.save(user);
        return roomRepository.save(room);
    }
    
    @Transactional
    public Room removeStudentFromRoom(Long userId) {
        // 通过User ID查找学生
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        Room room = user.getRoom();
        if (room == null) {
            throw new RuntimeException("学生未分配宿舍");
        }
        
        // 移除宿舍
        user.setRoom(null);
        room.setOccupied(room.getOccupied() - 1);
        
        // 更新房间状态
        if (room.getOccupied() < room.getCapacity()) {
            room.setStatus(Room.RoomStatus.AVAILABLE);
        }
        
        // 更新 User 表的状态
        user.setStatus("INACTIVE");
        
        userRepository.save(user);
        return roomRepository.save(room);
    }
    
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }
    
    public Room updateRoom(Long id, Room roomDetails) {
        Room room = getRoomById(id);
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setFloor(roomDetails.getFloor());
        room.setCapacity(roomDetails.getCapacity());
        room.setStatus(roomDetails.getStatus());
        room.setDormContract(roomDetails.getDormContract());
        return roomRepository.save(room);
    }
}
