package org.example.back.service;

import org.example.back.entity.Schedule;

import java.util.List;

public interface ScheduleService {

    /**
     * 获取医生未来7天排班
     */
    List<Schedule> getDoctorSchedules(Long doctorId);

    /**
     * 获取医生排班（指定天数）
     */
    List<Schedule> getDoctorSchedules(Long doctorId, int days);
}
