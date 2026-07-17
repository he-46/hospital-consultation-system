package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.back.entity.Feedback;

public interface FeedbackService extends IService<Feedback> {

    // 提交反馈
    boolean submit(Feedback feedback);

    // 我的反馈列表（分页）
    Page<Feedback> getMyList(Integer pageNum, Integer pageSize, Long userId);
}
