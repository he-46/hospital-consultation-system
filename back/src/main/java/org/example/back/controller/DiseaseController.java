package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Disease;
import org.example.back.service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 疾病控制器
 */
@RestController
@RequestMapping("/disease")
public class DiseaseController {

    @Autowired
    private DiseaseService diseaseService;

    /**
     * 获取疾病列表
     */
    @GetMapping("/list")
    public Result<?> getDiseaseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId) {
        Page<Disease> page = diseaseService.getDiseasePage(pageNum, pageSize, departmentId);
        return Result.success(page);
    }

    /**
     * 获取热门疾病
     */
    @GetMapping("/hot")
    public Result<?> getHotDiseases(@RequestParam(defaultValue = "8") Integer limit) {
        List<Disease> diseases = diseaseService.getHotDiseases(limit);
        return Result.success(diseases);
    }

    /**
     * 获取疾病详情
     */
    @GetMapping("/detail")
    public Result<?> getDiseaseDetail(@RequestParam Long id) {
        Disease disease = diseaseService.getDiseaseDetail(id);
        return Result.success(disease);
    }

    /**
     * 搜索疾病
     */
    @GetMapping("/search")
    public Result<?> searchDiseases(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Disease> page = diseaseService.searchDiseases(keyword, pageNum, pageSize);
        return Result.success(page);
    }
}
