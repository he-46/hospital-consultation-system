package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * 公开数据接口集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicDataServiceTest {

    @Autowired private HospitalService hospitalService;
    @Autowired private DoctorService doctorService;
    @Autowired private ArticleService articleService;
    @Autowired private DiseaseService diseaseService;

    @Test void testHotHospitals_returnsTop4() {
        List<Hospital> list = hospitalService.getHotHospitals(4);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() <= 4);
    }

    @Test void testHospitalDetail_validId() {
        Map<String, Object> detail = hospitalService.getHospitalDetail(1L);
        Assertions.assertNotNull(detail);
        Object hospital = detail.get("hospital");
        Assertions.assertNotNull(hospital, "医院详情应包含hospital对象");
    }

    @Test void testHospitalPage_notEmpty() {
        Page<Hospital> page = hospitalService.getHospitalPage(1, 10, null, null);
        Assertions.assertNotNull(page);
        Assertions.assertFalse(page.getRecords().isEmpty());
    }

    @Test void testHotDoctors_returnsTop4() {
        List<Doctor> list = doctorService.getHotDoctors(4);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() <= 4);
    }

    @Test void testDoctorDetail_validId() {
        Map<String, Object> detail = doctorService.getDoctorDetail(1L);
        Assertions.assertNotNull(detail);
        Object doctor = detail.get("doctor");
        Assertions.assertNotNull(doctor, "医生详情应包含doctor对象");
    }

    @Test void testDoctorPage_notEmpty() {
        Page<Doctor> page = doctorService.getDoctorPage(1, 10, null, null, null);
        Assertions.assertNotNull(page);
        Assertions.assertFalse(page.getRecords().isEmpty());
    }

    @Test void testDoctorReviews_notNull() {
        Page<Map<String, Object>> reviews = doctorService.getDoctorReviews(1L, 1, 10);
        Assertions.assertNotNull(reviews);
    }

    @Test void testArticlePage_notEmpty() {
        Page<Article> page = articleService.getArticlePage(1, 10, null);
        Assertions.assertNotNull(page);
        Assertions.assertFalse(page.getRecords().isEmpty());
    }

    @Test void testArticleDetail_validId() {
        Article article = articleService.getArticleDetail(1L);
        Assertions.assertNotNull(article);
        Assertions.assertNotNull(article.getTitle());
    }

    @Test void testArticleViewsIncrement() {
        Article before = articleService.getArticleDetail(1L);
        int v1 = before.getViews().intValue();
        Article after = articleService.getArticleDetail(1L);
        Assertions.assertEquals(v1 + 1, after.getViews().intValue(), "阅读量应+1");
    }

    @Test void testHotArticles_returnsTop3() {
        List<Article> list = articleService.getHotArticles(3);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() <= 3);
    }

    @Test void testDiseasePage_notEmpty() {
        Page<Disease> page = diseaseService.getDiseasePage(1, 10, null);
        Assertions.assertNotNull(page);
        Assertions.assertFalse(page.getRecords().isEmpty());
    }

    @Test void testDiseaseDetail_validId() {
        Disease disease = diseaseService.getDiseaseDetail(1L);
        Assertions.assertNotNull(disease);
        Assertions.assertNotNull(disease.getName());
    }

    @Test void testHotDiseases_returnsTop4() {
        List<Disease> list = diseaseService.getHotDiseases(4);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() <= 4);
    }

    @Test void testDiseaseSearchByKeyword() {
        Page<Disease> page = diseaseService.searchDiseases("糖尿病", 1, 10);
        Assertions.assertNotNull(page);
        Assertions.assertTrue(page.getTotal() >= 0, "搜索疾病应有分页结果");
    }

    @Test void testArticleSearchByKeyword() {
        Page<Article> page = articleService.searchArticles("健康", 1, 10);
        Assertions.assertNotNull(page);
    }

    @Test void testHospitalSearchByKeyword() {
        Page<Hospital> page = hospitalService.searchHospitals("人民", 1, 10);
        Assertions.assertNotNull(page);
    }

    @Test void testDoctorSearchByKeyword() {
        Page<Doctor> page = doctorService.searchDoctors("张", null, 1, 10);
        Assertions.assertNotNull(page);
    }
}
