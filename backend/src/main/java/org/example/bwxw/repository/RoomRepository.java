package org.example.bwxw.repository;

import org.example.bwxw.entity.Building;
import org.example.bwxw.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    @EntityGraph(attributePaths = "building")
    List<Room> findAll();
    
    @EntityGraph(attributePaths = "building")
    List<Room> findByBuildingId(Long buildingId);
    
    Optional<Room> findByBuildingIdAndRoomNumber(Long buildingId, String roomNumber);
    
    @EntityGraph(attributePaths = "building")
    List<Room> findByStatus(Room.RoomStatus status);
    
    @EntityGraph(attributePaths = "building")
    List<Room> findByBuildingIdAndStatus(Long buildingId, Room.RoomStatus status);
    
    @EntityGraph(attributePaths = "building")
    @Query("SELECT r FROM Room r WHERE r.occupied < r.capacity AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRooms();
    
    @EntityGraph(attributePaths = "building")
    @Query("SELECT r FROM Room r WHERE r.building.type = :gender AND r.occupied < r.capacity AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRoomsByGender(Building.Gender gender);
    
    long countByOccupiedGreaterThan(int occupied);
    
    @Query("SELECT b.name, COUNT(r.id) FROM Room r JOIN r.building b GROUP BY b.id, b.name ORDER BY b.name")
    List<Object[]> countRoomsByBuilding();
}