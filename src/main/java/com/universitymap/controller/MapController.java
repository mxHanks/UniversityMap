package com.universitymap.controller;

import com.universitymap.entity.Classmate;
import com.universitymap.service.ClassmateService;
import com.universitymap.controller.CityData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 蹭饭地图 —— 页面路由 & API
 */
@Controller
public class MapController {

    private final ClassmateService classmateService;

    public MapController(ClassmateService classmateService) {
        this.classmateService = classmateService;
    }

    // ==================== 页面路由 ====================

    /**
     * 首页：蹭饭地图
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("total", classmateService.findAll().size());
        return "index";
    }

    /**
     * 添加同学页面
     */
    @GetMapping("/classmates/add")
    public String addPage(Model model) {
        model.addAttribute("classmate", new Classmate());
        return "add";
    }

    /**
     * 编辑同学页面
     */
    @GetMapping("/classmates/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Classmate classmate = classmateService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("同学不存在: " + id));
        model.addAttribute("classmate", classmate);
        return "edit";
    }

    // ==================== API ====================

    /**
     * 获取所有同学（JSON，供地图 JS 调用）
     */
    @GetMapping("/api/classmates")
    @ResponseBody
    public List<Classmate> getClassmates(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return classmateService.search(keyword);
        }
        return classmateService.findAll();
    }

    /**
     * 添加同学
     */
    @PostMapping("/classmates")
    public String add(Classmate classmate) {
        classmateService.add(classmate);
        return "redirect:/";
    }

    /**
     * 更新同学信息
     */
    @PostMapping("/classmates/update/{id}")
    public String update(@PathVariable Long id, Classmate updated) {
        updated.setId(id);
        classmateService.update(updated);
        return "redirect:/";
    }

    /**
     * 删除同学
     */
    @GetMapping("/classmates/delete/{id}")
    public String delete(@PathVariable Long id) {
        classmateService.deleteById(id);
        return "redirect:/";
    }

    /**
     * 获取城市列表（含坐标），用于添加/编辑时的城市下拉
     */
    @GetMapping("/api/cities")
    @ResponseBody
    public List<Map<String, Object>> getCities() {
        return CityData.getCities();
    }

    /**
     * 获取按省份分组的城市列表，用于省-市二级联动
     */
    @GetMapping("/api/provinces")
    @ResponseBody
    public List<Map<String, Object>> getProvinces() {
        return CityData.getProvinces();
    }

    // ==================== CSV 导出 ====================

    /**
     * 导出所有同学数据为 CSV（UTF-8 BOM，兼容 Excel）
     */
    @GetMapping("/classmates/export")
    public ResponseEntity<byte[]> exportCsv() {
        List<Classmate> list = classmateService.findAll();
        StringBuilder sb = new StringBuilder();

        // 表头
        sb.append("姓名,大学,大学所在地,省份,备注\r\n");

        for (Classmate c : list) {
            sb.append(escapeCsv(c.getName())).append(',');
            sb.append(escapeCsv(c.getUniversity())).append(',');
            sb.append(escapeCsv(c.getCity())).append(',');
            sb.append(escapeCsv(c.getProvince())).append(',');
            sb.append(escapeCsv(c.getNote())).append("\r\n");
        }

        // UTF-8 with BOM (0xEF 0xBB 0xBF)
        byte[] utf8Bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(bom);
            bos.write(utf8Bytes);
        } catch (Exception e) {
            // fallback: 不加 BOM
        }
        byte[] result = bos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"classmates.csv\"");
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /** CSV 字段转义（含逗号或引号时用双引号包裹） */
    private String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== CSV 导入（已有） ====================

    /**
     * CSV 导入页面
     */
    @GetMapping("/classmates/import")
    public String importPage() {
        return "import";
    }

    /**
     * CSV 导入接口
     */
    @PostMapping("/api/classmates/import")
    @ResponseBody
    public Map<String, Object> importCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Classmate> imported = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // 自动检测编码：
            // 1) UTF-8 BOM (EF BB BF) → UTF-8
            // 2) 先试 GBK（Excel 导出常见），若表头不含中文字段则回退 UTF-8
            byte[] rawBytes = file.getBytes();
            String csvText;
            if (rawBytes.length >= 3
                    && (rawBytes[0] & 0xFF) == 0xEF
                    && (rawBytes[1] & 0xFF) == 0xBB
                    && (rawBytes[2] & 0xFF) == 0xBF) {
                // 有 UTF-8 BOM，跳过 BOM 用 UTF-8 解码
                csvText = new String(rawBytes, 3, rawBytes.length - 3, StandardCharsets.UTF_8);
            } else {
                // 无 BOM：先试 GBK
                csvText = new String(rawBytes, "GBK");
                // 快速验证：GBK 解码后的前 200 个字符是否包含可识别的表格头
                String probe = csvText.substring(0, Math.min(csvText.length(), 200));
                if (!probe.contains("姓名") && !probe.contains("学号") && !probe.contains("name")) {
                    // GBK 解码没找到表头，换 UTF-8 试试
                    String utf8Text = new String(rawBytes, StandardCharsets.UTF_8);
                    String utf8Probe = utf8Text.substring(0, Math.min(utf8Text.length(), 200));
                    if (utf8Probe.contains("姓名") || utf8Probe.contains("学号") || utf8Probe.contains("name")) {
                        csvText = utf8Text;
                    }
                }
            }

            BufferedReader reader = new BufferedReader(
                    new StringReader(csvText));

            // 跳过开头的标题行（如 "2307录取登记表"）
            String headerLine = null;
            String line;
            while ((line = reader.readLine()) != null) {
                headerLine = line;
                if (headerLine.isBlank()) continue;
                // 跳过标题行：如果第一行不是含"姓名"或"学号"的表头，继续往下读
                String lowerHeader = headerLine.toLowerCase();
                if (lowerHeader.contains("姓名") || lowerHeader.contains("学号") || lowerHeader.contains("name")) {
                    break;
                }
            }
            if (headerLine == null || headerLine.isBlank()) {
                result.put("success", false);
                result.put("message", "CSV 文件为空或格式不正确");
                return result;
            }

            // 解析表头列名
            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toLowerCase(), i);
            }

            // 找到关键列的索引
            Integer nameIdx = colIndex.get("姓名");
            if (nameIdx == null) nameIdx = colIndex.get("名字");
            if (nameIdx == null) nameIdx = colIndex.get("name");
            Integer univIdx = colIndex.get("大学");
            if (univIdx == null) univIdx = colIndex.get("录取大学");
            if (univIdx == null) univIdx = colIndex.get("学校");
            if (univIdx == null) univIdx = colIndex.get("录取学校");
            Integer cityIdx = colIndex.get("大学所在地");
            if (cityIdx == null) cityIdx = colIndex.get("城市");
            if (cityIdx == null) cityIdx = colIndex.get("所在城市");
            if (cityIdx == null) cityIdx = colIndex.get("city");
            Integer provinceIdx = colIndex.get("省份");
            if (provinceIdx == null) provinceIdx = colIndex.get("省");
            if (provinceIdx == null) provinceIdx = colIndex.get("province");
            Integer noteIdx = colIndex.get("备注");
            if (noteIdx == null) noteIdx = colIndex.get("留言");
            if (noteIdx == null) noteIdx = colIndex.get("note");

            if (nameIdx == null) {
                result.put("success", false);
                result.put("message", "未找到「姓名」列，请确保 CSV 包含「姓名」列");
                return result;
            }

            // 城市名清理：去掉省份前缀（如 "广东广州" → "广州"）
            // 获取所有省份名称列表
            List<Map<String, Object>> provinces = CityData.getProvinces();
            List<String> provinceNames = new ArrayList<>();
            for (Map<String, Object> p : provinces) {
                provinceNames.add(((String) p.get("province")).replace("省", "").replace("市", "")
                        .replace("自治区", "").replace("特别行政区", "").replace("壮族", "")
                        .replace("回族", "").replace("维吾尔", ""));
            }

            // 逐行解析
            int lineNum = 1;
            int successCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;
                String[] fields = parseCsvLine(line);

                String name = getField(fields, nameIdx, "");
                if (name.isEmpty()) {
                    errors.add("第 " + lineNum + " 行：姓名为空，已跳过");
                    continue;
                }

                Classmate cm = new Classmate();
                cm.setName(name);
                cm.setUniversity(getField(fields, univIdx, ""));

                // 处理城市名：去掉省份前缀
                String rawCity = getField(fields, cityIdx, "");
                String cityName = cleanCityName(rawCity, provinceNames);
                String provinceName = getField(fields, provinceIdx, "");

                if (cityName.isEmpty()) {
                    errors.add("第 " + lineNum + " 行（" + name + "）：城市为空，已跳过");
                    continue;
                }

                cm.setCity(cityName);
                Map<String, Object> cityInfo = CityData.findByName(cityName);
                if (cityInfo == null) {
                    errors.add("第 " + lineNum + " 行（" + name + "）：未找到城市「" + cityName + "」的坐标，已跳过");
                    continue;
                }

                if (provinceName.isEmpty()) {
                    cm.setProvince((String) cityInfo.get("province"));
                } else {
                    cm.setProvince(provinceName);
                }
                cm.setLatitude((Double) cityInfo.get("latitude"));
                cm.setLongitude((Double) cityInfo.get("longitude"));
                cm.setNote(getField(fields, noteIdx, ""));
                imported.add(cm);
                successCount++;
            }

            reader.close();

            // 批量导入
            if (!imported.isEmpty()) {
                classmateService.replaceAll(imported);
            }

            result.put("success", true);
            result.put("count", successCount);
            result.put("errors", errors);
            result.put("message", "成功导入 " + successCount + " 条数据"
                    + (errors.isEmpty() ? "" : "，" + errors.size() + " 条警告"));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }
        return result;
    }

    /** 简单 CSV 行解析（支持引号包裹的字段） */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }

    /** 安全获取字段值 */
    private String getField(String[] fields, Integer idx, String defaultValue) {
        if (idx == null || idx < 0 || idx >= fields.length) return defaultValue;
        return fields[idx];
    }

    /** 清理城市名：去掉省份前缀（如 "广东广州" → "广州"） */
    private String cleanCityName(String raw, List<String> provinceNames) {
        if (raw == null || raw.isBlank()) return "";
        String trimmed = raw.trim();
        // 优先精确匹配城市数据库
        Map<String, Object> cityInfo = CityData.findByName(trimmed);
        if (cityInfo != null) return trimmed;
        // 尝试去掉省份前缀后匹配
        for (String p : provinceNames) {
            if (trimmed.startsWith(p) && trimmed.length() > p.length()) {
                String stripped = trimmed.substring(p.length()).trim();
                Map<String, Object> ci = CityData.findByName(stripped);
                if (ci != null) return stripped;
            }
        }
        // 如果去掉前缀后只剩2个字（城市名通常2字），直接返回
        for (String p : provinceNames) {
            if (trimmed.startsWith(p) && trimmed.length() == p.length() + 2) {
                return trimmed.substring(p.length());
            }
        }
        return trimmed;
    }
}
