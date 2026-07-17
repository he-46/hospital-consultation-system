package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Department;
import org.example.back.entity.Disease;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.mapper.DiseaseMapper;
import org.example.back.service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiseaseServiceImpl extends ServiceImpl<DiseaseMapper, Disease> implements DiseaseService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Page<Disease> getDiseasePage(Integer pageNum, Integer pageSize, Long departmentId) {
        Page<Disease> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        
        if (departmentId != null && departmentId > 0) {
            wrapper.eq(Disease::getDepartmentId, departmentId);
        }
        
        wrapper.orderByDesc(Disease::getFollowCount);
        Page<Disease> result = this.page(page, wrapper);
        
        // 填充科室名称
        fillDepartmentName(result.getRecords());
        
        return result;
    }

    @Override
    public List<Disease> getHotDiseases(Integer limit) {
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Disease::getFollowCount)
               .last("LIMIT " + limit);
        List<Disease> diseases = this.list(wrapper);
        
        fillDepartmentName(diseases);
        return diseases;
    }

    @Override
    public Disease getDiseaseDetail(Long id) {
        Disease disease = this.getById(id);
        if (disease != null && disease.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(disease.getDepartmentId());
            if (department != null) {
                disease.setDepartmentName(department.getName());
            }
        }
        return disease;
    }

    @Override
    public Page<Disease> searchDiseases(String keyword, Integer pageNum, Integer pageSize) {
        Page<Disease> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Disease::getName, keyword)
                    .or().like(Disease::getAlias, keyword));
        }
        
        wrapper.orderByDesc(Disease::getFollowCount);
        Page<Disease> result = this.page(page, wrapper);
        
        fillDepartmentName(result.getRecords());
        return result;
    }
    
    private void fillDepartmentName(List<Disease> diseases) {
        if (diseases == null || diseases.isEmpty()) {
            return;
        }
        
        List<Long> deptIds = diseases.stream()
                .map(Disease::getDepartmentId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        
        if (deptIds.isEmpty()) {
            return;
        }
        
        Map<Long, String> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        
        for (Disease disease : diseases) {
            if (disease.getDepartmentId() != null) {
                disease.setDepartmentName(deptMap.get(disease.getDepartmentId()));
            }
        }
    }
}
