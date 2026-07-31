# 🍚 蹭饭地图

> 把班上的同学们都标记在地图上吧！以后去哪个城市就知道该蹭谁的饭了 😄

## 功能

- 🔐 **信息安全核验** — 可配置访问校验码，输入正确校验码后才能进入网站
- 🗺️ **地图展示** — 所有同学以彩色标记显示在地图上
- 🔍 **搜索** — 按姓名、城市、大学搜索
- ➕ **添加同学** — 记录姓名、大学、城市
- ✏️ **编辑/删除** — 随时更新同学信息
- 📋 **左侧列表** — 点击同学名可聚焦到地图位置
- 📥 **CSV 导入 / 📤 导出** — 从腾讯文档导出的 CSV 一键导入

## 技术栈

| 层面 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3 + Java 17+ |
| 前端 | Thymeleaf + Bootstrap 5 |
| 地图 | 高德地图 JavaScript API（v1.4） |
| 数据 | JSON 文件存储（Jackson） |
| 构建 | Maven |

## 快速开始

### 1. 配置文件 config.yml

所有站点配置都集中在项目根目录的 **`config.yml`** 中（打成 jar 部署时放在 jar 同目录即可），包含三类内容：

| 配置项 | 说明 |
|--------|------|
| `server.port` | 应用运行端口（默认 8081） |
| `app.security.verify-code` | 访问校验码。填写后开启信息安全核验，留空则关闭 |
| `app.amap.key` | 高德地图 Web 端(JS API) Key |
| `app.site.icp` | ICP 备案号（页面底部展示，可选） |
| `app.site.net-filing` | 公安网备号（可选，页面底部展示） |

```yaml
server:
  port: 8081            # 应用运行端口（默认 8081）
app:
  security:
    enabled: true          # 是否开启访问核验
    verify-code: ""        # 访问校验码，留空=关闭核验
    session-ttl: 604800    # 通过核验后的有效时长（秒），默认7天
  amap:
    key: "YOUR_AMAP_KEY"   # 高德地图 Key
  site:
    icp: ""                # ICP 备案号，如：京ICP备2024000000号
    net-filing: ""         # 公安网备号（可选）
```

> ⚠️ 修改 `config.yml` 后需**重启应用**生效。

### 2. 信息安全核验（可选）

- 在 `config.yml` 的 `app.security.verify-code` 填写一个校验码（如 `abc2026`），并保持 `enabled: true`
- 重启后，访问网站任意页面都会被重定向到 **访问核验页**，输入正确的校验码才能进入
- 通过核验后浏览器会记住凭证（默认有效 7 天），期间无需重复输入
- 想临时关闭核验：将 `enabled` 改为 `false`，或把 `verify-code` 留空

### 3. 申请高德地图 API Key

- 打开 https://lbs.amap.com/
- 注册账号 → 控制台 → 应用管理 → 创建新应用
- 添加 Key，选择「Web端(JS API)」
- 把 Key 填入 `config.yml` 的 `app.amap.key`

> 未配置 Key 时地图页会显示黄色提示条，功能页面仍可正常使用。

### 4. 启动项目

1. 用 IntelliJ IDEA 打开项目文件夹
2. IDEA 会自动下载 Maven 依赖（右下角有进度条，等它完成）
3. 运行 `UniversityMapApplication.java`（点绿色 ▶ 按钮）
4. 浏览器打开 http://localhost:8080

> **首次启动会自动创建** `data/classmates.json` 并预置 6 条示例数据。

### 5. 开始添加同学

点击右上角「添加同学」，选择城市、填写大学和留言，保存后就会显示在地图上啦！

## 项目结构

```
UniversityMap/
├── config.yml                       # ⚙️ 站点配置（校验码、高德 Key、备案号）
├── pom.xml                          # Maven 依赖配置
├── data/
│   └── classmates.json              # 📁 JSON 数据文件（自动生成）
├── src/main/java/com/universitymap/
│   ├── UniversityMapApplication.java # 启动类
│   ├── config/
│   │   ├── AppConfig.java            # ⚙️ config.yml 绑定类
│   │   ├── SecurityVerifyFilter.java # 🔐 信息安全核验过滤器
│   │   └── SiteModelAdvice.java      # 向视图注入高德 Key、备案号
│   ├── controller/
│   │   ├── MapController.java        # 页面路由 & API
│   │   ├── VerifyController.java     # 🔐 访问核验页路由
│   │   └── CityData.java             # 城市坐标数据（37个城市）
│   ├── entity/
│   │   └── Classmate.java            # 同学实体
│   └── service/
│       └── ClassmateService.java      # 业务逻辑 + JSON 文件读写
├── src/main/resources/
│   ├── application.properties         # 应用配置
│   └── templates/
│       ├── verify.html                # 🔐 访问核验页
│       ├── index.html                 # 🗺️ 地图首页（高德地图 + 搜索）
│       ├── add.html                   # ➕ 添加同学表单
│       ├── edit.html                  # ✏️ 编辑同学表单
│       └── import.html                # 📥 CSV 导入页
```

## 数据文件

所有同学数据存储在 `data/classmates.json`，格式如下：

```json
[
  {
    "id": 1,
    "name": "张三",
    "university": "清华大学",
    "city": "北京",
    "province": "北京市",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "note": "欢迎来清华找我玩！",
    "createdAt": "2025-07-21T15:30:00",
    "updatedAt": "2025-07-21T15:30:00"
  }
]
```

不需要数据库，直接复制 `data/classmates.json` 文件就能迁移数据！
