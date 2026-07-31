package com.universitymap.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 向所有视图注入站点配置（高德地图 Key、备案号等）
 *
 * <p>所有模板可直接引用 {@code ${amapKey}}、{@code ${amapUrl}}、
 * {@code ${icpNumber}}、{@code ${netFilingNumber}}。</p>
 */
@ControllerAdvice
public class SiteModelAdvice {

    private final AppConfig appConfig;

    public SiteModelAdvice(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /** 高德地图 Key（原始值，用于页面判断是否已配置） */
    @ModelAttribute("amapKey")
    public String amapKey() {
        return appConfig.getAmap().getKey();
    }

    /** 高德地图 JS API 脚本完整地址（未配置 Key 时返回空串，模板据此不加载脚本） */
    @ModelAttribute("amapUrl")
    public String amapUrl() {
        String key = appConfig.getAmap().getKey();
        if (key == null || key.isBlank()) {
            return "";
        }
        return "https://webapi.amap.com/maps?v=1.4.15&key=" + key.trim();
    }

    /** ICP 备案号 */
    @ModelAttribute("icpNumber")
    public String icpNumber() {
        return appConfig.getSite().getIcp();
    }

    /** 公安网备号（可选） */
    @ModelAttribute("netFilingNumber")
    public String netFilingNumber() {
        return appConfig.getSite().getNetFiling();
    }
}
