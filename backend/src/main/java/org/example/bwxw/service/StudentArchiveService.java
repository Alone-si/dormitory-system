package org.example.bwxw.service;

import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Year;
import java.util.List;

@Service
public class StudentArchiveService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 自动归档过期学生
     * 每天凌晨2点执行
     * 将超过3年学制的学生标记为已毕业
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void archiveGraduatedStudents() {
        int currentYear = Year.now().getValue();
        int graduationYear = currentYear - 3; // 3年学制
        
        List<User> students = userRepository.findByRole(User.UserRole.STUDENT);
        int archivedCount = 0;
        
        for (User student : students) {
            if (student.getStudentId() != null && student.getStudentId().length() >= 4) {
                try {
                    String yearStr = student.getStudentId().substring(0, 4);
                    int enrollmentYear = Integer.parseInt(yearStr);
                    
                    // 如果入学年份 <= 毕业年份，且状态为ACTIVE，则标记为已毕业
                    if (enrollmentYear <= graduationYear && "ACTIVE".equals(student.getStatus())) {
                        student.setStatus("GRADUATED");
                        
                        // 如果有宿舍，自动退寝
                        if (student.getRoom() != null) {
                            var room = student.getRoom();
                            room.setOccupied(room.getOccupied() - 1);
                            if (room.getOccupied() < room.getCapacity()) {
                                room.setStatus(org.example.bwxw.entity.Room.RoomStatus.AVAILABLE);
                            }
                            student.setRoom(null);
                        }
                        
                        userRepository.save(student);
                        archivedCount++;
                    }
                } catch (NumberFormatException e) {
                    // 学号格式不正确，跳过
                    continue;
                }
            }
        }
        
        System.out.println("自动归档完成：共归档 " + archivedCount + " 名学生");
    }
    
    /**
     * 手动归档指定学生
     */
    @Transactional
    public void archiveStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        if (student.getRole() != User.UserRole.STUDENT) {
            throw new RuntimeException("该用户不是学生");
        }
        
        if ("GRADUATED".equals(student.getStatus())) {
            throw new RuntimeException("该学生已经是毕业状态");
        }
        
        student.setStatus("GRADUATED");
        
        // 如果有宿舍，自动退寝
        if (student.getRoom() != null) {
            var room = student.getRoom();
            room.setOccupied(room.getOccupied() - 1);
            if (room.getOccupied() < room.getCapacity()) {
                room.setStatus(org.example.bwxw.entity.Room.RoomStatus.AVAILABLE);
            }
            student.setRoom(null);
        }
        
        userRepository.save(student);
    }
    
    /**
     * 批量归档过期学生
     */
    @Transactional
    public int batchArchiveGraduatedStudents() {
        int currentYear = Year.now().getValue();
        int graduationYear = currentYear - 3;
        
        List<User> students = userRepository.findByRole(User.UserRole.STUDENT);
        int archivedCount = 0;
        
        for (User student : students) {
            if (student.getStudentId() != null && student.getStudentId().length() >= 4) {
                try {
                    String yearStr = student.getStudentId().substring(0, 4);
                    int enrollmentYear = Integer.parseInt(yearStr);
                    
                    if (enrollmentYear <= graduationYear && "ACTIVE".equals(student.getStatus())) {
                        student.setStatus("GRADUATED");
                        
                        if (student.getRoom() != null) {
                            var room = student.getRoom();
                            room.setOccupied(room.getOccupied() - 1);
                            if (room.getOccupied() < room.getCapacity()) {
                                room.setStatus(org.example.bwxw.entity.Room.RoomStatus.AVAILABLE);
                            }
                            student.setRoom(null);
                        }
                        
                        userRepository.save(student);
                        archivedCount++;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        return archivedCount;
    }
}
