package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Appointment;
import org.example.back.entity.Consult;
import org.example.back.entity.PaymentFlow;
import org.example.back.entity.Schedule;
import org.example.back.mapper.AppointmentMapper;
import org.example.back.mapper.ConsultMapper;
import org.example.back.mapper.PaymentFlowMapper;
import org.example.back.mapper.ScheduleMapper;
import org.example.back.service.PaymentFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentFlowServiceImpl extends ServiceImpl<PaymentFlowMapper, PaymentFlow> implements PaymentFlowService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ConsultMapper consultMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public Map<String, Object> pay(Long appointmentId, Integer payMethod, Long userId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("挂号订单不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (appointment.getStatus() != 1) {
            throw new RuntimeException("订单状态异常，无法支付");
        }

        LambdaQueryWrapper<PaymentFlow> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(PaymentFlow::getBusinessOrderNo, appointment.getOrderNo())
                    .eq(PaymentFlow::getPayStatus, 1);
        if (this.count(existWrapper) > 0) {
            throw new RuntimeException("该订单已支付");
        }

        PaymentFlow flow = new PaymentFlow();
        flow.setBusinessOrderNo(appointment.getOrderNo());
        flow.setBusinessType(1);
        flow.setPayMethod(payMethod != null ? payMethod : 1);
        flow.setActualAmount(appointment.getAmount());
        flow.setPayStatus(0);
        this.save(flow);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", appointment.getOrderNo());
        result.put("amount", appointment.getAmount());
        result.put("payMethod", payMethod);
        result.put("message", "请完成支付");
        return result;
    }

    @Override
    public Map<String, Object> callback(String orderNo) {
        LambdaQueryWrapper<PaymentFlow> flowWrapper = new LambdaQueryWrapper<>();
        flowWrapper.eq(PaymentFlow::getBusinessOrderNo, orderNo);
        PaymentFlow flow = this.getOne(flowWrapper);
        if (flow == null) {
            throw new RuntimeException("支付流水不存在");
        }

        if (flow.getPayStatus() == 1) {
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("message", "已支付，无需重复回调");
            return result;
        }

        String thirdPartyTradeNo = "SIM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // 更新支付流水
        LambdaUpdateWrapper<PaymentFlow> flowUpdate = new LambdaUpdateWrapper<>();
        flowUpdate.eq(PaymentFlow::getId, flow.getId())
                  .set(PaymentFlow::getPayStatus, 1)
                  .set(PaymentFlow::getThirdPartyTradeNo, thirdPartyTradeNo)
                  .set(PaymentFlow::getPaySuccessTime, now)
                  .set(PaymentFlow::getUpdateTime, now);
        this.update(flowUpdate);

        // 处理咨询订单
        if (flow.getBusinessType() != null && flow.getBusinessType() == 2) {
            LambdaQueryWrapper<Consult> consultWrapper = new LambdaQueryWrapper<>();
            consultWrapper.eq(Consult::getOrderNo, orderNo);
            Consult consult = consultMapper.selectOne(consultWrapper);
            if (consult == null) {
                throw new RuntimeException("咨询订单不存在");
            }
            LambdaUpdateWrapper<Consult> consultUpdate = new LambdaUpdateWrapper<>();
            consultUpdate.eq(Consult::getId, consult.getId())
                        .set(Consult::getStatus, 2)
                        .set(Consult::getPayTime, now)
                        .set(Consult::getUpdateTime, now);
            consultMapper.update(null, consultUpdate);

            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("thirdPartyTradeNo", thirdPartyTradeNo);
            result.put("paySuccessTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("status", 2);
            return result;
        }

        // 处理挂号订单
        LambdaQueryWrapper<Appointment> apptWrapper = new LambdaQueryWrapper<>();
        apptWrapper.eq(Appointment::getOrderNo, orderNo);
        Appointment appointment = appointmentMapper.selectOne(apptWrapper);
        if (appointment == null) {
            throw new RuntimeException("挂号订单不存在");
        }

        Long scheduleId = appointment.getScheduleId();
        if (scheduleId == null) {
            throw new RuntimeException("排班信息缺失");
        }

        LambdaUpdateWrapper<Schedule> scheduleUpdate = new LambdaUpdateWrapper<>();
        scheduleUpdate.eq(Schedule::getId, scheduleId)
                      .gt(Schedule::getRemainCount, 0)
                      .setSql("remain_count = remain_count - 1");
        int rows = scheduleMapper.update(null, scheduleUpdate);
        if (rows == 0) {
            throw new RuntimeException("号源已约满，支付失败");
        }

        LambdaUpdateWrapper<Appointment> apptUpdate = new LambdaUpdateWrapper<>();
        apptUpdate.eq(Appointment::getId, appointment.getId())
                  .set(Appointment::getStatus, 2)
                  .set(Appointment::getPayTime, now)
                  .set(Appointment::getUpdateTime, now);
        appointmentMapper.update(null, apptUpdate);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("thirdPartyTradeNo", thirdPartyTradeNo);
        result.put("paySuccessTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("status", 2);
        return result;
    }

    @Override
    public Map<String, Object> query(String orderNo) {
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentFlow::getBusinessOrderNo, orderNo);
        PaymentFlow flow = this.getOne(wrapper);
        if (flow == null) {
            throw new RuntimeException("支付流水不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("businessOrderNo", flow.getBusinessOrderNo());
        result.put("businessType", flow.getBusinessType());
        result.put("payMethod", flow.getPayMethod());
        result.put("actualAmount", flow.getActualAmount());
        result.put("payStatus", flow.getPayStatus());
        result.put("thirdPartyTradeNo", flow.getThirdPartyTradeNo());
        result.put("paySuccessTime", flow.getPaySuccessTime());
        return result;
    }
}
