package org.example.bwxw.repository;

import org.example.bwxw.entity.Building;
import org.example.bwxw.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByBuildingId(Long buildingId);
    
    Optional<Room> findByBuildingIdAndRoomNumber(Long buildingId, String roomNumber);
    
    List<Room> findByStatus(Room.RoomStatus status);
    
    List<Room> findByBuildingIdAndStatus(Long buildingId, Room.RoomStatus status);
    
    @Query("SELECT r FROM Room r WHERE r.occupied < r.capacity AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRooms();
    
    @Query("SELECT r FROM Room r WHERE r.building.type = :gender AND r.occupied < r.capacity AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRoomsByGender(Building.Gender gender);
}
