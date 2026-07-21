package org.example.back.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Feedback;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * FeedbackService 集成测试
 * 规则：所有创建的反馈测试后立即删除
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeedbackServiceTest {

    @Autowired
    private FeedbackService feedbackService;

    private static final Long TEST_USER_ID = 1L;
    private static final List<Long> createdIds = new ArrayList<>();

    @AfterEach
    void cleanupEach() {
        // 每个测试后删除本轮创建的反馈
        if (!createdIds.isEmpty()) {
            for (Long id : createdIds) {
                feedbackService.removeById(id);
            }
            createdIds.clear();
        }
    }

    @Test @Order(1)
    void testSubmit_validFeedback() {
        Feedback feedback = new Feedback();
        feedback.setUserId(TEST_USER_ID);
        feedback.setFeedbackType(1);
        feedback.setContent("JUnit测试：登录页面加载慢");
        Assertions.assertTrue(feedbackService.submit(feedback));
        Assertions.assertNotNull(feedback.getId());
        Assertions.assertEquals(1, feedback.getStatus());
        createdIds.add(feedback.getId());
    }

    @Test @Order(2)
    void testSubmit_multipleTypes() {
        int[] types = {1, 2, 3, 4};
        for (int type : types) {
            Feedback f = new Feedback();
            f.setUserId(TEST_USER_ID);
            f.setFeedbackType(type);
            f.setContent("类型" + type + "的测试反馈");
            Assertions.assertTrue(feedbackService.submit(f));
            createdIds.add(f.getId());
        }
    }

    @Test @Order(3)
    void testGetMyList_returnsData() {
        // 先创建一条确保有数据
        Feedback f = new Feedback();
        f.setUserId(TEST_USER_ID);
        f.setFeedbackType(1);
        f.setContent("分页查询测试");
        feedbackService.submit(f);
        createdIds.add(f.getId());

        Page<Feedback> page = feedbackService.getMyList(1, 10, TEST_USER_ID);
        Assertions.assertNotNull(page);
        Assertions.assertTrue(page.getTotal() >= 1);
    }

    @Test @Order(4)
    void testGetMyList_timeOrderDesc() {
        Page<Feedback> page = feedbackService.getMyList(1, 50, TEST_USER_ID);
        List<Feedback> records = page.getRecords();
        if (records.size() >= 2) {
            for (int i = 1; i < records.size(); i++) {
                Assertions.assertTrue(
                    records.get(i - 1).getCreateTime()
                        .compareTo(records.get(i).getCreateTime()) >= 0);
            }
        }
    }

    @Test @Order(5)
    void testGetMyList_emptyForOtherUser() {
        Page<Feedback> page = feedbackService.getMyList(1, 10, 99999L);
        Assertions.assertEquals(0, page.getTotal());
    }

    @Test @Order(6)
    void testGetMyList_pageOutOfRange() {
        Page<Feedback> page = feedbackService.getMyList(999, 10, TEST_USER_ID);
        Assertions.assertTrue(page.getRecords().isEmpty());
    }

    @Test @Order(7)
    void testSubmit_emptyContent_shouldStillAccept() {
        Feedback f = new Feedback();
        f.setUserId(TEST_USER_ID);
        f.setFeedbackType(1);
        f.setContent("");
        // 反馈内容可能允许为空，由前端校验
        Assertions.assertTrue(feedbackService.submit(f));
        createdIds.add(f.getId());
    }
}
