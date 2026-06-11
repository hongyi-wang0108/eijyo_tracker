# 决定记录 — 永住 Tracker

> PMdoc 没覆盖、但开发中口头/临时敲定的产品·交互·技术决定。带日期和理由，防止下次忘记「当时为什么这么做」。
> 纯代码结构无需记这里（看代码即可）；只记「看代码看不出动机」的决定。

格式：`### YYYY-MM-DD — 标题` ＋ **决定 / 理由 / 影响**。

---

### 2026-06-11 — 三参考区间卡片等高，以最高那张为准

**决定**：预测详情页「三种参考区间」三张卡片用 `IntrinsicSize.Max` 统一高度，以内容最高的那张为基准撑高其余两张。
**理由**：「正常」区间显示完整区间（如 `2026年9月中旬-11月上旬`）会换行变高，而乐观/保守是短单点，三张不等高难看。用户选择保留完整文案、靠等高对齐。
**影响**：`ScenarioCard` 的 Row 加 `IntrinsicSize.Max`，各 `Scenario` 用 `fillMaxHeight()`。

### 2026-06-11 — 采用瘦身版项目管理流程

**决定**：用 `docs/PROGRESS.md` + `docs/DECISIONS.md` + `CHANGELOG.md` 三件套，不上完整六件套（README/prd/adr/checklist/release-notes）。
**理由**：单人 + AI 结对项目，全套会烂尾成噪音。核心诉求是「跨会话记住进度」，一个永远最新的 PROGRESS 即可解决。ADR/release-notes 等以后真需要再加。
**影响**：每次有意义改动按流程：说清范围 → 更 PROGRESS/DECISIONS → 看代码 → 复用优先 → 改 → 编译 → 同步文档 → 总结。测试步骤暂跳过。

### 2026-06-11 — 首页 MOCK 用 `.copy()` 只补缺失字段

**决定**：首页预览「审查中」态时，不硬编码整份假 state，而是 `realState.copy(field = realState.field ?: mock)`，真实数据优先，仅缺失字段用假值兜底。
**理由**：让用户看到自己真实的昵称/入管/等待天数，同时能预览「审查中」视觉态。
**影响**：`HomeScreen.kt` 保留该逻辑块；`MOCK_PREVIEW=false` 时也安全。
