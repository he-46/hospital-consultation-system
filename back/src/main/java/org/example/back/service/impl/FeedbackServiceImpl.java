package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Feedback;
import org.example.back.mapper.FeedbackMapper;
import org.example.back.service.FeedbackService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    public boolean submit(Feedback feedback) {
        if (feedback.getUserId() == null) {
            throw new RuntimeException("请先登录");
        }
        feedback.setStatus(1);                    // 初始状态：待处理
        feedback.setCreateTime(LocalDateTime.now());
        return save(feedback);
    }

    @Override
    public Page<Feedback> getMyList(Integer pageNum, Integer pageSize, Long userId) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId);
        wrapper.orderByDesc(Feedback::getCreateTime);
        return page(page, wrapper);
    }
}
