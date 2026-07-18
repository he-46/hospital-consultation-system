package org.example.back.common;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;

/**
 * 阿里云短信验证码发送工具类
 */
public class AliyunSmsUtil {

    private Client client;

    /**
     * 初始化阿里云短信客户端
     */
    public void init(String accessKeyId, String accessKeySecret) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dypnsapi.aliyuncs.com");
            this.client = new Client(config);
        } catch (Exception e) {
            throw new RuntimeException("初始化阿里云短信客户端失败", e);
        }
    }

    /**
     * 发送短信验证码
     */
    public boolean sendVerifyCode(String phoneNumber, String signName, String templateCode, String verifyCode) {
        if (client == null) {
            throw new RuntimeException("阿里云短信客户端未初始化");
        }

        try {
            String templateParam = "{\"code\":\"" + verifyCode + "\",\"min\":\"5\"}";
            
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);
            
            if ("OK".equals(response.getBody().getCode())) {
                return true;
            } else {
                throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("短信发送异常: " + e.getMessage(), e);
        }
    }
}
