package com.universitymap.controller;

import java.util.*;

/**
 * 中国城市坐标数据 —— 按省份分组，供前端省-市二级联动
 */
public class CityData {

    /** 省份 → 城市列表（含坐标） */
    private static final List<Map<String, Object>> PROVINCES = new ArrayList<>();

    static {
        // ===== 每个 addProvince 调用添加一个省份及其城市 =====
        addProvince("北京市", List.of(
                city("北京", 39.9042, 116.4074)
        ));
        addProvince("天津市", List.of(
                city("天津", 39.3434, 117.3616)
        ));
        addProvince("上海市", List.of(
                city("上海", 31.2304, 121.4737)
        ));
        addProvince("重庆市", List.of(
                city("重庆", 29.4316, 106.9123)
        ));

        addProvince("河北省", List.of(
                city("石家庄", 38.0428, 114.5149),
                city("唐山", 39.6309, 118.1802),
                city("秦皇岛", 39.9353, 119.5996),
                city("邯郸", 36.6256, 114.5391),
                city("保定", 38.8739, 115.4646),
                city("廊坊", 39.5379, 116.6838)
        ));
        addProvince("山西省", List.of(
                city("太原", 37.8706, 112.5489),
                city("大同", 40.0768, 113.3000),
                city("临汾", 36.0882, 111.5189),
                city("运城", 35.0265, 111.0069)
        ));
        addProvince("内蒙古自治区", List.of(
                city("呼和浩特", 40.8422, 111.7498),
                city("包头", 40.6574, 109.8403),
                city("赤峰", 42.2577, 118.8869)
        ));
        addProvince("辽宁省", List.of(
                city("沈阳", 41.8057, 123.4315),
                city("大连", 38.9140, 121.6147),
                city("鞍山", 41.1078, 122.9944),
                city("锦州", 41.0952, 121.1270)
        ));
        addProvince("吉林省", List.of(
                city("长春", 43.8171, 125.3235),
                city("吉林", 43.8379, 126.5492),
                city("延边", 42.8913, 129.5087)
        ));
        addProvince("黑龙江省", List.of(
                city("哈尔滨", 45.8038, 126.5350),
                city("大庆", 46.5913, 125.1030),
                city("齐齐哈尔", 47.3543, 123.9180)
        ));
        addProvince("江苏省", List.of(
                city("南京", 32.0603, 118.7969),
                city("苏州", 31.2990, 120.5853),
                city("无锡", 31.4906, 120.3111),
                city("常州", 31.8108, 119.9743),
                city("徐州", 34.2044, 117.2841),
                city("南通", 31.9798, 120.8937),
                city("扬州", 32.3936, 119.4147),
                city("镇江", 32.1896, 119.4250)
        ));
        addProvince("浙江省", List.of(
                city("杭州", 30.2741, 120.1551),
                city("宁波", 29.8683, 121.5440),
                city("温州", 28.0013, 120.6994),
                city("绍兴", 29.9986, 120.5800),
                city("嘉兴", 30.7710, 120.7550),
                city("金华", 29.0892, 119.6476),
                city("台州", 28.6562, 121.4207)
        ));
        addProvince("安徽省", List.of(
                city("合肥", 31.8206, 117.2272),
                city("芜湖", 31.3525, 118.4330),
                city("蚌埠", 32.9166, 117.3892),
                city("马鞍山", 31.6704, 118.5061)
        ));
        addProvince("福建省", List.of(
                city("福州", 26.0745, 119.2965),
                city("厦门", 24.4798, 118.0894),
                city("泉州", 24.8744, 118.6760),
                city("漳州", 24.5130, 117.6474)
        ));
        addProvince("江西省", List.of(
                city("南昌", 28.6829, 115.8582),
                city("九江", 29.7051, 115.9989),
                city("赣州", 25.8311, 114.9340),
                city("景德镇", 29.2689, 117.1784)
        ));
        addProvince("山东省", List.of(
                city("济南", 36.6512, 117.1201),
                city("青岛", 36.0671, 120.3826),
                city("烟台", 37.4635, 121.4479),
                city("潍坊", 36.7168, 119.1618),
                city("临沂", 35.0527, 118.3565),
                city("淄博", 36.8131, 118.0549),
                city("威海", 37.5135, 122.1216)
        ));
        addProvince("河南省", List.of(
                city("郑州", 34.7466, 113.6253),
                city("洛阳", 34.6181, 112.4540),
                city("开封", 34.7973, 114.3072),
                city("南阳", 33.0002, 112.5287),
                city("新乡", 35.3037, 113.9267)
        ));
        addProvince("湖北省", List.of(
                city("武汉", 30.5928, 114.3055),
                city("宜昌", 30.6919, 111.2865),
                city("襄阳", 32.0090, 112.1224),
                city("荆州", 30.3376, 112.2397)
        ));
        addProvince("湖南省", List.of(
                city("长沙", 28.2282, 112.9388),
                city("株洲", 27.8274, 113.1340),
                city("湘潭", 27.8351, 112.9440),
                city("衡阳", 26.8886, 112.5720),
                city("岳阳", 29.3570, 113.1290)
        ));
        addProvince("广东省", List.of(
                city("广州", 23.1291, 113.2644),
                city("深圳", 22.5431, 114.0579),
                city("珠海", 22.2710, 113.5669),
                city("汕头", 23.3542, 116.6822),
                city("佛山", 23.0352, 113.1219),
                city("东莞", 23.0208, 113.7518),
                city("中山", 22.5176, 113.3827),
                city("惠州", 23.1107, 114.4162)
        ));
        addProvince("广西壮族自治区", List.of(
                city("南宁", 22.8170, 108.3665),
                city("桂林", 25.2736, 110.2900),
                city("柳州", 24.3263, 109.4153),
                city("北海", 21.4689, 109.1196)
        ));
        addProvince("海南省", List.of(
                city("海口", 20.0440, 110.3495),
                city("三亚", 18.2528, 109.5119)
        ));
        addProvince("四川省", List.of(
                city("成都", 30.5728, 104.0668),
                city("绵阳", 31.4674, 104.6796),
                city("德阳", 31.1271, 104.3979),
                city("宜宾", 28.7697, 104.6430),
                city("泸州", 28.8717, 105.4426),
                city("南充", 30.7980, 106.0820)
        ));
        addProvince("贵州省", List.of(
                city("贵阳", 26.6470, 106.6302),
                city("遵义", 27.7066, 106.9372),
                city("六盘水", 26.5927, 104.8306)
        ));
        addProvince("云南省", List.of(
                city("昆明", 25.0389, 102.7183),
                city("大理", 25.6065, 100.2280),
                city("丽江", 26.8721, 100.2299),
                city("曲靖", 25.4899, 103.7960)
        ));
        addProvince("西藏自治区", List.of(
                city("拉萨", 29.6500, 91.1000),
                city("日喀则", 29.2675, 88.8805)
        ));
        addProvince("陕西省", List.of(
                city("西安", 34.3416, 108.9398),
                city("咸阳", 34.3294, 108.7092),
                city("宝鸡", 34.3621, 107.2370),
                city("延安", 36.5852, 109.4897)
        ));
        addProvince("甘肃省", List.of(
                city("兰州", 36.0611, 103.8343),
                city("天水", 34.5809, 105.7248),
                city("酒泉", 39.7446, 98.4949)
        ));
        addProvince("青海省", List.of(
                city("西宁", 36.6232, 101.7800),
                city("海东", 36.5000, 102.1000)
        ));
        addProvince("宁夏回族自治区", List.of(
                city("银川", 38.4864, 106.2325),
                city("石嘴山", 38.9833, 106.3833)
        ));
        addProvince("新疆维吾尔自治区", List.of(
                city("乌鲁木齐", 43.8256, 87.6168),
                city("克拉玛依", 45.5953, 84.8813),
                city("伊犁", 43.9170, 81.3247)
        ));
        addProvince("香港特别行政区", List.of(
                city("香港", 22.3193, 114.1694)
        ));
        addProvince("澳门特别行政区", List.of(
                city("澳门", 22.1987, 113.5492)
        ));
        addProvince("台湾省", List.of(
                city("台北", 25.0330, 121.5654),
                city("高雄", 22.6273, 120.3014),
                city("台中", 24.1477, 120.6736)
        ));
    }

