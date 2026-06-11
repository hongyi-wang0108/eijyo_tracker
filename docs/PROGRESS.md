# 开发进度 — 永住 Tracker

> 单一事实来源。每次有意义的改动后更新这里。新会话先读本文件即可接上进度。
> 规格见根目录 `PRODUCT_PM.md`；口头敲定但 PMdoc 未覆盖的决定记在 `docs/DECISIONS.md`。

**最后更新**：2026-06-11

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
| 申请 Tab | §8 | ⬜ Placeholder | |
| 材料 Tab / 材料清单 | §9(材料) | ⬜ Placeholder | |
| 数据页 | §11 | ⬜ Placeholder | |
| 我的 / 设置 | — | ⬜ Placeholder | |

---

## 进行中

- **预测详情页**：UI 完成
  - [x] 三参考区间卡片等高（`IntrinsicSize.Max`，以最高的「正常」为准）— 2026-06-11
  - [ ] 关掉 `MOCK_PREVIEW`，接 ViewModel 真实数据
- **风险自检页**：UI 完成 — 2026-06-11
  - [ ] 关掉 `MOCK_PREVIEW`，接 ViewModel 真实数据

## 下一步（按优先级）

1. 首页「准备中」「已结束」两态（现仅审查中态）
2. 申请 Tab（§8，时间线 + 添加事件 Bottom Sheet）
3. 材料 Tab（§9 材料清单）
4. 数据页（§11）
5. 真实数据贯通：时间线日期(CaseRecord)、公开数据卡数值(PublicData)
7. 上架准备：Inter 字体、暗色模式、release 签名

---

## MOCK / 占位开关清单

> 每个还在用假数据或占位的地方都登记在这，避免上架前漏掉。

| 位置 | 开关 | 当前值 | 含义 |
|------|------|--------|------|
| `feature/prediction/PredictionDetailScreen.kt` | `MOCK_PREVIEW` | `true` ⚠️ | UI 验收用假数据，验收完须改 `false` |
| `feature/risk/RiskDetailScreen.kt` | `MOCK_PREVIEW` | `true` ⚠️ | UI 验收用假数据，验收完须改 `false` |
| `feature/home/HomeScreen.kt` | `MOCK_PREVIEW` | `false` ✅ | 真实数据；保留 `.copy()` 补缺字段逻辑 |
| 首页 申请时间线卡 | — | 占位日期 | 待接 CaseRecord |
| 首页 公开数据卡 | — | 占位数值 | 待接 PublicData 真实值 |
| 4 个 Tab（申请/材料/数据/我的） | — | PlaceholderScreen | 待实现 |

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
