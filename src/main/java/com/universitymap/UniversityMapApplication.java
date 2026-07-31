package com.universitymap;

import com.universitymap.util.AppPaths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.File;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UniversityMapApplication {

    public static void main(String[] args) {
        File config = AppPaths.findConfigFile();
        if (config != null) {
            // 以系统属性（优先级高于 application.properties）指定 config.yml，等同命令行参数；
            // 写在 application.properties 里的相对路径在部分环境无法解析到工作目录。
            System.setProperty("spring.config.additional-location",
                    "optional:file:" + config.getAbsolutePath().replace('\\', '/'));
        }
        SpringApplication.run(UniversityMapApplication.class, args);
    }
}
