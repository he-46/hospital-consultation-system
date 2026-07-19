package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Article;
import org.example.back.entity.Department;
import org.example.back.mapper.ArticleMapper;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Page<Article> getArticlePage(Integer pageNum, Integer pageSize, Long departmentId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);

        if (departmentId != null && departmentId > 0) {
            // 展开一级科室为所有子科室
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(departmentId);
            List<Department> children = departmentMapper.selectList(
                    new LambdaQueryWrapper<Department>()
                            .eq(Department::getParentId, departmentId)
                            .eq(Department::getStatus, 1));
            for (Department child : children) {
                deptIds.add(child.getId());
            }
            wrapper.in(Article::getDepartmentId, deptIds);
        }
        
        wrapper.orderByDesc(Article::getPublishTime);
        return this.page(page, wrapper);
    }

    @Override
    public java.util.List<Article> getHotArticles(Integer limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .orderByDesc(Article::getViews)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public Article getArticleDetail(Long id) {
        Article article = this.getById(id);
        if (article != null) {
            // 增加阅读量
            article.setViews(article.getViews() + 1);
            this.updateById(article);
        }
        return article;
    }

    @Override
    public Page<Article> searchArticles(String keyword, Integer pageNum, Integer pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or().like(Article::getSummary, keyword));
        }
        
        wrapper.orderByDesc(Article::getPublishTime);
        return this.page(page, wrapper);
    }
}
