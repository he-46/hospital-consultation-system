package org.example.back.service;

import java.util.Map;

public interface PaymentFlowService {

    Map<String, Object> pay(Long appointmentId, Integer payMethod, Long userId);

    Map<String, Object> callback(String orderNo);

    Map<String, Object> query(String orderNo);
}
