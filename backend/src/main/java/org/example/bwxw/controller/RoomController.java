package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.dto.RoomWithStudentsDTO;
import org.example.bwxw.entity.Room;
import org.example.bwxw.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    @GetMapping
    public ApiResponse<List<Room>> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/with-students")
    public ApiResponse<List<RoomWithStudentsDTO>> getAllRoomsWithStudents() {
        try {
            List<RoomWithStudentsDTO> rooms = roomService.getAllRoomsWithStudents();
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Room> getRoomById(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            return ApiResponse.success(room);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/students")
    public ApiResponse<List<RoomWithStudentsDTO.StudentSimpleDTO>> getRoomStudents(@PathVariable Long id) {
        try {
            List<RoomWithStudentsDTO.StudentSimpleDTO> students = roomService.getRoomStudents(id);
            return ApiResponse.success(students);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/available")
    public ApiResponse<List<Room>> getAvailableRooms() {
        try {
            List<Room> rooms = roomService.getAvailableRooms();
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/building/{buildingId}")
    public ApiResponse<List<Room>> getRoomsByBuilding(@PathVariable Long buildingId) {
        try {
            List<Room> rooms = roomService.getRoomsByBuilding(buildingId);
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Room> createRoom(@RequestBody Room room) {
        try {
            Room created = roomService.createRoom(room);
            return ApiResponse.success("房间创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            Room updated = roomService.updateRoom(id, room);
            return ApiResponse.success("房间更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/assign")
    public ApiResponse<Room> assignStudent(@RequestParam Long studentId, @RequestParam Long roomId) {
        try {
            Room room = roomService.assignStudentToRoom(studentId, roomId);
            return ApiResponse.success("分配成功", room);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/remove/{studentId}")
    public ApiResponse<Room> removeStudent(@PathVariable Long studentId) {
        try {
            Room room = roomService.removeStudentFromRoom(studentId);
            return ApiResponse.success("退宿成功", room);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
