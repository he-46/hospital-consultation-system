package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 2-7 统一搜索控制器
 * 关键词跨 4 张表模糊搜索（医院名/医生名+擅长/疾病名/文章标题）
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 多类型统一搜索 - GET /api/search
     * @param keyword 关键词
     * @param type 搜索类型: all(默认)/hospital/doctor/disease/article
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    @GetMapping
    public Result<?> unifiedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = searchService.unifiedSearch(keyword, type, pageNum, pageSize);
        return Result.success(result);
    }
}
