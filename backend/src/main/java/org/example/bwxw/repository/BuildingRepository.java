package org.example.bwxw.repository;

import org.example.bwxw.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    
    Optional<Building> findByName(String name);
    
    List<Building> findByType(Building.Gender type);
    
    boolean existsByName(String name);
}
