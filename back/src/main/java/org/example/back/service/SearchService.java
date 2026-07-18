package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Article;
import org.example.back.entity.Disease;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;

import java.util.Map;

public interface SearchService {

    /**
     * 统一搜索
     * @param keyword 关键词
     * @param type 搜索类型: all/hospital/doctor/disease/article
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 根据type返回不同结果，all返回包含所有类型的Map
     */
    Map<String, Object> unifiedSearch(String keyword, String type, Integer pageNum, Integer pageSize);

    /**
     * 搜索医院
     */
    Page<Hospital> searchHospitals(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 搜索医生
     */
    Page<Doctor> searchDoctors(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 搜索疾病
     */
    Page<Disease> searchDiseases(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 搜索文章
     */
    Page<Article> searchArticles(String keyword, Integer pageNum, Integer pageSize);
}
