package org.example.back.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    // 不需要认证的路径（公开接口）
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            // 用户相关
            "/user/login",
            "/user/register",
            "/user/verifyPhone",
            "/user/sendCode",
            "/user/checkCode",
            "/user/resetPassword",
            "/user/logout",
            // 配置相关
            "/config/list",
            "/config/getByKey",
            // 公开数据接口
            "/hospital/list",
            "/hospital/hot",
            "/hospital/detail",
            "/hospital/search",
            "/department/primary",
            "/department/secondary",
            "/department/tree",
            "/doctor",
            "/doctors",
            "/department",
            "/hospital",
            "/article/list",
            "/article/hot",
            "/article/detail",
            "/article/search",
            "/disease/list",
            "/disease/hot",
            "/disease/detail",
            "/disease/search",
            // 文件上传
            "/file/uploadAvatar",
            // 静态资源
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
            
            return true;
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "登录已失效，请重新登录");
            return false;
        }
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
