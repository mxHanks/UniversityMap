package com.universitymap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universitymap.entity.Classmate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 同学业务逻辑层 —— 基于 JSON 文件存储
 */
@Service
public class ClassmateService {

    private final File dataFile;
    private final ObjectMapper mapper;
    private final List<Classmate> classmates = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ClassmateService(@Value("${app.data.dir:data}") String dataDir) {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
        // dataDir 可能是相对路径或绝对路径
        File dir = new File(dataDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), dataDir);
        }
        this.dataFile = new File(dir, "classmates.json");
        System.out.println("🗄️ 数据文件路径: " + this.dataFile.getAbsolutePath());
    }

    /** 启动时加载数据 */
    @PostConstruct
    public void init() {
        loadFromFile();
    }

    // ==================== 查询方法 ====================

    /** 获取所有同学 */
    public List<Classmate> findAll() {
        return new ArrayList<>(classmates);
    }

    /** 根据 ID 查找 */
    public Optional<Classmate> findById(Long id) {
        return classmates.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    /** 搜索（姓名/城市/大学） */
    public List<Classmate> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String kw = keyword.trim().toLowerCase();
        return classmates.stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(kw))
                        || (c.getCity() != null && c.getCity().toLowerCase().contains(kw))
                        || (c.getUniversity() != null && c.getUniversity().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    /** 判断是否存在 */
    public boolean existsById(Long id) {
        return classmates.stream().anyMatch(c -> c.getId().equals(id));
    }

    // ==================== 写操作方法 ====================

    /** 添加同学 */
    public synchronized Classmate add(Classmate classmate) {
        classmate.setId(idCounter.getAndIncrement());
        classmate.setCreatedAt(LocalDateTime.now());
        classmate.setUpdatedAt(LocalDateTime.now());
        classmates.add(classmate);
        saveToFile();
        return classmate;
    }

    /** 更新同学 */
    public synchronized Classmate update(Classmate updated) {
        for (int i = 0; i < classmates.size(); i++) {
            if (classmates.get(i).getId().equals(updated.getId())) {
                updated.setCreatedAt(classmates.get(i).getCreatedAt());
                updated.setUpdatedAt(LocalDateTime.now());
                classmates.set(i, updated);
                saveToFile();
                return updated;
            }
        }
        throw new NoSuchElementException("同学不存在: " + updated.getId());
    }

    /** 删除同学 */
    public synchronized void deleteById(Long id) {
        classmates.removeIf(c -> c.getId().equals(id));
        saveToFile();
    }

    /** 批量替换所有数据（CSV 导入用） */
    public synchronized void replaceAll(List<Classmate> newList) {
        classmates.clear();
        long id = 1;
        for (Classmate cm : newList) {
            cm.setId(id++);
            classmates.add(cm);
        }
        idCounter.set(id);
        saveToFile();
    }

    // ==================== 文件读写 ====================

    private void loadFromFile() {
        if (!dataFile.exists()) {
            // 首次使用：创建空文件，不生成示例数据
            try {
                dataFile.getParentFile().mkdirs();
                saveToFile();
                System.out.println("✅ 已创建空数据文件: " + dataFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("⚠️ 创建数据文件失败: " + e.getMessage());
            }
            return;
        }

        try {
            List<Classmate> loaded = mapper.readValue(dataFile, new TypeReference<List<Classmate>>() {});
            if (loaded != null && !loaded.isEmpty()) {
                classmates.clear();
                classmates.addAll(loaded);
                long maxId = loaded.stream()
                        .filter(c -> c.getId() != null)
                        .mapToLong(Classmate::getId)
                        .max()
                        .orElse(0);
                idCounter.set(maxId + 1);
                System.out.println("✅ 已加载 " + loaded.size() + " 条同学数据");
            }
        } catch (IOException e) {
            System.err.println("⚠️ 读取数据文件失败，使用空数据: " + e.getMessage());
        }
    }

    private synchronized void saveToFile() {
        try {
            dataFile.getParentFile().mkdirs();
            mapper.writeValue(dataFile, classmates);
        } catch (IOException e) {
            System.err.println("❌ 保存数据失败: " + e.getMessage());
        }
    }
}
