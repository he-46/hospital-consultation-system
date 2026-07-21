package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.back.entity.Message;

public interface MessageService extends IService<Message> {
    // 用户分页查消息（支持已读/未读筛选）
    Page<Message> pageByUser(int pageNum, int pageSize, Long userId, Integer isRead);
    // 标记为已读
    void markRead(Long messageId);
    // 发送系统消息
    void sendSystemMsg(Long userId, String title, String content);
    // 统计未读数量
    long countUnread(Long userId);
}
