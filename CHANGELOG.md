# Changelog

记录用户可见的行为变化，跟着 commit 同步。遵循 [Keep a Changelog](https://keepachangelog.com/) 风格。

## [Unreleased]

### Added
- 风险自检页（§10）：Hero 风险等级 + 82 环形、三大检查项（素行/生计/公益性）、建议补强、分析依据、免责声明；首页风险卡点击进入
- 预测详情页（§9）：Hero 区间卡、三参考区间、影响因素、公开数据依据、免责声明；首页预测卡点击进入
- 项目管理文档：`docs/PROGRESS.md`、`docs/DECISIONS.md`、本 `CHANGELOG.md`

### Changed
- 首页 Dashboard 按设计稿重写（审查中态）

### Fixed
- Onboarding 完成页被跳过：冻结 NavHost startDestination（`rememberSaveable`）
- 滚轮日期选择器居中项缺年/月/日单位：改用 `displayedValues`

---

历史（首两次提交）：
- `05b7f33` feat(home): 首页 Dashboard 按设计稿重写
- `719bc12` init: 永住 Tracker 工程骨架 + 主线 UI 按设计稿落地
