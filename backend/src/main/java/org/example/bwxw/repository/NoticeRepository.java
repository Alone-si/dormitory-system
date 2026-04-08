package org.example.bwxw.repository;

import org.example.bwxw.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    List<Notice> findByType(Notice.NoticeType type);
    
    List<Notice> findByTarget(Notice.NoticeTarget target);
    
    List<Notice> findByPinnedTrue();
    
    List<Notice> findByPublisherId(Long publisherId);
    
    @Query("SELECT n FROM Notice n WHERE n.target = :target OR n.target = 'ALL' ORDER BY n.pinned DESC, n.publishedAt DESC")
    List<Notice> findByTargetOrAllOrderByPinnedAndDate(Notice.NoticeTarget target);
    
    @Query("SELECT n FROM Notice n ORDER BY n.pinned DESC, n.publishedAt DESC")
    List<Notice> findAllOrderByPinnedAndDate();
}
