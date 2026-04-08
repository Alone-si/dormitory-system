package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.Building;
import org.example.bwxw.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BuildingController {
    private final BuildingService buildingService;
    
    @GetMapping
    public ApiResponse<List<Building>> getAllBuildings() {
        try {
            List<Building> buildings = buildingService.getAllBuildings();
            return ApiResponse.success(buildings);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Building> getBuildingById(@PathVariable Long id) {
        try {
            Building building = buildingService.getBuildingById(id);
            return ApiResponse.success(building);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Building> createBuilding(@RequestBody Building building) {
        try {
            Building created = buildingService.createBuilding(building);
            return ApiResponse.success("宿舍楼创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBuilding(@PathVariable Long id) {
        try {
            buildingService.deleteBuilding(id);
            return ApiResponse.success("宿舍楼删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
