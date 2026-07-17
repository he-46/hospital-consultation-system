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

    // 消息列表：GET /message/list?pageNum=1&pageSize=10&isRead=0
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) Integer isRead) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Message> page = messageService.getList(pageNum, pageSize, userId, isRead);
        return Result.success(page);
    }

    // 标记已读：PUT /message/1/read
    @PutMapping("/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success("已标记为已读");
    }
}
