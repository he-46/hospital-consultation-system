package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Message;
import org.example.back.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 获取我的消息列表
    @GetMapping("/list")
    public Result<Page<Message>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer isRead,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Message> page = messageService.pageByUser(pageNum, pageSize, userId, isRead);
        return Result.success(page);
    }

    // 标记为已读
    @PutMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Message message = messageService.getById(id);
        if (message == null) {
            return Result.error("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        messageService.markRead(id);
        return Result.success("已标记为已读");
    }

    // 获取未读消息数量
    @GetMapping("/unread/count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = messageService.countUnread(userId);
        return Result.success(count);
    }
}
