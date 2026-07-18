package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.dto.AppointmentCreateRequest;
import org.example.back.entity.Appointment;
import org.example.back.entity.Doctor;
import org.example.back.entity.Schedule;
import org.example.back.mapper.AppointmentMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.mapper.ScheduleMapper;
import org.example.back.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Override
    public Map<String, Object> create(AppointmentCreateRequest request, Long userId) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        if (schedule.getRemainCount() <= 0) {
            throw new RuntimeException("号源已约满");
        }

        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            amount = doctor.getRegistrationPrice();
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        String orderNo = generateOrderNo();

        Appointment appointment = new Appointment();
        appointment.setOrderNo(orderNo);
        appointment.setUserId(userId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setHospitalId(request.getHospitalId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setPatientName(request.getPatientName());
        appointment.setPatientPhone(request.getPatientPhone());
        appointment.setPatientIdCard(request.getPatientIdCard());
        appointment.setPatientGender(request.getPatientGender());
        appointment.setPatientAge(request.getPatientAge());
        appointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate()));
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setDiseaseDesc(request.getDiseaseDesc());
        appointment.setAmount(amount);
        appointment.setStatus(1);

        this.save(appointment);

        Map<String, Object> result = new HashMap<>();
        result.put("id", appointment.getId().toString());
        result.put("orderNo", orderNo);
        result.put("status", 1);
        result.put("amount", amount);
        return result;
    }

    @Override
    public Map<String, Object> list(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId);
        if (status != null && status > 0) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        Page<Appointment> result = this.page(page, wrapper);

        java.util.Set<Long> doctorIds = new java.util.HashSet<>();
        java.util.Set<Long> hospitalIds = new java.util.HashSet<>();
        for (Appointment a : result.getRecords()) {
            doctorIds.add(a.getDoctorId());
            hospitalIds.add(a.getHospitalId());
        }

        Map<Long, Doctor> doctorMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            doctorMapper.selectBatchIds(doctorIds).forEach(d -> doctorMap.put(d.getId(), d));
        }

        Map<Long, String> hospitalMap = new HashMap<>();
        if (!hospitalIds.isEmpty()) {
            hospitalMapper.selectBatchIds(hospitalIds).forEach(h -> hospitalMap.put(h.getId(), h.getName()));
        }

        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Appointment a : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("orderNo", a.getOrderNo());
            item.put("userId", a.getUserId());
            item.put("doctorId", a.getDoctorId());
            item.put("hospitalId", a.getHospitalId());
            item.put("scheduleId", a.getScheduleId());
            item.put("patientName", a.getPatientName());
            item.put("patientPhone", a.getPatientPhone());
            item.put("patientIdCard", a.getPatientIdCard());
            item.put("patientGender", a.getPatientGender());
            item.put("patientAge", a.getPatientAge());
            item.put("appointmentDate", a.getAppointmentDate());
            item.put("appointmentTime", a.getAppointmentTime());
            item.put("diseaseDesc", a.getDiseaseDesc());
            item.put("amount", a.getAmount());
            item.put("status", a.getStatus());
            item.put("payTime", a.getPayTime());
            item.put("createTime", a.getCreateTime());
            item.put("updateTime", a.getUpdateTime());

            Doctor d = doctorMap.get(a.getDoctorId());
            item.put("doctorName", d != null ? d.getName() : null);
            item.put("doctorTitle", d != null ? d.getTitle() : null);

            item.put("hospitalName", hospitalMap.getOrDefault(a.getHospitalId(), null));

            records.add(item);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", result.getTotal());
        map.put("size", result.getSize());
        map.put("current", result.getCurrent());
        map.put("pages", result.getPages());
        return map;
    }

    @Override
    public Map<String, Object> detail(Long id, Long userId) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("挂号订单不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("appointment", appointment);

        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        if (doctor != null) {
            result.put("doctorName", doctor.getName());
            result.put("doctorTitle", doctor.getTitle());
            result.put("doctorAvatar", doctor.getAvatar());
        }

        if (appointment.getHospitalId() != null) {
            var hospital = hospitalMapper.selectById(appointment.getHospitalId());
            if (hospital != null) {
                result.put("hospitalName", hospital.getName());
                result.put("hospitalLevel", hospital.getLevel());
            }
        }

        return result;
    }

    @Override
    public void cancel(Long id, Long userId) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("挂号订单不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        int currentStatus = appointment.getStatus();
        if (currentStatus != 1 && currentStatus != 2) {
            throw new RuntimeException("当前状态不可取消");
        }

        LambdaUpdateWrapper<Appointment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Appointment::getId, id)
               .set(Appointment::getStatus, 4)
               .set(Appointment::getUpdateTime, LocalDateTime.now());
        this.update(wrapper);

        if (currentStatus == 2) {
            Long scheduleId = appointment.getScheduleId();
            if (scheduleId != null) {
                LambdaUpdateWrapper<Schedule> scheduleUpdate = new LambdaUpdateWrapper<>();
                scheduleUpdate.eq(Schedule::getId, scheduleId)
                              .gt(Schedule::getRemainCount, 0)
                              .setSql("remain_count = remain_count + 1");
                scheduleMapper.update(null, scheduleUpdate);
            }
        }
    }

    private String generateOrderNo() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "APPT" + System.currentTimeMillis() + sb;
    }
}
