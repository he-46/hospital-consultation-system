package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.RedisUtil;
import org.example.back.entity.Config;
import org.example.back.mapper.ConfigMapper;
import org.example.back.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Autowired
    private RedisUtil redisUtil;

    private static final String REDIS_CONFIG_PREFIX = "config:";

    @PostConstruct
    public void init() {
        // 启动时加载配置到Redis缓存
        List<Config> configs = this.list();
        for (Config config : configs) {
            redisUtil.set(REDIS_CONFIG_PREFIX + config.getConfigKey(), config.getConfigValue(), 24, TimeUnit.HOURS);
        }
    }

    @Override
    public List<Config> getAllConfig() {
        return this.list();
    }

    @Override
    public String getConfigByKey(String key) {
        Object value = redisUtil.get(REDIS_CONFIG_PREFIX + key);
        if (value != null) {
            return value.toString();
        }
        
        // 缓存未命中，从数据库获取
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, key);
        Config config = this.getOne(wrapper);
        if (config != null) {
            redisUtil.set(REDIS_CONFIG_PREFIX + key, config.getConfigValue(), 24, TimeUnit.HOURS);
            return config.getConfigValue();
        }
        return null;
    }
}
