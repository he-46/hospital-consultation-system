package org.example.back.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.back.common.Result;
import org.example.back.config.AlipayConfig;
import org.example.back.dto.AlipayDto;
import org.example.back.entity.Appointment;
import org.example.back.entity.Consult;
import org.example.back.mapper.AppointmentMapper;
import org.example.back.mapper.ConsultMapper;
import org.example.back.service.PaymentFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private PaymentFlowService paymentFlowService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ConsultMapper consultMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 支付宝支付接口
     */
    @PostMapping("/pay")
    public Result<?> pay(@RequestBody AlipayDto alipayDto) {
        String out_trade_no = alipayDto.getWidout_trade_no();
        String total_amount = alipayDto.getWidtotal_amount().toString();
        String subject = alipayDto.getWidsubject();
        String body = alipayDto.getWidbody();

        // 创建支付流水（自动识别挂号/咨询）
        try {
            paymentFlowService.createByOrderNo(out_trade_no);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    AlipayConfig.gatewayUrl, AlipayConfig.app_id,
                    AlipayConfig.merchant_private_key, "json",
                    AlipayConfig.charset, AlipayConfig.alipay_public_key,
                    AlipayConfig.sign_type);

            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(AlipayConfig.return_url);
            alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

            Map<String, String> bizContent = new LinkedHashMap<>();
            bizContent.put("out_trade_no", out_trade_no);
            bizContent.put("total_amount", total_amount);
            bizContent.put("subject", subject);
            bizContent.put("body", body);
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            alipayRequest.setBizContent(objectMapper.writeValueAsString(bizContent));

            String result = alipayClient.pageExecute(alipayRequest).getBody();
            return Result.success(result);
        } catch (AlipayApiException | IOException e) {
            return Result.error("支付请求失败");
        }
    }

    /**
     * 支付宝异步通知回调
     */
    @GetMapping("/notifyUrl")
    public void notifyUrl(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        Map<String, String> params = parseRequestParams(request);

        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                AlipayConfig.alipay_public_key, AlipayConfig.charset,
                AlipayConfig.sign_type);

        if (signVerified) {
            String out_trade_no = getAlipayParam(request, "out_trade_no");
            String trade_no = getAlipayParam(request, "trade_no");
            String trade_status = getAlipayParam(request, "trade_status");

            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                try {
                    paymentFlowService.callback(out_trade_no, trade_no);
                } catch (Exception e) {
                    // 业务异常不影响给支付宝返回 success
                }
            }

            response.getWriter().println("success");
        } else {
            response.getWriter().println("fail");
        }
    }

    /**
     * 支付宝同步跳转页面
     */
    @GetMapping("/returnUrl")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        Map<String, String> params = parseRequestParams(request);

        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                AlipayConfig.alipay_public_key, AlipayConfig.charset,
                AlipayConfig.sign_type);

        if (signVerified) {
            String out_trade_no = getAlipayParam(request, "out_trade_no");
            String trade_no = getAlipayParam(request, "trade_no");

            String tradeNo = "";
            try {
                Map<String, Object> cbResult = paymentFlowService.callback(out_trade_no, trade_no);
                if (cbResult != null && cbResult.get("thirdPartyTradeNo") != null) {
                    tradeNo = cbResult.get("thirdPartyTradeNo").toString();
                }
            } catch (Exception e) {
                // 可能已回调过，忽略
            }

            // 根据订单号查找对应的挂号/咨询订单，跳转到对应成功页
            String redirectUrl = "http://localhost:3000/#/personal"; // 兜底

            String query = "?orderNo=" + URLEncoder.encode(out_trade_no, "UTF-8")
                    + "&tradeNo=" + URLEncoder.encode(tradeNo, "UTF-8");

            Appointment appointment = appointmentMapper.selectOne(
                    new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, out_trade_no));
            if (appointment != null) {
                redirectUrl = "http://localhost:3000/#/reservation-success/" + appointment.getId() + query;
            } else {
                Consult consult = consultMapper.selectOne(
                        new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, out_trade_no));
                if (consult != null) {
                    redirectUrl = "http://localhost:3000/#/consult-success/" + consult.getId() + query;
                }
            }

            response.sendRedirect(redirectUrl);
        } else {
            response.getWriter().println("验签失败");
        }
    }

    private String getAlipayParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return "";
        }
        try {
            return new String(value.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return value;
        }
    }

    private Map<String, String> parseRequestParams(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = String.join(",", values);
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
            params.put(name, valueStr);
        }
        return params;
    }
}
