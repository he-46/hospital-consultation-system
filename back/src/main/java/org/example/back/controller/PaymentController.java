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

    /**
     * 模拟支付回调（第三方支付平台回调）
     * 注意：此接口为模拟实现，实际生产环境应：
     * 1. 验证回调签名（支付宝/微信提供的签名校验）
     * 2. 使用IP白名单限制调用来源
     * 3. 对orderNo做幂等处理
     * 当前已从JWT拦截器中排除，仅用于开发/演示
     */
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
