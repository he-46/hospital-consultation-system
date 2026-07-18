package org.example.back.config;

import org.example.back.common.AliyunSmsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 阿里云短信配置
 */
@Configuration
public class AliyunSmsConfig {

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    private AliyunSmsUtil aliyunSmsUtil;

    @PostConstruct
    public void init() {
        aliyunSmsUtil = new AliyunSmsUtil();
        aliyunSmsUtil.init(accessKeyId, accessKeySecret);
    }

    public AliyunSmsUtil getAliyunSmsUtil() {
        return aliyunSmsUtil;
    }

    public String getSignName() {
        return signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }
}
