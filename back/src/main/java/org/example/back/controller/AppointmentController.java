package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.dto.AppointmentCreateRequest;
import org.example.back.dto.PaymentRequest;
import org.example.back.service.AppointmentService;
import org.example.back.service.PaymentFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentFlowService paymentFlowService;

    @PostMapping
    public Result<?> create(@RequestBody AppointmentCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> data = appointmentService.create(request, userId);
        return Result.success(data);
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "0") Integer status,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> data = appointmentService.list(userId, status, page, size);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> data = appointmentService.detail(id, userId);
        return Result.success(data);
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.cancel(id, userId);
        return Result.success("取消成功");
    }

    @PostMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id,
                         @RequestBody PaymentRequest request,
                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> data = paymentFlowService.pay(id, request.getPayMethod(), userId);
        return Result.success(data);
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.confirm(id, userId);
        return Result.success("确认完成");
    }
}
