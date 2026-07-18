package org.example.back.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.common.PageResult;
import org.example.back.common.Result;
import org.example.back.dto.review.ReviewSubmitDTO;
import org.example.back.entity.Review;
import org.example.back.service.ReviewService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    @PostMapping
    public Result<Void> submit(@Validated @RequestBody ReviewSubmitDTO dto){
        reviewService.submitReview(dto);
        return Result.success("评价提交成功",null);
    }

    @GetMapping
    public Result<PageResult<Review>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size
    ){
        IPage<Review> pageData = reviewService.getMyReviewPage(page, size);
        return Result.success(PageResult.of(pageData));
    }
}