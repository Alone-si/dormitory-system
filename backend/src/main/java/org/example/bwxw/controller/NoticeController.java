package org.example.bwxw.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.Notice;
import org.example.bwxw.entity.User;
import org.example.bwxw.service.CurrentUserService;
import org.example.bwxw.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private CurrentUserService currentUserService;
    
    @GetMapping
    public ApiResponse<List<Notice>> getAllNotices() {
        try {
            List<Notice> notices = noticeService.getAllNotices();
            return ApiResponse.success(notices);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Notice> getNoticeById(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getNoticeById(id);
            return ApiResponse.success(notice);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/target/{target}")
    public ApiResponse<List<Notice>> getNoticesByTarget(@PathVariable Notice.NoticeTarget target) {
        try {
            List<Notice> notices = noticeService.getNoticesByTarget(target);
            return ApiResponse.success(notices);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Notice> createNotice(@RequestBody Notice notice) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            notice.setId(null);
            notice.setPublisher(currentUser);
            if (notice.getTarget() == null) {
                notice.setTarget(Notice.NoticeTarget.ALL);
            }
            if (notice.getPinned() == null) {
                notice.setPinned(false);
            }
            Notice created = noticeService.createNotice(notice);
            return ApiResponse.success("通知发布成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Notice> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        try {
            Notice updated = noticeService.updateNotice(id, notice);
            return ApiResponse.success("通知更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        try {
            noticeService.deleteNotice(id);
            return ApiResponse.success("通知删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
