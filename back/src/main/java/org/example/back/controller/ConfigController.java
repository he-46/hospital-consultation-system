package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.entity.Config;
import org.example.back.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public Result<?> getAllConfig() {
        List<Config> configs = configService.getAllConfig();
        
        Map<String, String> configMap = new HashMap<>();
        for (Config config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return Result.success(configMap);
    }

    /**
     * 根据key获取配置
     */
    @GetMapping("/getByKey")
    public Result<?> getConfigByKey(String key) {
        if (key == null || key.isEmpty()) {
            return Result.error("配置key不能为空");
        }
        String value = configService.getConfigByKey(key);
        return Result.success(value);
    }
}
