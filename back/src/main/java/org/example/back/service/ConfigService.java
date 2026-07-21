package org.example.back.service;

import org.example.back.entity.Config;

import java.util.List;

public interface ConfigService {
    
    /**
     * 获取所有配置
     */
    List<Config> getAllConfig();
    
    /**
     * 根据key获取配置值
     */
    String getConfigByKey(String key);
}
