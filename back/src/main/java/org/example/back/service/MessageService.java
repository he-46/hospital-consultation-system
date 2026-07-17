package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.back.entity.Message;

public interface MessageService extends IService<Message> {

    // 消息列表（支持按已读/未读筛选）
    Page<Message> getList(Integer pageNum, Integer pageSize, Long userId, Integer isRead);

    // 标记已读
    boolean markRead(Long id);
}
