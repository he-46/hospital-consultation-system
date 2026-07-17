package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Feedback;
import org.example.back.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // 提交反馈：POST /feedback/submit
    @PostMapping("/submit")
    public Result<?> submit(HttpServletRequest request, @RequestBody Feedback feedback) {
        Long userId = (Long) request.getAttribute("userId");
        feedback.setUserId(userId);
        feedbackService.submit(feedback);
        return Result.success("反馈提交成功");
    }

    // 我的反馈列表：GET /feedback/myList?pageNum=1&pageSize=10
    @GetMapping("/myList")
    public Result<?> myList(HttpServletRequest request,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Feedback> page = feedbackService.getMyList(pageNum, pageSize, userId);
        return Result.success(page);
    }
}
