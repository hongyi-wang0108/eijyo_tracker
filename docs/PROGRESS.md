# 开发进度 — 永住 Tracker

> 单一事实来源。每次有意义的改动后更新这里。新会话先读本文件即可接上进度。
> 规格见根目录 `PRODUCT_PM.md`；口头敲定但 PMdoc 未覆盖的决定记在 `docs/DECISIONS.md`。
> 预测算法 + 公开数据管线的完整技术方案见 `docs/PREDICTION_AND_DATA.md`。

**最后更新**：2026-06-14（Step 1 完成：数据结构 + 转换脚本 + 真实 public-data.json 入 assets + 单测）

---

## 技术栈

Kotlin · Jetpack Compose · Material3 · Hilt · Room · DataStore · Navigation Compose
设计稿：`/Users/yuki/Documents/untitled.pen`（用 Pencil MCP 读 Node ID）
Debug 包名：`com.eijyo.tracker.debug` · 远端：https://github.com/hongyi-wang0108/eijyo_tracker

---

## 里程碑总览

| 区域 | PMdoc | 状态 | 备注 |
|------|-------|------|------|
| Welcome 首启页 | §5 | ✅ 完成 | 按设计稿重写 |
| Onboarding 问答 | §6.1-6.2 | ✅ 完成 | 12 问 + 滚轮日期选择器 |
| Onboarding 完成页 | §6.3 | ✅ 完成 | generated 态；跳过 bug 已修 |
| 首页 Dashboard | §7 | ✅ 完成 | 审查中态按设计稿；准备中/已结束态未做 |
| 预测详情页 | §9 | ✅ UI 完成 | **MOCK_PREVIEW=true，待接真实数据** |
| 风险自检页 | §10 | ✅ UI 完成 | **MOCK_PREVIEW=true，待接真实数据** |
| 申请 Tab | §8 | ✅ UI 完成 | **MOCK_PREVIEW=true**；状态详情子页/时间线节点详情推迟 |
| 材料 Tab / 材料清单 | §9(材料) | ✅ 完成 | 真实数据；内联状态更新；四档筛选 |
| 数据页 | §11 | ✅ 完成 | 真实数据；趋势图；地区/路径/来源卡；详情页待做 |
| 我的 / 设置 | — | ✅ 完成 | 真实数据；语言入口暂灰；免责/关于 Sheet 可用；隐私 Sheet UI 占位 |

---

## 进行中

- **预测详情页**：UI 完成
  - [x] 三参考区间卡片等高（`IntrinsicSize.Max`，以最高的「正常」为准）— 2026-06-11
  - [ ] 关掉 `MOCK_PREVIEW`，接 ViewModel 真实数据
- **风险自检页**：UI 完成 — 2026-06-11
  - [ ] 关掉 `MOCK_PREVIEW`，接 ViewModel 真实数据

## 上架前 TODO（1-7，2026-06-15 起逐项推进）

> 大方案（预测+数据管线）已全部完成、MOCK 全清。以下是上架前剩余清单，分三档。

🔴 上架必须（阻塞）
- [x] **1. 数据仓库上线** — 2026-06-15：scripts/raw/public-data.json + README(SOP) 推送 `eijyo_tracker_data@main`；raw & jsDelivr 均 200，16 断言绿
- [x] **2. 隐私与数据 Sheet 真逻辑** — 2026-06-15：导出走系统保存文件选择器写 JSON 备份；删除有确认弹窗，清 Room(clearAllTables)+偏好(clearAll)后 recreate 回首启
- [x] **3. release 签名** — 2026-06-15：signingConfigs 读 keystore.properties(gitignored)，`assembleRelease` 出已签名 APK（apksigner 验证通过）；详见 `docs/RELEASE_SIGNING.md`

🟡 体验完整性
- [ ] **4. 首页「准备中」「已结束」两态**（现仅审查中态）
- [ ] **5. 其他局校准系数**：仅东京=1.65 有 ground truth，其余暂 1.0（无实测，保持 1.0 是正确做法，待实测再调）

🟢 锦上添花
- [ ] **6. 全 App 本地化**：抽 strings.xml + 日英翻译 → 打开语言入口（大工程）
- [ ] **7. 上架准备**：Inter 字体、暗色模式

---

## 下一步（按优先级）