    /** 添加一个省及其城市列表 */
    private static void addProvince(String provinceName, List<Map<String, Object>> cities) {
        Map<String, Object> province = new LinkedHashMap<>();
        province.put("province", provinceName);
        province.put("cities", cities);
        PROVINCES.add(province);
    }

    /** 创建一个城市条目 */
    private static Map<String, Object> city(String name, double lat, double lng) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("name", name);
        c.put("latitude", lat);
        c.put("longitude", lng);
        return c;
    }

    /** 获取按省份分组的城市列表 */
    public static List<Map<String, Object>> getProvinces() {
        return PROVINCES;
    }

    /** 获取所有城市的扁平列表（兼容旧版前端） */
    public static List<Map<String, Object>> getCities() {
        List<Map<String, Object>> all = new ArrayList<>();
        for (Map<String, Object> province : PROVINCES) {
            String provinceName = (String) province.get("province");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cities = (List<Map<String, Object>>) province.get("cities");
            for (Map<String, Object> city : cities) {
                Map<String, Object> entry = new LinkedHashMap<>(city);
                entry.put("province", provinceName);
                all.add(entry);
            }
        }
        return all;
    }

    /** 根据城市名查找坐标和省份 */
    public static Map<String, Object> findByName(String name) {
        if (name == null) return null;
        for (Map<String, Object> province : PROVINCES) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cities = (List<Map<String, Object>>) province.get("cities");
            for (Map<String, Object> city : cities) {
                if (name.equals(city.get("name"))) {
                    Map<String, Object> result = new LinkedHashMap<>(city);
                    result.put("province", province.get("province"));
                    return result;
                }
            }
        }
        return null;
    }
}
