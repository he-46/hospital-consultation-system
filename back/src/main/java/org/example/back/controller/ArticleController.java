package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Article;
import org.example.back.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 获取文章列表
     */
    @GetMapping("/list")
    public Result<?> getArticleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId) {
        Page<Article> page = articleService.getArticlePage(pageNum, pageSize, departmentId);
        return Result.success(page);
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public Result<?> getHotArticles(@RequestParam(defaultValue = "5") Integer limit) {
        List<Article> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/detail")
    public Result<?> getArticleDetail(@RequestParam Long id) {
        Article article = articleService.getArticleDetail(id);
        return Result.success(article);
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public Result<?> searchArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Article> page = articleService.searchArticles(keyword, pageNum, pageSize);
        return Result.success(page);
    }
}
