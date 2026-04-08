package org.example.bwxw.service;

import org.example.bwxw.entity.Notice;
import org.example.bwxw.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoticeService {
    
    @Autowired
    private NoticeRepository noticeRepository;
    
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllOrderByPinnedAndDate();
    }
    
    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
    }
    
    public List<Notice> getNoticesByTarget(Notice.NoticeTarget target) {
        return noticeRepository.findByTargetOrAllOrderByPinnedAndDate(target);
    }
    
    public Notice createNotice(Notice notice) {
        return noticeRepository.save(notice);
    }
    
    public Notice updateNotice(Long id, Notice noticeDetails) {
        Notice notice = getNoticeById(id);
        notice.setTitle(noticeDetails.getTitle());
        notice.setContent(noticeDetails.getContent());
        notice.setType(noticeDetails.getType());
        notice.setPriority(noticeDetails.getPriority());
        notice.setTarget(noticeDetails.getTarget());
        notice.setPinned(noticeDetails.getPinned());
        return noticeRepository.save(notice);
    }
    
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