1. **预测算法 + 公开数据管线**（大方案，方案已定稿见 `docs/PREDICTION_AND_DATA.md`）：
   - [x] 数据结构（`PublicDataDoc` / `OfficeData` / `MonthlyPoint` 等 Kotlin 模型） — 2026-06-14
   - [x] Python 转换脚本（`estat_to_json.py`）+ 16 个转换断言全绿 — 2026-06-14（脚本在 data repo `scripts/`）
   - [x] 真实 `public-data.json` 入 `app/src/main/assets/`（65 个月·6 个地区·gold-standard 2026-03 验证） — 2026-06-14
   - [x] `PublicDataDocTest.kt`（Android JUnit，解析 + gold-standard 断言） — 2026-06-14
   - [x] Step2 网络层 + 三层降级（jsDelivr → DataStore 缓存 → APK assets 兜底）+ 降级链6测试 — 2026-06-14
   - [x] Step3 数据页接真实数据（积压卡 pending/月处理/排队 + permitsByYear 趋势图 + 双口径） — 2026-06-14
   - [x] Step4 时间线共享重构：抽 `TimelineBuilder` 到 `domain/timeline`，申请页用 `build()` 完整、
     首页用 `summary()` 只读摘要（接真实提交/补资料/预测/结果）；关 ApplicationScreen MOCK_PREVIEW — 2026-06-14
   - [x] Step5 `PredictionEngine` FIFO 模型 + Case A-G 单测全绿（38 tests, 0 failures） — 2026-06-14
     - 算法在 `FifoPrediction.computeWait()`（纯函数）；`predict(profile, PublicDataDoc, today)` 重载格式化为 Prediction
     - 无地区数据/OTHER/空序列 → 退回旧 4-6 月逻辑（LOW 置信度）
   - [x] 接线：`AnalysisRepository.regenerate/refreshPrediction` 改用 FIFO 重载（接 PublicDataRepository）；
     启动时 `RootViewModel` 重算预测（避免冻结在上次编辑）；关闭 PredictionDetailScreen MOCK_PREVIEW — 2026-06-14
     - Home/PredictionDetail 读 Room 存好的 Prediction，自动变 FIFO 结果，无需改 ViewModel
2. 关掉三处 `MOCK_PREVIEW`，接真实数据（见下方开关清单）
3. 首页「准备中」「已结束」两态（现仅审查中态）
4. 隐私与数据 Sheet 接真实逻辑（导出 / 删除档案，现仅 UI 占位）
5. **全 App 本地化**（抽 strings.xml + 日英翻译）→ 做完去掉语言入口 `enabled = false`。基础设施已就绪（`LanguagePrefs`/`attachBaseContext`/`findActivity`）
6. 上架准备：Inter 字体、暗色模式、release 签名

> 注：原「真实数据贯通：时间线日期 + 公开数据卡数值」已并入第 1 项大方案。

---

## MOCK / 占位开关清单

> 每个还在用假数据或占位的地方都登记在这，避免上架前漏掉。

| 位置 | 开关 | 当前值 | 含义 |
|------|------|--------|------|
| `feature/prediction/PredictionDetailScreen.kt` | `MOCK_PREVIEW` | `false` ✅ | 已接 FIFO 真实预测 |
| `feature/risk/RiskDetailScreen.kt` | `MOCK_PREVIEW` | `false` ✅ | 已接 RiskEngine 真实评估 |
| `feature/application/ApplicationScreen.kt` | `MOCK_PREVIEW` | `false` ✅ | 时间线已接 TimelineBuilder 真实数据 |
| `feature/home/HomeScreen.kt` | `MOCK_PREVIEW` | `false` ✅ | 真实数据；保留 `.copy()` 补缺字段逻辑 |
| `feature/settings/SettingsScreen.kt` | — | 真实数据 ✅ | 已无 MOCK，读 ProfileRepository 真名/状态 |
| 我的页 语言设置入口 | `enabled` | `false` ⚠️ | 暂灰；本地化做完后改 `true` |
| 首页 申请时间线卡 | — | 真实数据 ✅ | 接 TimelineBuilder.summary（提交/受理/预测/结果） |
| 首页 公开数据卡 | — | 真实数据 ✅ | 接 PublicDataRepository（更新月 + 真实 mini 趋势） |
| 材料 Tab | — | 真实数据 ✅ | 已实现，内联更新 + 筛选 |
| 数据 Tab | — | 真实数据 ✅ | 接 e-Stat 真实积压/处理/许可数（三层降级）；§11.5 数据详情页已删除 |

---

## 已知问题 / 待办细节

- [ ] 首页预测卡目前永远可点；按 §7 应仅「审查中」态可点进详情
- [x] 首页数据卡「准备中」态标题 fallback → 显示「入管处理数据」— 2026-06-11
- [x] 三参考区间卡等高 — 2026-06-11

---

## 关键技术备忘（踩过的坑）

- **NavHost startDestination 必须冻结**：`val start = rememberSaveable { resolved }`。onboarding 完成标志翻 true 会让活动的 NavHost 重定向到 MAIN，跳过完成页。
- **WheelDatePicker**：用 `displayedValues`（不是 `setFormatter`）给年/月/日加单位；`setFormatter` 不作用于居中选中项。日数组保持 31 项满长，靠 `maxValue` 收窄可见范围。
- **DogFace 耳朵 z-order**：先画脸 oval 再画耳朵，否则脸盖住耳朵。
- adb 重置数据：`adb shell pm clear com.eijyo.tracker.debug`
- 截图太大读不了：`sips -Z 1400` 降到 1400px。
