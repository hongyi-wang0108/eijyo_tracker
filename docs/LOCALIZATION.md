# 本地化方案（#6 全 App 本地化）

> 规模：592 条中文字面量 / 36 文件（2026-06-15 统计）。分批推进，**中文进默认 `values/` 做兜底** → 翻译没跟上的自动回退中文，每批之间 App 永远可用、不崩、不空白。语言入口等做够批次再打开。

## 核心原则

1. **中文 = 默认 `res/values/strings.xml`**（fallback）。`values-ja/`、`values-en/` 为翻译；缺失项自动回退中文。
2. **每批闭环**：抽字符串 → 加 ja/en → 代码替换 → 编译+装机切语言验证该页 → 单独 commit。
3. **架构改动集中最后一批**，前面只动 UI 层，风险隔离。

## 架构决定（非 Composable 字符串）

`stringResource` 只能在 Composable 用。非 UI 层处理：
- **Enums（61 条，`Enums.kt`）**：`label: String` → `@StringRes val labelRes: Int`；UI 层 `stringResource(enum.labelRes)` 取。
- **ViewModel / domain（TimelineBuilder / DocumentTemplateGenerator / RiskEngine 等）**：注入 `@ApplicationContext Context` 用 `context.getString(...)`，或把拼装挪到 UI 层。
- 这些都放 **批 6** 做。

## 分批清单

| 批 | 范围 | 约条数 | 状态 |
|---|------|-------|------|
| 0 | 基建：strings.xml 三语结构 + 语言切换端到端打通 | — | ✅ 2026-06-15 |
| 1 | 设置页 + 通用词（确定/取消/按钮） | ~38 | ✅ 2026-06-15（zh/ja/en 实机验证切换） |
| 2 | 首页 Dashboard | ~33 | ✅ 2026-06-15（zh/ja/en，HomeScreen.kt 全替换） |
| 3 | Onboarding 问答 | ~52 | ✅ 2026-06-15（zh/ja/en，OnboardingScreen + CompleteScreen） |
| 4 | 数据页 + 预测页 + 风险页 | ~52 | ✅ 2026-06-15（zh/ja/en，DataScreen+PredictionDetailScreen+RiskDetailScreen） |
| 5 | 材料页 + 申请页 + 各 Sheet | ~48 | ✅ 2026-06-15（zh/ja/en，MaterialsScreen+ApplicationScreen+AddEventSheet+WelcomeScreen） |
| 6 | Enums + domain（架构改动集中于此） | ~158 | ✅ 2026-06-15（zh/ja/en，Enums.kt/Routes.kt/MaterialsViewModel/AddEventSheet/MainScaffold/MaterialsScreen/OnboardingScreen/OnboardingCompleteScreen/HomeVM/ApplicationVM/DataVM/OnboardingCompleteVM/PredictionDetailVM/RiskDetailVM/SettingsVM/TimelineBuilder/PredictionEngine） |

> 验证结论（批1）：语言入口已打开（`enabled=true`）；切 zh/ja/en 经 `recreate()` 即时生效；
> 未抽取的字符串（导航/Enums/其他页）按设计回退原中文，App 不空白不崩。
>
> 批2 注意：`HomeViewModel.greeting()` / `statusSummary()` / `placeholderFor()` / `backlogLabel` 里的中文（约10条）
> 留批6处理（需注入 Context 或上移到 UI 层）。`officeName()` 的 `contains("入管")` 检查在批6 ViewModel 改造后需同步更新。

> 批5 注意：`EventType` enum 的 `label`（6 条）留批6；`DocumentFilter.label` / `DocumentCategory.label`
> 也留批6。`PlaceholderScreen` 无活跃调用方，暂跳过。
>
> 状态：⏳ 待做 / 🔧 进行中 / ✅ 完成。每批完成后回填本表 + commit。

## 每批固定流程

1. 抽该区域中文 → `values/strings.xml`（key 用 `区域_含义`，如 `settings_privacy_title`）
2. `values-ja/strings.xml` + `values-en/strings.xml` 补该批译文
3. 代码替换为 `stringResource(R.string.xxx)`（Composable）/ 资源引用（非 UI）
4. 编译 + 装机切语言验证该页
5. commit（每批独立，可随时停）

## 语言入口

基础设施已就绪（`LanguagePrefs` + `MainActivity.attachBaseContext` + `findActivity().recreate()`）。
当前「我的」页语言入口 `enabled = false`（灰）。做够批次后改 `true` 打开。

## 翻译质量

法律/入管术语必须准确：永住=永住権/Permanent Residence、在留資格=Status of Residence、
標準処理期間=Standard Processing Period 等。机翻后上架前建议人工校对一遍。
