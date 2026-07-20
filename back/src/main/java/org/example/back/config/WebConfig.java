package org.example.back.config;

import org.example.back.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射，访问 /uploads/** 时映射到服务器上的 uploads 目录
        String resourcePath = System.getProperty("user.dir") + File.separator + uploadPath.replace("./", "") + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resourcePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 用户登录注册相关
                        "/user/login",
                        "/user/register",
                        "/user/sendRegisterCode",
                        "/user/verifyPhone",
                        "/user/sendCode",
                        "/user/checkCode",
                        "/user/resetPassword",
                        // 医院浏览相关
                        "/hospital/list",
                        "/hospital/hot",
                        "/hospital/detail",
                        "/hospital/search",
                        // 科室相关
                        "/department/primary",
                        "/department/secondary",
                        "/department/tree",
                        "/department/list",
                        // 医生浏览相关
                        "/doctor/list",
                        "/doctor/detail",
                        "/doctor/search",
                        "/doctors",
                        "/schedule/list",
                        "/schedule/detail",
                        // 文章科普相关
                        "/article/list",
                        "/article/hot",
                        "/article/detail",
                        "/article/search",
                        // 疾病百科相关
                        "/disease/list",
                        "/disease/hot",
                        "/disease/detail",
                        "/disease/search",
                        // 搜索相关
                        "/search/**",
                        // 配置相关
                        "/config/**",
                        // 支付宝回调（需外部回调）
                        "/alipay/notifyUrl",
                        "/alipay/returnUrl",
                        // 静态资源
                        "/uploads/**"
                );
    }
}
