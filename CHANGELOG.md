# Changelog

记录用户可见的行为变化，跟着 commit 同步。遵循 [Keep a Changelog](https://keepachangelog.com/) 风格。

## [Unreleased]

### Added
- 我的 Tab：正脸坐姿狗狗视觉区 + 用户档案卡（真实昵称/状态/入管）+ 设置列表；免责声明、关于 App 两个 Bottom Sheet 可用；语言设置入口暂灰（「即将支持」）等待本地化；隐私与数据 Sheet UI 占位
- 申请 Tab（§8）：状态卡、完整时间线（4 节点 + 连线 + 提示横幅）、信息摘要卡；「添加事件」Bottom Sheet 支持 7 种事件类型，收到补资料/许可/不许可/撤回均可写入数据库
- 风险自检页（§10）：Hero 风险等级 + 82 环形、三大检查项（素行/生计/公益性）、建议补强、分析依据、免责声明；首页风险卡点击进入
- 预测详情页（§9）：Hero 区间卡、三参考区间、影响因素、公开数据依据、免责声明；首页预测卡点击进入
- 项目管理文档：`docs/PROGRESS.md`、`docs/DECISIONS.md`、本 `CHANGELOG.md`

### Changed
- 首页 Dashboard 按设计稿重写（审查中态）

### Fixed
- 我的页狗狗耳朵图层错误：改为头盖住耳朵内缘，脸部干净、耳朵从两侧露出（对齐设计 node `RnhY9`）
- Onboarding 完成页被跳过：冻结 NavHost startDestination（`rememberSaveable`）
- 滚轮日期选择器居中项缺年/月/日单位：改用 `displayedValues`

---

历史（首两次提交）：
- `05b7f33` feat(home): 首页 Dashboard 按设计稿重写
- `719bc12` init: 永住 Tracker 工程骨架 + 主线 UI 按设计稿落地
