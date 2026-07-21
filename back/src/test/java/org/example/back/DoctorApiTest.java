package org.example.back;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 医生+排班模块 API 集成测试。
 * 一键运行即可验证全部 8 个接口，无需手动敲 curl。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DoctorApiTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== 3-1 医生分页列表 ====================
    @Test
    @Order(1)
    void testDoctorList() throws Exception {
        mockMvc.perform(get("/doctors")
                        .param("pageNum", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.total").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.records[0].name").exists())
                .andExpect(jsonPath("$.data.records[0].hospitalName").exists())
                .andExpect(jsonPath("$.data.records[0].departmentName").exists());
        System.out.println("[PASS] 3-1 医生分页列表");
    }

    @Test
    @Order(2)
    void testDoctorListByDepartment() throws Exception {
        mockMvc.perform(get("/doctors")
                        .param("departmentId", "25")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(greaterThan(0)));
        System.out.println("[PASS] 3-1 按科室筛选");
    }

    // ==================== 3-2 医生搜索 ====================
    @Test
    @Order(3)
    void testDoctorSearch() throws Exception {
        mockMvc.perform(get("/doctors/search")
                        .param("keyword", "冠心病")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(greaterThan(0)));
        System.out.println("[PASS] 3-2 医生搜索");
    }

    // ==================== 3-3 医生详情 ====================
    @Test
    @Order(4)
    void testDoctorDetail() throws Exception {
        mockMvc.perform(get("/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.doctor.id").value(1))
                .andExpect(jsonPath("$.data.doctor.name").exists())
                .andExpect(jsonPath("$.data.doctor.hospitalName").exists())
                .andExpect(jsonPath("$.data.doctor.departmentName").exists())
                .andExpect(jsonPath("$.data.doctor.intro").exists())
                .andExpect(jsonPath("$.data.doctor.expertise").exists())
                .andExpect(jsonPath("$.data.doctor.rating").exists())
                .andExpect(jsonPath("$.data.doctor.onlineStatus").exists());
        System.out.println("[PASS] 3-3 医生详情");
    }

    // ==================== 3-4 科室下医生 ====================
    @Test
    @Order(5)
    void testDoctorsByDepartment() throws Exception {
        mockMvc.perform(get("/department/25/doctors")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(greaterThan(0)));
        System.out.println("[PASS] 3-4 科室下医生列表");
    }

    // ==================== 3-5 医院下医生 ====================
    @Test
    @Order(6)
    void testDoctorsByHospital() throws Exception {
        mockMvc.perform(get("/hospital/1/doctors")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(greaterThan(0)));
        System.out.println("[PASS] 3-5 医院下医生列表");
    }

    // ==================== 3-6 医生评价 ====================
    @Test
    @Order(7)
    void testDoctorReviews() throws Exception {
        mockMvc.perform(get("/doctors/1/reviews")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].userName").exists())
                .andExpect(jsonPath("$.data.records[0].rating").exists())
                .andExpect(jsonPath("$.data.records[0].content").exists())
                .andExpect(jsonPath("$.data.records[0].createTime").exists());
        System.out.println("[PASS] 3-6 医生评价列表");
    }

    // ==================== 3-7 热门医生（Redis 缓存） ====================
    @Test
    @Order(8)
    void testHotDoctors() throws Exception {
        mockMvc.perform(get("/doctors/hot")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].onlineStatus").value(1));
        System.out.println("[PASS] 3-7 热门医生（含Redis缓存）");
    }

    // ==================== 3-8 医生排班 ====================
    @Test
    @Order(9)
    void testDoctorSchedules() throws Exception {
        mockMvc.perform(get("/doctors/1/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("[PASS] 3-8 医生排班");
    }
}
