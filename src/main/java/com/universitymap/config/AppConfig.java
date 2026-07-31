package com.universitymap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 站点全局配置（对应项目根目录 {@code config.yml}）
 *
 * <p>通过 {@code spring.config.additional-location=optional:file:./config.yml}
 * 将外部 YAML 配置加载进 Spring Environment，再绑定到本类。</p>
 */
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private final Security security = new Security();
    private final Amap amap = new Amap();
    private final Site site = new Site();

    public Security getSecurity() { return security; }

    public Amap getAmap() { return amap; }

    public Site getSite() { return site; }

    /**
     * 信息安全核验是否生效（要求 {@code enabled=true} 且已填写校验码）。
     * 校验码为空时视为关闭，避免因未配置而把所有人挡在门外。
     */
    public boolean isSecurityEnabled() {
        return security.isEnabled()
                && security.getVerifyCode() != null
                && !security.getVerifyCode().isBlank();
    }

    /** 信息安全核验配置 */
    public static class Security {
        /** 是否开启访问核验 */
        private boolean enabled = true;
        /** 进入网站的校验码（留空视为关闭核验） */
        private String verifyCode = "";
        /** 通过核验后的有效时长（秒），默认 7 天 */
        private long sessionTtl = 604800;

        public boolean isEnabled() { return enabled; }

        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getVerifyCode() { return verifyCode == null ? "" : verifyCode; }

        public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }

        public long getSessionTtl() { return sessionTtl; }

        public void setSessionTtl(long sessionTtl) { this.sessionTtl = sessionTtl; }
    }

    /** 高德地图配置 */
    public static class Amap {
        /** 高德地图 Web 端(JS API) Key */
        private String key = "";

        public String getKey() { return key == null ? "" : key; }

        public void setKey(String key) { this.key = key; }
    }

    /** 备案信息配置（页面底部展示） */
    public static class Site {
        /** ICP 备案号 */
        private String icp = "";
        /** 公安网备号（可选） */
        private String netFiling = "";

        public String getIcp() { return icp == null ? "" : icp; }

        public void setIcp(String icp) { this.icp = icp; }

        public String getNetFiling() { return netFiling == null ? "" : netFiling; }

        public void setNetFiling(String netFiling) { this.netFiling = netFiling; }
    }
}
