package org.example.bwxw.repository;

import org.example.bwxw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByStudentId(String studentId);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByRoleOrderByStudentIdAsc(User.UserRole role);
    
    List<User> findByRoomId(Long roomId);
    
    List<User> findByGenderAndRole(User.Gender gender, User.UserRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByStudentId(String studentId);
    
    long countByRole(User.UserRole role);
    
    long countByClassName(String className);
}
