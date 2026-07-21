package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.entity.Article;
import org.example.back.entity.Disease;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.service.ArticleService;
import org.example.back.service.DiseaseService;
import org.example.back.service.DoctorService;
import org.example.back.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DiseaseService diseaseService;

    @Autowired
    private ArticleService articleService;

    @GetMapping("/data")
    public Result<?> indexData() {
        Map<String, Object> data = new HashMap<>();

        List<Hospital> hospitals = hospitalService.getHotHospitals(4);
        List<Doctor> doctors = doctorService.getHotDoctors(4);
        List<Disease> diseases = diseaseService.getHotDiseases(4);
        List<Article> articles = articleService.getHotArticles(3);

        data.put("hospitals", hospitals);
        data.put("doctors", doctors);
        data.put("diseases", diseases);
        data.put("articles", articles);

        return Result.success(data);
    }
}
