package org.example.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectPath = System.getProperty("user.dir");
        String resourcePath = projectPath + File.separator + uploadPath.replace("./", "");
        
        // 配置静态资源映射，访问 /uploads/** 路径时映射到 uploads 目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resourcePath + File.separator);
    }
}
