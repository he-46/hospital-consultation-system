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
                        "/user/login",
                        "/user/register",
                        "/user/sendRegisterCode",
                        "/user/verifyPhone",
                        "/user/sendCode",
                        "/user/checkCode",
                        "/user/resetPassword",
                        // 医院浏览
                        "/hospital/**",
                        // 科室浏览
                        "/department/**",
                        "/departments/**",
                        // 医生浏览
                        "/doctors/**",
                        // 文章科普
                        "/article/**",
                        // 疾病百科
                        "/disease/**",
                        // 搜索
                        "/search/**",
                        // 首页
                        "/index/**",
                        // 系统配置
                        "/config/**",
                        // 支付宝回调（需外部回调）
                        "/alipay/**",
                        "/payment/callback",
                        "/file/**",
                        "/uploads/**"
                );
    }
}
