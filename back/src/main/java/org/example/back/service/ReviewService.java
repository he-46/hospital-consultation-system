package org.example.back.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.dto.review.ReviewSubmitDTO;
import org.example.back.entity.Review;

public interface ReviewService {
    // 提交评价
    void submitReview(ReviewSubmitDTO dto);
    // 我的评价分页
    IPage<Review> getMyReviewPage(Long page, Long size);
}