package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Disease;

import java.util.List;

public interface DiseaseService {
    
    /**
     * 获取疾病列表（分页）
     */
    Page<Disease> getDiseasePage(Integer pageNum, Integer pageSize, Long departmentId);
    
    /**
     * 获取热门疾病
     */
    List<Disease> getHotDiseases(Integer limit);
    
    /**
     * 获取疾病详情
     */
    Disease getDiseaseDetail(Long id);
    
    /**
     * 搜索疾病
     */
    Page<Disease> searchDiseases(String keyword, Integer pageNum, Integer pageSize);
}
