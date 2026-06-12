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

### 2026-06-12 — 语言切换暂缓，先灰掉入口

**决定**：「我的」Tab 的「语言设置」行做成禁用态（变灰 + 不可点 + 「即将支持」标签）。底层基础设施（`LanguagePrefs` SharedPrefs + `MainActivity.attachBaseContext` 应用 locale + `findActivity().recreate()`）全部保留并已验证可用。
**理由**：Android 语言切换只对 `res/strings.xml` 里的资源字符串生效，但全 App 文字目前硬编码在 Kotlin 里。要真正生效需做「全 App 本地化」独立工程（抽字符串 + 日英翻译），MVP 阶段页面还在逐个搭，过早本地化会反复返工。用户拍板暂缓。
**影响**：`SettingsRowData.enabled` 标志控制；重新启用只需去掉 `enabled = false`。本地化工程列入上架前 backlog。

### 2026-06-12 — 我的页狗狗用专属 `DogProfile`，非欢迎页 `DogMascot`

**决定**：「我的」Tab 视觉区用新建的 `DogProfile`（正脸坐姿，对应设计 node `RnhY9`），不复用欢迎页的 `DogMascot`（站立全身）。
**理由**：两者是不同素材；最初误用 `DogMascot` 导致比例/姿态不符。
**耳朵 z-order**：绕了一圈——最终是 **head 画在耳朵之上**（body→耳朵→head→五官）。设计 JSON 里耳朵虽是 head 之后的 children，但实际渲染效果是「干净的脸、耳朵从两侧露出、内缘被头盖住」。若把耳朵画在头前，会糊到脸上、盖住眼睛，与设计不符。**以截图视觉为准，不照搬 JSON child 顺序。**
**影响**：`DogMascot.kt` 新增 `DogProfile` + `drawDogProfile`，坐标严格照搬 node `RnhY9`（186×161.2 参考框），耳朵 ±12° 旋转。

### 2026-06-12 — 数据详情页（§11.5）删除

**决定**：删除数据详情页（DataDetailScreen）规格和设计稿，数据 Tab 各卡片改为不可点击。
**理由**：详情页内容与主卡片高度重叠（同样的处理期间/来源信息），读只读、无操作入口，MVP 阶段对用户无增量价值。
**影响**：PMdoc §11.5 已清空为「已删除」注释；§11.4 各卡片交互改为「不可点击」；Pencil 设计稿同步删除。

### 2026-06-11 — 首页 MOCK 用 `.copy()` 只补缺失字段

**决定**：首页预览「审查中」态时，不硬编码整份假 state，而是 `realState.copy(field = realState.field ?: mock)`，真实数据优先，仅缺失字段用假值兜底。
**理由**：让用户看到自己真实的昵称/入管/等待天数，同时能预览「审查中」视觉态。
**影响**：`HomeScreen.kt` 保留该逻辑块；`MOCK_PREVIEW=false` 时也安全。
