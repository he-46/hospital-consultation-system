package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.service.PaymentFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentFlowService paymentFlowService;

    @PostMapping("/callback")
    public Result<?> callback(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        Map<String, Object> data = paymentFlowService.callback(orderNo);
        return Result.success(data);
    }

    @GetMapping("/{orderNo}")
    public Result<?> query(@PathVariable String orderNo) {
        Map<String, Object> data = paymentFlowService.query(orderNo);
        return Result.success(data);
    }
}
