package com.universitymap.util;

import java.io.File;
import java.net.URI;

/**
 * 应用安装位置定位工具。
 *
 * <p>约定：config.yml 与 data 数据目录都以「jar 包同目录」为基准
 * （IntelliJ 运行时 = 项目根目录），与进程启动目录无关。</p>
 */
public final class AppPaths {

    private AppPaths() {
    }

    /** 定位 config.yml；找不到返回 {@code null} */
    public static File findConfigFile() {
        File codeSource = codeSourceBase();
        if (codeSource == null) {
            return null;
        }
        // jar 文件取其父目录，classes 目录取自身
        File base = codeSource.isFile() ? codeSource.getParentFile() : codeSource;
        // 1. jar 同目录 / classes 同目录
        File jarDir = new File(base, "config.yml");
        if (jarDir.isFile()) {
            return jarDir;
        }
        // 2. classes 在 target/classes 时，向上逐级找项目根目录
        if (codeSource.isDirectory()) {
            File parent = base.getParentFile();
            int depth = 0;
            while (parent != null && depth < 4) {
                File candidate = new File(parent, "config.yml");
                if (candidate.isFile()) {
                    return candidate;
                }
                parent = parent.getParentFile();
                depth++;
            }
        }
        return null;
    }

    /**
     * 应用主目录：config.yml 所在目录；找不到则 jar 同目录（或 classes 所在目录）；再不行用工作目录。
     */
    public static File findAppHome() {
        File config = findConfigFile();
        if (config != null) {
            return config.getParentFile();
        }
        File codeSource = codeSourceBase();
        if (codeSource != null) {
            return codeSource.isFile() ? codeSource.getParentFile() : codeSource;
        }
        return new File(System.getProperty("user.dir"));
    }

    /**
     * 当前类（jar/classes）所在位置，统一解析成真实文件或目录路径：
     * <ul>
     *   <li>IntelliJ 运行时：file:/.../target/classes/（目录）</li>
     *   <li>Spring Boot 3.x fat jar：jar:nested:/.../app.jar/!BOOT-INF/classes/!/
     *       （外层 jar 与嵌套路径之间用 /! 分隔，且 URL 以 /!/ 结尾）</li>
     *   <li>其它形式 jar：jar:file:/.../app.jar!/BOOT-INF/classes/</li>
     * </ul>
     */
    private static File codeSourceBase() {
        try {
            String spec = AppPaths.class.getProtectionDomain()
                    .getCodeSource().getLocation().toString();
            if (spec.startsWith("jar:nested:")) {
                String inner = spec.substring("jar:nested:".length());
                int sep = inner.indexOf("/!");
                if (sep >= 0) {
                    inner = inner.substring(0, sep);
                }
                spec = "file:" + inner;
            } else if (spec.startsWith("jar:")) {
                spec = spec.substring("jar:".length());
                int sep = spec.indexOf("!/");
                if (sep >= 0) {
                    spec = spec.substring(0, sep);
                }
            }
            return new File(new URI(spec));
        } catch (Exception ignored) {
            return null;
        }
    }
}
