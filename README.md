# 空调遥控器 (AC Remote Android)

Android 红外空调遥控器 APP，通过手机红外发射器控制空调。基于 IRext 开源红外编码库，支持 1000+ 空调品牌。

## 功能

### 核心遥控
- 开关控制、温度调节（16-30°C）
- 模式切换：制冷 / 制热 / 自动 / 送风 / 除湿
- 风速调节：自动 / 低 / 中 / 高
- 扫风开关

### 配对 & 管理
- 品牌选择 → 逐型号测试配对 → 保存自定义名称
- 同品牌支持多台设备
- 首页卡片网格管理，点击卡片快速进入遥控器

### 辅助功能
- 浅色/深色主题切换
- 使用统计：偏好温度、常用模式、累计运行时长
- 节能建议：根据使用习惯动态生成节电提示
- 技巧页面：10 大分类，数十条空调使用技巧

## 技术栈

| 类别 | 方案 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 红外编码 | IRext (C 解码库 + JNI) |
| 最低 SDK | Android 8.0 (API 26) |
| 构建 | Gradle KTS + AGP 8.5 |

## 项目结构

```
app/src/main/java/com/jun3120/acremote/
├── App.kt                    # Application 入口
├── MainActivity.kt            # ComponentActivity + Compose
├── ui/compose/
│   ├── AcRemoteApp.kt         # 导航中枢
│   ├── screens/               # 页面 Compose
│   │   ├── DeviceListScreen   # 首页 - 设备卡片网格
│   │   ├── SelectBrandScreen   # 品牌选择
│   │   ├── TestRemoteScreen   # 配对测试
│   │   ├── AcControlScreen    # 遥控器操作
│   │   ├── ProfileScreen      # 我的 - 统计 + 菜单
│   │   └── TipsScreen         # 技巧页
│   ├── components/            # 可复用组件
│   └── theme/                 # 主题 + 颜色
├── ui/remote/
│   └── RemoteViewModel.kt     # 红外发射核心
├── ui/brand/
│   ├── PairingViewModel.kt    # 配对逻辑
│   └── BrandViewModel.kt      # 品牌 API
├── data/
│   ├── ir/                    # 红外发射器封装
│   ├── local/                 # 数据持久化
│   └── usage/                 # 使用统计
└── decodesdk/                 # IRext JNI 解码库
```

## 版本

| 版本 | 说明 |
|------|------|
| v1.0.0 | XML UI 稳定版 |
| v2.0.0 | Compose UI 初版 |
| v3.0.0 | Compose 优化版 |
| v3.0.1 | 技巧页 + 使用统计 |
| v3.1.0 | 深色模式 + 手势返回 |

## 开发环境

- Android Studio Ladybug+
- JDK 17
- Gradle 8.x + AGP 8.5
