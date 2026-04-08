package org.example.bwxw.service;

import org.example.bwxw.entity.Building;
import org.example.bwxw.entity.Room;
import org.example.bwxw.repository.BuildingRepository;
import org.example.bwxw.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }
    
    public Building getBuildingById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("宿舍楼不存在"));
    }
    
    @Transactional
    public Building createBuilding(Building building) {
        if (buildingRepository.existsByName(building.getName())) {
            throw new RuntimeException("宿舍楼名称已存在");
        }
        
        // 保存宿舍楼
        Building savedBuilding = buildingRepository.save(building);
        
        // 自动生成房间
        generateRooms(savedBuilding);
        
        return savedBuilding;
    }
    
    @Transactional
    public void deleteBuilding(Long id) {
        Building building = getBuildingById(id);
        
        // 检查是否有学生入住
        List<Room> rooms = roomRepository.findByBuildingId(id);
        boolean hasStudents = rooms.stream().anyMatch(room -> room.getOccupied() > 0);
        
        if (hasStudents) {
            throw new RuntimeException("该宿舍楼还有学生入住，无法删除");
        }
        
        // 删除所有房间
        roomRepository.deleteAll(rooms);
        
        // 删除宿舍楼
        buildingRepository.delete(building);
    }
    
    private void generateRooms(Building building) {
        for (int floor = 1; floor <= building.getFloors(); floor++) {
            for (int roomNum = 1; roomNum <= building.getRoomsPerFloor(); roomNum++) {
                Room room = new Room();
                room.setBuilding(building);
                room.setFloor(floor);
                // 房间号格式：101, 102, ..., 201, 202, ...
                room.setRoomNumber(String.format("%d%02d", floor, roomNum));
                room.setCapacity(building.getCapacity());
                room.setOccupied(0);
                room.setStatus(Room.RoomStatus.AVAILABLE);
                
                roomRepository.save(room);
            }
        }
    }
}
