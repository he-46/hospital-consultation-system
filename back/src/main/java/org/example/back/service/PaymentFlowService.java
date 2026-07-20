package org.example.back.service;

import java.util.Map;

public interface PaymentFlowService {

    Map<String, Object> pay(Long appointmentId, Integer payMethod, Long userId);

    /**
     * 根据订单号创建支付流水（支付宝支付前调用），自动识别挂号/咨询类型
     */
    void createByOrderNo(String orderNo);

    Map<String, Object> callback(String orderNo);

    Map<String, Object> callback(String orderNo, String thirdPartyTradeNo);

    Map<String, Object> query(String orderNo);
}
