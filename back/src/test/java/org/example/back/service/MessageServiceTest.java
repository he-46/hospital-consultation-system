package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Message;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MessageService 集成测试
 * 覆盖：分页查询（已读/未读筛选）、标记已读、系统消息发送、未读数统计、边界情况
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    private static final Long TEST_USER_ID = 7L;

    @Test @Order(1)
    void testPageByUser_noFilter() {
        Page<Message> page = messageService.pageByUser(1, 10, TEST_USER_ID, null);
        Assertions.assertNotNull(page);
        Assertions.assertTrue(page.getTotal() >= 0);
    }

    @Test @Order(2)
    void testPageByUser_filterUnread() {
        Page<Message> page = messageService.pageByUser(1, 10, TEST_USER_ID, 0);
        for (Message m : page.getRecords()) {
            Assertions.assertEquals(0, m.getIsRead(), "筛选未读时isRead应为0");
        }
    }

    @Test @Order(3)
    void testPageByUser_filterRead() {
        Page<Message> page = messageService.pageByUser(1, 10, TEST_USER_ID, 1);
        for (Message m : page.getRecords()) {
            Assertions.assertEquals(1, m.getIsRead(), "筛选已读时isRead应为1");
        }
    }

    @Test @Order(4)
    void testMarkRead() {
        Page<Message> unread = messageService.pageByUser(1, 1, TEST_USER_ID, 0);
        if (!unread.getRecords().isEmpty()) {
            Message msg = unread.getRecords().get(0);
            Assertions.assertDoesNotThrow(() -> messageService.markRead(msg.getId()));
        }
    }

    @Test @Order(5)
    void testSendSystemMsg() {
        messageService.sendSystemMsg(TEST_USER_ID, "测试标题", "测试内容");
        // 验证消息已入库
        Page<Message> page = messageService.pageByUser(1, 1, TEST_USER_ID, null);
        Assertions.assertTrue(page.getTotal() > 0);
        Message latest = page.getRecords().get(0);
        Assertions.assertEquals("测试标题", latest.getTitle());
        Assertions.assertEquals(0, latest.getIsRead());
        // 清理
        messageService.removeById(latest.getId());
    }

    @Test @Order(6)
    void testCountUnread() {
        // 先发一条确保有未读
        messageService.sendSystemMsg(TEST_USER_ID, "未读数测试", "内容");
        long count = messageService.countUnread(TEST_USER_ID);
        Assertions.assertTrue(count > 0);
        // 清理
        Page<Message> page = messageService.pageByUser(1, 1, TEST_USER_ID, 0);
        if (!page.getRecords().isEmpty()) {
            messageService.removeById(page.getRecords().get(0).getId());
        }
    }

    @Test @Order(7)
    void testPageByUser_otherUser_empty() {
        Page<Message> page = messageService.pageByUser(1, 10, 99999L, null);
        Assertions.assertEquals(0, page.getTotal());
    }

    @Test @Order(8)
    void testPageByUser_timeOrderDesc() {
        Page<Message> page = messageService.pageByUser(1, 50, TEST_USER_ID, null);
        if (page.getRecords().size() >= 2) {
            for (int i = 1; i < page.getRecords().size(); i++) {
                Assertions.assertTrue(
                    page.getRecords().get(i - 1).getCreateTime()
                        .compareTo(page.getRecords().get(i).getCreateTime()) >= 0,
                    "消息应按创建时间倒序");
            }
        }
    }
}
