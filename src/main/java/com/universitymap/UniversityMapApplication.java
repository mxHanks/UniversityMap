package com.universitymap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.File;
import java.net.URI;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UniversityMapApplication {

    public static void main(String[] args) {
        String configPath = locateConfigFile();
        if (configPath != null) {
            // 以系统属性（优先级高于 application.properties）指定 config.yml，等同命令行参数；
            // 写在 application.properties 里的相对路径在部分环境无法解析到工作目录。
            System.setProperty("spring.config.additional-location", "optional:file:" + configPath);
        }
        SpringApplication.run(UniversityMapApplication.class, args);
    }

    /**
     * 按顺序查找 config.yml：
     * <ol>
     *   <li>jar 包同目录（打 jar 部署时 config.yml 与 jar 放一起即可，与启动目录无关）</li>
     *   <li>classes 目录逐级向上（IntelliJ 运行时 classes 在 target/classes，向上找到项目根目录）</li>
     *   <li>当前工作目录（兜底）</li>
     * </ol>
     *
     * @return 找到的绝对路径；都找不到返回 {@code null}
     */
    private static String locateConfigFile() {
        try {
            // 获取当前类所在位置（统一解析成真实文件/目录路径）：
            //   - IntelliJ 运行时：file:/.../target/classes/
            //   - Spring Boot 3.x fat jar：jar:nested:/.../app.jar/!BOOT-INF/classes/!/
            //     （外层 jar 与嵌套路径之间用 /! 分隔，且 URL 以 /!/ 结尾）
            //   - 其它形式 jar：jar:file:/.../app.jar!/BOOT-INF/classes/
            String spec = UniversityMapApplication.class.getProtectionDomain()
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
            File codeSource = new File(new URI(spec));
            File base = codeSource.isFile() ? codeSource.getParentFile() : codeSource;
            // 1. jar 同目录 / classes 同目录
            File jarDir = new File(base, "config.yml");
            if (jarDir.isFile()) {
                return jarDir.getAbsolutePath().replace('\\', '/');
            }
            // 2. classes 在 target/classes 时，向上逐级找项目根目录
            if (codeSource.isDirectory()) {
                File parent = base.getParentFile();
                int depth = 0;
                while (parent != null && depth < 4) {
                    File candidate = new File(parent, "config.yml");
                    if (candidate.isFile()) {
                        return candidate.getAbsolutePath().replace('\\', '/');
                    }
                    parent = parent.getParentFile();
                    depth++;
                }
            }
        } catch (Exception ignored) {
            // 获取 jar 位置失败时忽略，走工作目录兜底
        }
        // 3. 当前工作目录兜底
        File cwd = new File(System.getProperty("user.dir"), "config.yml");
        if (cwd.isFile()) {
            return cwd.getAbsolutePath().replace('\\', '/');
        }
        return null;
    }
}
