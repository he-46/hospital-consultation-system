package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Message;
import org.example.back.mapper.MessageMapper;
import org.example.back.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Page<Message> pageByUser(int pageNum, int pageSize, Long userId, Integer isRead) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> page = new Page<>(pageNum, pageSize);
        return this.page(page, wrapper);
    }

    @Override
    public void markRead(Long messageId) {
        Message msg = new Message();
        msg.setId(messageId);
        msg.setIsRead(1);
        this.updateById(msg);
    }

    @Override
    public void sendSystemMsg(Long userId, String title, String content) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        this.save(msg);
    }

    @Override
    public long countUnread(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        return this.count(wrapper);
    }
}
