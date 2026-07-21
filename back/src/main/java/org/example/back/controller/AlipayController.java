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
                    System.out.println("notifyUrl回调成功: orderNo=" + out_trade_no + ", tradeNo=" + trade_no);
                } catch (Exception e) {
                    System.err.println("notifyUrl回调失败: orderNo=" + out_trade_no + ", tradeNo=" + trade_no);
                    e.printStackTrace();
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
                System.out.println("支付宝回调成功: orderNo=" + out_trade_no + ", tradeNo=" + tradeNo);
            } catch (Exception e) {
                System.err.println("支付宝回调失败: orderNo=" + out_trade_no + ", trade_no=" + trade_no);
                e.printStackTrace();
            }

            // 根据订单号查找对应的挂号/咨询订单，获取跳转信息
            String orderType = "unknown";
            String orderId = "";
            Appointment appointment = appointmentMapper.selectOne(
                    new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, out_trade_no));
            if (appointment != null) {
                orderType = "appointment";
                orderId = String.valueOf(appointment.getId());
            } else {
                Consult consult = consultMapper.selectOne(
                        new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, out_trade_no));
                if (consult != null) {
                    orderType = "consult";
                    orderId = String.valueOf(consult.getId());
                }
            }

            // 返回HTML页面，通知原页面后自动关闭弹窗
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("<!DOCTYPE html><html><head><meta charset='utf-8'><title>支付成功</title>" +
                "<style>body{font-family:'Microsoft YaHei',sans-serif;text-align:center;padding-top:80px;background:#f5f7fa;}" +
                ".success{color:#4caf50;font-size:48px;}.info{margin-top:20px;color:#666;font-size:14px;}</style></head><body>" +
                "<div class='success'>&#10003;</div><h2>支付成功</h2>" +
                "<div class='info'>订单号：" + out_trade_no + "<br/>交易号：" + tradeNo + "</div>" +
                "<div class='info' style='margin-top:10px;color:#999;'>窗口即将关闭，请返回原页面查看...</div>" +
                "<script>" +
                "if(window.opener){" +
                "  window.opener.postMessage({" +
                "    type:'PAY_SUCCESS'," +
                "    orderType:'" + orderType + "'," +
                "    orderId:'" + orderId + "'," +
                "    orderNo:'" + out_trade_no + "'," +
                "    tradeNo:'" + tradeNo + "'" +
                "  }, '*');" +
                "} setTimeout(function(){window.close();},2000);" +
                "</script></body></html>");
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
