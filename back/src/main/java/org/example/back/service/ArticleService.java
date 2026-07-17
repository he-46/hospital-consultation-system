package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Article;

import java.util.List;

public interface ArticleService {
    
    /**
     * 获取文章列表（分页）
     */
    Page<Article> getArticlePage(Integer pageNum, Integer pageSize, Long departmentId);
    
    /**
     * 获取热门文章
     */
    List<Article> getHotArticles(Integer limit);
    
    /**
     * 获取文章详情
     */
    Article getArticleDetail(Long id);
    
    /**
     * 搜索文章
     */
    Page<Article> searchArticles(String keyword, Integer pageNum, Integer pageSize);
}
