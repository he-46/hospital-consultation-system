package org.example.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Doctor;
import org.example.back.entity.Follow;
import org.example.back.entity.Schedule;
import org.example.back.mapper.FollowMapper;
import org.example.back.mapper.ScheduleMapper;
import org.example.back.service.DoctorService;
import org.example.back.service.ScheduleService;
import org.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserService userService;

    @Resource
    private ScheduleMapper scheduleMapper;

    /**
     * 医生分页列表 - GET /api/doctors
     * 支持按科室、医院、职称筛选
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String title) {
        Page<Doctor> page = doctorService.getDoctorPage(pageNum, pageSize, departmentId, hospitalId, title);
        return Result.success(page);
    }

    /**
     * 热门医生 - GET /api/doctors/hot
     */
    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "10") Integer limit) {
        List<Doctor> doctors = doctorService.getHotDoctors(limit);
        return Result.success(doctors);
    }

    /**
     * 搜索医生 - GET /api/doctors/search
     */
    @GetMapping("/search")
    public Result<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = doctorService.searchDoctors(keyword, departmentId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 医生详情 - GET /api/doctors/{id}
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Map<String, Object> detail = doctorService.getDoctorDetail(id);
        return Result.success(detail);
    }

    /**
     * 医生排班 - GET /api/doctors/{id}/schedules
     * 默认返回未来7天排班
     */
    @GetMapping("/{id}/schedules")
    public Result<?> schedules(@PathVariable Long id) {
        List<Schedule> schedules = scheduleService.getDoctorSchedules(id);
        return Result.success(schedules);
    }

    /**
     * 医生评价列表 - GET /api/doctors/{id}/reviews
     */
    @GetMapping("/{id}/reviews")
    public Result<?> reviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Map<String, Object>> page = doctorService.getDoctorReviews(id, pageNum, pageSize);
        return Result.success(page);
    }

    @Autowired
    private FollowMapper followMapper;

    /**
     * 查询用户是否关注该医生
     */
    @GetMapping("/follow/status/{doctorId}")
    public Result<Boolean> getFollowStatus(HttpServletRequest request, @PathVariable Long doctorId) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, doctorId)
                .eq(Follow::getFollowType, 1); // 1=医生
        long count = followMapper.selectCount(wrapper);
        return Result.success(count > 0);
    }

    /**
     * 关注医生
     */
    @PostMapping("/follow")
    public Result<?> followDoctor(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long doctorId = params.get("doctorId");
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowId(doctorId);
        follow.setFollowType(1);
        follow.setCreateTime(LocalDateTime.now());
        followMapper.insert(follow);
        return Result.success(null);
    }

    /**
     * 取消关注医生
     */
    @DeleteMapping("/unfollow")
    public Result<?> unfollowDoctor(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long doctorId = params.get("doctorId");
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, doctorId)
                .eq(Follow::getFollowType, 1);
        followMapper.delete(wrapper);
        return Result.success(null);
    }

    @GetMapping("/{doctorId}/schedule")
    public Result<List<Schedule>> getDoctorSchedule(@PathVariable Long doctorId) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId);
        List<Schedule> list = scheduleMapper.selectList(wrapper);
        return Result.success(list);
    }
}
