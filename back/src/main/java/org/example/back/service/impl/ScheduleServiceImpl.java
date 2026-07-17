package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Schedule;
import org.example.back.mapper.ScheduleMapper;
import org.example.back.service.ScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Override
    public List<Schedule> getDoctorSchedules(Long doctorId) {
        return getDoctorSchedules(doctorId, 7);
    }

    @Override
    public List<Schedule> getDoctorSchedules(Long doctorId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
                .ge(Schedule::getScheduleDate, today)
                .le(Schedule::getScheduleDate, endDate)
                .eq(Schedule::getStatus, 1)
                .orderByAsc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getTimeSlot);

        return this.list(wrapper);
    }
}
