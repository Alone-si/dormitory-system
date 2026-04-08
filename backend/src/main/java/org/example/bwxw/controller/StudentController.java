package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.example.bwxw.service.StudentArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private StudentArchiveService studentArchiveService;
    
    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<User>> getAllStudents() {
        try {
            List<User> students = userRepository.findByRoleOrderByStudentIdAsc(User.UserRole.STUDENT);
            // 强制加载关联数据，避免懒加载问题
            students.forEach(student -> {
                if (student.getRoom() != null) {
                    student.getRoom().getBuilding().getName(); // 触发加载
                }
            });
            return ApiResponse.success(students);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<User> getStudentById(@PathVariable Long id) {
        try {
            User student = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("学生不存在"));
            if (student.getRole() != User.UserRole.STUDENT) {
                return ApiResponse.error("该用户不是学生");
            }
            return ApiResponse.success(student);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    @Transactional
    public ApiResponse<User> createStudent(@RequestBody Map<String, Object> params) {
        try {
            String studentId = (String) params.get("studentId");
            
            if (studentId == null || studentId.length() < 4) {
                return ApiResponse.error("学号格式不正确");
            }
            
            String yearStr = studentId.substring(0, 4);
            int studentYear;
            try {
                studentYear = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                return ApiResponse.error("学号前4位必须是年份");
            }
            
            int currentYear = java.time.Year.now().getValue();
            int minYear = currentYear - 2;
            
            if (studentYear < minYear || studentYear > currentYear) {
                return ApiResponse.error("学号年份必须在 " + minYear + " 至 " + currentYear + " 之间（3年学制）");
            }
            
            String username = studentId;

            if (userRepository.existsByUsername(username)) {
                return ApiResponse.error("学号对应的用户名已存在");
            }
            if (userRepository.existsByStudentId(studentId)) {
                return ApiResponse.error("学号已存在");
            }

            // Create User - 自动生成账号
            User user = new User();
            user.setUsername(username); // 登录用户名 = 学号
            user.setDisplayName(studentId); // 显示用户名默认为学号，用户可后续修改
            user.setName((String) params.get("name"));
            user.setStudentId(studentId);
            user.setRole(User.UserRole.STUDENT);
            String genderStr = (String) params.get("gender");
            user.setGender(User.Gender.valueOf(genderStr));
            user.setPhone((String) params.get("phone"));
            
            if (params.containsKey("className")) {
                String newClassName = (String) params.get("className");
                if (newClassName != null && !newClassName.trim().isEmpty()) {
                    long classCount = userRepository.countByClassName(newClassName);
                    if (classCount >= 30) {
                        return ApiResponse.error("该班级人数已达上限（30人），无法添加");
                    }
                }
                user.setClassName(newClassName);
            }
            
            String password = "123456";
            user.setPassword(password);
            user.setStatus("ACTIVE");
            
            User savedUser = userRepository.save(user);
            return ApiResponse.success(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<User> updateStudent(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("学生不存在"));
            
            if (user.getRole() != User.UserRole.STUDENT) {
                return ApiResponse.error("该用户不是学生");
            }
            
            if (params.containsKey("className")) {
                String newClassName = (String) params.get("className");
                String oldClassName = user.getClassName();
                
                if (newClassName != null && !newClassName.trim().isEmpty() && !newClassName.equals(oldClassName)) {
                    long classCount = userRepository.countByClassName(newClassName);
                    if (classCount >= 30) {
                        return ApiResponse.error("该班级人数已达上限（30人），无法修改");
                    }
                }
                user.setClassName(newClassName);
            }
            
            if (params.containsKey("name")) {
                user.setName((String) params.get("name"));
            }
            
            if (params.containsKey("phone")) {
                user.setPhone((String) params.get("phone"));
            }
            
            if (params.containsKey("gender")) {
                String genderStr = (String) params.get("gender");
                user.setGender(User.Gender.valueOf(genderStr));
            }
            
            if (params.containsKey("password")) {
                String password = (String) params.get("password");
                if (password != null && !password.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                }
            }
            
            if (params.containsKey("displayName")) {
                String displayName = (String) params.get("displayName");
                // 允许displayName为空，这样前端会显示默认的学号
                user.setDisplayName(displayName != null && !displayName.trim().isEmpty() ? displayName.trim() : null);
            }
            
            // 注意：email 字段不在此处理
            // 邮箱只能由学生本人通过 PUT /api/users/email 端点修改
            // 管理员无权修改学生邮箱，即使请求中包含 email 字段也会被忽略

            User updatedUser = userRepository.save(user);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/checkin")
    @Transactional
    public ApiResponse<String> checkInStudent(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (user.getRole() != User.UserRole.STUDENT) {
                return ApiResponse.error("该用户不是学生");
            }
            
            // 将学生状态设置为ACTIVE（入住）
            user.setStatus("ACTIVE");
            userRepository.save(user);
            
            return ApiResponse.success("办理入住成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("办理入住失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/checkout")
    @Transactional
    public ApiResponse<String> checkOutStudent(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (user.getRole() != User.UserRole.STUDENT) {
                return ApiResponse.error("该用户不是学生");
            }
            
            // 将学生状态设置为INACTIVE（退宿）并清除宿舍分配
            user.setStatus("INACTIVE");
            user.setRoom(null);
            userRepository.save(user);
            
            return ApiResponse.success("办理退宿成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("办理退宿失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch-import")
    @Transactional
    public ApiResponse<String> batchImportStudents(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> studentList = (List<Map<String, Object>>) params.get("students");
            
            if (studentList == null || studentList.isEmpty()) {
                return ApiResponse.error("学生数据不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();
            
            for (Map<String, Object> studentData : studentList) {
                try {
                    String studentId = (String) studentData.get("studentId");
                    String name = (String) studentData.get("name");
                    String genderStr = (String) studentData.get("gender");
                    String phone = (String) studentData.get("phone");
                    String className = (String) studentData.get("className");
                    
                    // 验证必填字段
                    if (studentId == null || studentId.trim().isEmpty()) {
                        failCount++;
                        errorMessages.append("学号不能为空; ");
                        continue;
                    }
                    if (name == null || name.trim().isEmpty()) {
                        failCount++;
                        errorMessages.append("姓名不能为空; ");
                        continue;
                    }
                    if (genderStr == null || genderStr.trim().isEmpty()) {
                        failCount++;
                        errorMessages.append("性别不能为空; ");
                        continue;
                    }
                    
                    if (userRepository.existsByStudentId(studentId)) {
                        failCount++;
                        errorMessages.append("学号 ").append(studentId).append(" 已存在; ");
                        continue;
                    }
                    
                    if (className != null && !className.trim().isEmpty()) {
                        long classCount = userRepository.countByClassName(className);
                        if (classCount >= 30) {
                            failCount++;
                            errorMessages.append("班级 ").append(className).append(" 人数已达上限（30人）; ");
                            continue;
                        }
                    }
                    
                    User user = new User();
                    user.setUsername(studentId);
                    user.setDisplayName(studentId);
                    user.setName(name);
                    user.setStudentId(studentId);
                    user.setRole(User.UserRole.STUDENT);
                    user.setGender(User.Gender.valueOf(genderStr.toUpperCase()));
                    user.setPhone(phone);
                    user.setClassName(className);
                    user.setPassword("123456");
                    user.setStatus("ACTIVE");
                    
                    userRepository.save(user);
                    successCount++;
                    
                } catch (Exception e) {
                    failCount++;
                    errorMessages.append("导入失败: ").append(e.getMessage()).append("; ");
                }
            }
            
            String result = String.format("批量导入完成！成功: %d 人，失败: %d 人", successCount, failCount);
            if (errorMessages.length() > 0) {
                result += "。错误信息: " + errorMessages.toString();
            }
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("批量导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动归档单个学生（仅超级管理员）
     */
    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archiveStudent(@PathVariable Long id) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (currentUser.getRole() != User.UserRole.ADMIN || 
                currentUser.getAdminType() != User.AdminType.SUPER_ADMIN) {
                return ApiResponse.error("权限不足：仅超级管理员可以归档学生");
            }
            
            studentArchiveService.archiveStudent(id);
            return ApiResponse.success("学生已归档为毕业状态", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 批量归档过期学生（仅超级管理员）
     */
    @PostMapping("/batch-archive")
    public ApiResponse<String> batchArchiveStudents() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (currentUser.getRole() != User.UserRole.ADMIN || 
                currentUser.getAdminType() != User.AdminType.SUPER_ADMIN) {
                return ApiResponse.error("权限不足：仅超级管理员可以批量归档学生");
            }
            
            int count = studentArchiveService.batchArchiveGraduatedStudents();
            return ApiResponse.success("批量归档完成，共归档 " + count + " 名学生");
        } catch (Exception e) {
            return ApiResponse.error("批量归档失败: " + e.getMessage());
        }
    }
}
