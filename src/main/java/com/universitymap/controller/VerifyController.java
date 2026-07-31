package com.universitymap.controller;

import com.universitymap.config.AppConfig;
import com.universitymap.config.SecurityVerifyFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 信息安全核验 —— 核验页路由
 */
@Controller
public class VerifyController {

    private final AppConfig appConfig;

    public VerifyController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * 核验页（GET）
     * 未开启核验时直接进入首页，避免出现死路。
     */
    @GetMapping("/verify")
    public String verifyPage(@RequestParam(required = false) String next, Model model) {
        if (!appConfig.isSecurityEnabled()) {
            return "redirect:/";
        }
        model.addAttribute("next", next);
        return "verify";
    }

    /**
     * 校验码提交（POST）
     * 校验码正确 → 颁发 HttpOnly 校验 Cookie 并跳回原目标页；否则返回错误提示。
     */
    @PostMapping("/verify")
    public String doVerify(@RequestParam("code") String code,
                           @RequestParam(required = false) String next,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {
        if (appConfig.getSecurity().getVerifyCode().equals(code)) {
            Cookie cookie = new Cookie(SecurityVerifyFilter.VERIFY_COOKIE,
                    SecurityVerifyFilter.hashOf(appConfig.getSecurity().getVerifyCode()));
            cookie.setHttpOnly(true);
            cookie.setPath(cookiePath(request));
            cookie.setMaxAge((int) appConfig.getSecurity().getSessionTtl());
            response.addCookie(cookie);

            // 只允许站内相对路径，防止开放重定向
            String target = (next != null && next.startsWith("/") && !next.startsWith("//")) ? next : "/";
            return "redirect:" + target;
        }

        model.addAttribute("error", "校验码错误，请重新输入");
        model.addAttribute("next", next);
        return "verify";
    }

    private String cookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.isEmpty() ? "/" : contextPath;
    }
}
