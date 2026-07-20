package org.example.back.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.back.common.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String secret;

    // 不需要认证的路径（公开浏览内容）
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
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
            // 医院浏览（通配）
            "/hospital",
            // 科室浏览（通配）
            "/department",
            "/departments",
            // 医生浏览（通配）
            "/doctors",
            // 文章科普（通配）
            "/article",
            // 疾病百科（通配）
            "/disease",
            // 首页
            "/index",
            // 反馈消息
            "/feedback",
            "/message",
            // 支付宝回调
            "/alipay",
            "/file",
            "/uploads"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 去掉contextPath得到实际路径
        String path = uri.substring(contextPath.length());

        // 检查是否在排除列表中
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.equals(excludePath) || path.startsWith(excludePath + "/")) {
                return true;
            }
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendUnauthorizedResponse(response, "请先登录");
            return false;
        }

        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 验证并解析token
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 将用户信息存入request
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.getSubject();
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            // 同时设置 UserContext，供 Service 层使用
            UserContext.setUserId(userId);
            // 新增打印
            System.out.println("拦截器存入userId："+userId);

            return true;
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "登录已失效，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        PrintWriter out = response.getWriter();
        out.print("{\"code\":401,\"message\":\"" + message + "\"}");
        out.flush();
    }
}
