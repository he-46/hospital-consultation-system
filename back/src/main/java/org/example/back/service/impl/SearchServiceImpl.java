package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.*;
import org.example.back.mapper.*;
import org.example.back.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl extends ServiceImpl<ArticleMapper, Article> implements SearchService {

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DiseaseMapper diseaseMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private HospitalDepartmentMapper hospitalDepartmentMapper;

    @Override
    public Map<String, Object> unifiedSearch(String keyword, String type, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        if ("all".equals(type)) {
            // 全部类型一起查
            Page<Hospital> hospitals = searchHospitals(keyword, pageNum, pageSize);
            Page<Doctor> doctors = searchDoctors(keyword, pageNum, pageSize);
            Page<Disease> diseases = searchDiseases(keyword, pageNum, pageSize);
            Page<Article> articles = searchArticles(keyword, pageNum, pageSize);

            result.put("hospitals", hospitals);
            result.put("doctors", doctors);
            result.put("diseases", diseases);
            result.put("articles", articles);
        } else if ("hospital".equals(type)) {
            result.put("hospitals", searchHospitals(keyword, pageNum, pageSize));
        } else if ("doctor".equals(type)) {
            result.put("doctors", searchDoctors(keyword, pageNum, pageSize));
        } else if ("disease".equals(type)) {
            result.put("diseases", searchDiseases(keyword, pageNum, pageSize));
        } else if ("article".equals(type)) {
            result.put("articles", searchArticles(keyword, pageNum, pageSize));
        }

        result.put("type", type);
        result.put("keyword", keyword);
        return result;
    }

    @Override
    public Page<Hospital> searchHospitals(String keyword, Integer pageNum, Integer pageSize) {
        Page<Hospital> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Hospital::getName, keyword)
                    .or().like(Hospital::getAddress, keyword));
        }

        wrapper.orderByDesc(Hospital::getFollowCount);
        Page<Hospital> result = hospitalMapper.selectPage(page, wrapper);

        // 填充科室标签
        fillDepartmentTags(result.getRecords());

        return result;
    }

    @Override
    public Page<Doctor> searchDoctors(String keyword, Integer pageNum, Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Doctor::getName, keyword)
                    .or().like(Doctor::getExpertise, keyword));
        }

        wrapper.orderByDesc(Doctor::getRating);
        Page<Doctor> result = doctorMapper.selectPage(page, wrapper);

        // 填充医院名和科室名
        fillDoctorNames(result.getRecords());

        return result;
    }

    @Override
    public Page<Disease> searchDiseases(String keyword, Integer pageNum, Integer pageSize) {
        Page<Disease> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Disease::getName, keyword)
                    .or().like(Disease::getDescription, keyword)
                    .or().like(Disease::getAlias, keyword));
        }

        wrapper.orderByDesc(Disease::getFollowCount);
        Page<Disease> result = diseaseMapper.selectPage(page, wrapper);

        // 填充科室名
        fillDepartmentNames(result.getRecords());

        return result;
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

        wrapper.orderByDesc(Article::getViews);
        return articleMapper.selectPage(page, wrapper);
    }

    /**
     * 填充医院列表的科室标签
     */
    private void fillDepartmentTags(List<Hospital> hospitals) {
        if (hospitals == null || hospitals.isEmpty()) return;

        Set<Long> hospitalIds = hospitals.stream().map(Hospital::getId).collect(Collectors.toSet());

        // 通过关联表查询每个医院关联的科室
        List<HospitalDepartment> links = hospitalDepartmentMapper.selectList(
                new LambdaQueryWrapper<HospitalDepartment>()
                        .in(HospitalDepartment::getHospitalId, hospitalIds)
        );

        if (links.isEmpty()) return;

        Set<Long> deptIds = links.stream()
                .map(HospitalDepartment::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> deptNameMap = departmentMapper.selectBatchIds(deptIds).stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .collect(Collectors.toMap(Department::getId, Department::getName));

        // 按医院分组
        Map<Long, List<String>> tagMap = links.stream()
                .collect(Collectors.groupingBy(
                        HospitalDepartment::getHospitalId,
                        Collectors.mapping(l -> deptNameMap.get(l.getDepartmentId()),
                                Collectors.filtering(Objects::nonNull, Collectors.toList()))
                ));

        for (Hospital hospital : hospitals) {
            hospital.setDepartmentTags(tagMap.getOrDefault(hospital.getId(), Collections.emptyList()));
        }
    }

    /**
     * 填充医生的医院名和科室名
     */
    private void fillDoctorNames(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) return;

        Set<Long> deptIds = doctors.stream()
                .map(Doctor::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> hospitalIds = doctors.stream()
                .map(Doctor::getHospitalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        Map<Long, String> hospitalMap = hospitalMapper.selectBatchIds(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getId, Hospital::getName));

        for (Doctor doctor : doctors) {
            doctor.setDepartmentName(deptMap.get(doctor.getDepartmentId()));
            doctor.setHospitalName(hospitalMap.get(doctor.getHospitalId()));
        }
    }

    /**
     * 填充疾病的科室名
     */
    private void fillDepartmentNames(List<Disease> diseases) {
        if (diseases == null || diseases.isEmpty()) return;

        Set<Long> deptIds = diseases.stream()
                .map(Disease::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        for (Disease disease : diseases) {
            disease.setDepartmentName(deptMap.get(disease.getDepartmentId()));
        }
    }
}
