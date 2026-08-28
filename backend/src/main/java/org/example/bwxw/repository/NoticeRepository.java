package org.example.bwxw.repository;

import org.example.bwxw.entity.Notice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    @EntityGraph(attributePaths = "publisher")
    List<Notice> findByType(Notice.NoticeType type);
    
    @EntityGraph(attributePaths = "publisher")
    List<Notice> findByTarget(Notice.NoticeTarget target);
    
    @EntityGraph(attributePaths = "publisher")
    List<Notice> findByPinnedTrue();
    
    @EntityGraph(attributePaths = "publisher")
    List<Notice> findByPublisherId(Long publisherId);
    
    @EntityGraph(attributePaths = "publisher")
    @Query("SELECT n FROM Notice n WHERE n.target = :target OR n.target = 'ALL' ORDER BY n.pinned DESC, n.publishedAt DESC")
    List<Notice> findByTargetOrAllOrderByPinnedAndDate(Notice.NoticeTarget target);
    
    @EntityGraph(attributePaths = "publisher")
    @Query("SELECT n FROM Notice n ORDER BY n.pinned DESC, n.publishedAt DESC")
    List<Notice> findAllOrderByPinnedAndDate();
}