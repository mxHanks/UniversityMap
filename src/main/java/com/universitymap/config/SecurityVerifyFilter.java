package com.universitymap.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 信息安全核验过滤器
 *
 * <p>未通过核验（没有有效校验 Cookie）的请求，一律重定向到
 * {@code /verify} 核验页；校验通过后由 {@code VerifyController} 颁发 Cookie 放行。</p>
 *
 * <p>开关逻辑：{@link AppConfig#isSecurityEnabled()} 同时满足
 * {@code app.security.enabled=true} 且 {@code app.security.verify-code} 非空时生效。</p>
 */
@Component
public class SecurityVerifyFilter extends OncePerRequestFilter {

    /** 通过核验后颁发的凭证 Cookie 名 */
    public static final String VERIFY_COOKIE = "app_verify_token";

    private final AppConfig appConfig;

    public SecurityVerifyFilter(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /** 计算校验码的 SHA-256 摘要（十六进制），作为核验凭证，防止伪造简单值 */
    public static String hashOf(String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 未开启核验（或校验码为空）→ 直接放行
        if (!appConfig.isSecurityEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 核验页本身、常见静态资源与错误页直接放行
        if (isAllowedPath(path, contextPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 已有有效校验 Cookie → 放行
        if (hasValidCookie(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 未通过核验 → 重定向到核验页，并带上原目标地址
        String next = path;
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            next += "?" + request.getQueryString();
        }
        response.sendRedirect(contextPath + "/verify?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
    }

    private boolean isAllowedPath(String path, String contextPath) {
        if (path.equals(contextPath + "/verify") || path.startsWith(contextPath + "/verify/")) {
            return true;
        }
        return path.equals(contextPath + "/error")
                || path.equals(contextPath + "/favicon.ico")
                || path.startsWith(contextPath + "/css/")
                || path.startsWith(contextPath + "/js/")
                || path.startsWith(contextPath + "/images/");
    }

    private boolean hasValidCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        String expected = hashOf(appConfig.getSecurity().getVerifyCode());
        for (Cookie cookie : cookies) {
            if (VERIFY_COOKIE.equals(cookie.getName())
                    && expected.equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }
}
