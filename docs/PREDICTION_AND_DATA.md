# 预测算法 & 公开数据管线方案

> 永住处理时间预测，从「固定 4-6 个月 + 拍脑袋缓冲」升级为「基于入管局真实积压/处理量的 FIFO 排队消化模型」。
> 本文是该方案的单一技术事实来源：数据源、托管方式、人工更新 SOP、算法公式、口径陷阱、降级策略。
> 决策摘要见 `DECISIONS.md`；产品规格见 `PRODUCT_PM.md` §11/§7.3.4/§14。

**最后更新**：2026-06-13（方案定稿，待实现）

---

## 0. 一句话

App 启动从 GitHub raw 拉一份人工维护的 JSON（各入管局每月 新受/既済/未済 + 年度许可数），用 FIFO 排队模型算「你这件还要等多久」，无后端、人工每月跟 e-Stat 更新。

---

## 1. 架构：A2（托管静态 JSON + 人工更新，无后端）

```
e-Stat 月報(每月发布,约3个月延迟)
   │  ← 人工：每月抄数 + 拆横滨，更新 JSON，commit push
   ▼
GitHub raw 上的 public-data.json   （静态文件，非后端服务）
   │  ← App 启动拉取
   ▼
App 三层降级：
   GitHub raw  →（失败）→ Room 缓存（上次拉到的）→（还没有）→ APK 内置 bundled 兜底
```

- **不是后端**：没有任何服务端逻辑、不抓 e-Stat，就是一个你手动维护的静态文件。
- **不用每月发版**：每月只改 JSON 文件 commit push，App 下次启动即生效。
- **永不白屏**：网络挂了用缓存，全新安装无网用 APK 内置兜底。

### 1.1 托管位置（待定 1 项）

- 文件路径建议：数据单独放，避免和 App 代码仓库耦合。两个选项：
  - **同仓库** `eijyo_tracker` 加 `data/public-data.json`
  - **单独数据仓库**（推荐，数据更新不污染 App commit 历史）
- 拉取 URL 两种，各有取舍：
  - `https://raw.githubusercontent.com/{user}/{repo}/{branch}/data/public-data.json` — 实时，但**国内访问不稳定**（用户多来自小红书=国内）
  - `https://cdn.jsdelivr.net/gh/{user}/{repo}@{branch}/data/public-data.json` — 有 CDN、国内相对稳，但缓存更新有延迟（~12h，或需 purge）
- ⚠️ **国内可达性是真实风险**，两个域名都可能被限。APK 内置兜底因此是必须的，不是可选。后续若发现都不稳，再考虑换更稳的静态托管（OSS/对象存储静态文件，仍非后端）。

---

## 2. 数据源（e-Stat 出入国管理統計）

| 用途 | 统计表 | e-Stat sid |
|------|--------|-----------|
| 受理及处理人员（旧受/新受/既済/未済） | 在留資格の取得等の受理及び処理人員 | [0003288730](https://www.e-stat.go.jp/dbview?sid=0003288730) / [0003449073](https://www.e-stat.go.jp/dbview?sid=0003449073) |
| 永住許可人员（年度许可数，趋势用） | 国籍・地域別 地方出入国在留管理局管内別 永住許可人員 | [0003289203](https://www.e-stat.go.jp/dbview?sid=0003289203) |

口径（日语统计术语）：

| 我们的字段 | e-Stat 口径 | 含义 |
|-----------|------------|------|
| `pending` | **未済** | 期末未处理 = 积压存量 Q |
| `received` | **新受** | 本期新受理 = 到达率 λ |
| `processed` | **既済** | 本期处理完成 = 处理率 μ（含许可+不许可+撤回） |
| （趋势）`permitsByYear` | 永住許可人員 | 年度纯许可数 |

- 发布频率：**月報（概数，每月）+ 年報（确定值，每年）**。算法用月報。
- 数据延迟：约 **3 个月**（属正常，已是最新可得）。
- ⚠️ **待拉数据时验证**：月報（概数）是否细到「地区 × 永住 × 未済」。竞品（xiaofushi.com，用 sid=0003449073）能做说明大概率有，但要核实。

---

## 3. 口径陷阱：东京含横滨，必须拆

- e-Stat 的「東京」管辖区**包含横浜支局**。
- 横滨 2025 年起处理超快（月 800+），东京总处理 2800+；横滨把东京平均拉高。
- **不拆**：东京用户看到虚低的等待。**拆掉横滨后东京都实际约 700 天**（≈23 个月，远超标准 4-6 月）。
- 处理：bundle 数据时人工把横滨从东京里减出来，`offices` 里东京填「已剔除横滨」的数，横滨单列，用 `excludesYokohama` 标记。
- 现有 `ImmigrationOffice` enum 东京、横滨已是独立项，建模上 OK。

---

## 4. JSON Schema

```jsonc
{
  "schemaVersion": 1,
  "dataAsOf": "2026-03",          // 这批数据覆盖的最新月份（e-Stat 最新可得月）
  "updatedAt": "2026-06-13",      // 人工更新此文件的日期
  "source": {
    "name": "出入国在留管理庁 出入国管理統計",
    "permitTableUrl": "https://www.e-stat.go.jp/dbview?sid=0003289203",
    "processTableUrl": "https://www.e-stat.go.jp/dbview?sid=0003288730"
  },
  "standardProcessing": {          // 全国官方标准处理期间（兜底用）
    "rangeLabel": "4 - 6 个月",
    "minMonths": 4,
    "maxMonths": 6
  },
  "offices": [
    {
      "code": "TOKYO",             // 对应 ImmigrationOffice enum name
      "displayName": "东京入管",
      "excludesYokohama": true,    // 已从东京数据中剔除横滨
      "regionalNote": "东京入管案件量大，剔除横滨支局后实际等待显著偏长。",
      "monthly": [                 // 受理处理月度序列（升序）
        { "month": "2026-01", "pending": 20000, "received": 2800, "processed": 2100 },
        { "month": "2026-02", "pending": 20300, "received": 2600, "processed": 2300 },
        { "month": "2026-03", "pending": 20100, "received": 2500, "processed": 2700 }
      ],
      "permitsByYear": [           // 年度许可数（趋势图/统计展示）
        { "year": 2023, "count": 12345 },
        { "year": 2024, "count": 13456 }
      ]
    }
    // OSAKA / NAGOYA / YOKOHAMA / KOBE / FUKUOKA ...
  ]
}
```

- `offices[].code` 覆盖现有 enum 的 6 个具体局；`OTHER`（其他/不确定）无地区数据，预测退回 `standardProcessing` 兜底。
- e-Stat 实际地方局比 enum 多（札幌/仙台/广岛/高松等）；MVP 只维护 enum 内的 6 个，其余归 OTHER。

---

## 5. 预测算法：FIFO 排队消化模型

复刻竞品思路。核心：**用「你提交那刻前面排了多少人」定位队列位置，扣掉之后已处理的，剩余用最新速度估**。

### 5.1 输入
- `submitMonth`：用户提交月（`ApplicationProfile.submittedDate`，精确到月即可）
- `office`：用户提交地区
- `series`：该 office 的 `monthly` 序列
- `latest`：序列最后一个月（= `dataAsOf`）
- `today`

### 5.2 步骤

```
1. 锚定队列位置 Q0
   Q0 = series[submitMonth].pending      // 提交当月末的未済（你前面的积压）
   // 提交月早于序列起点 → 用序列最早月的 pending（保守近似）

2. 扣减「提交后~最新数据月」已处理
   ΣP = Σ processed，从 submitMonth 之后 到 latest 月
   R_latest = Q0 - ΣP                    // 截至最新数据月，还排在你前面的

3. 外推数据延迟期（latest→today，约3个月无数据）
   gapMonths = 月差(today, latest)
   μ_latest  = series[latest].processed   // 最新月处理速度
   R_now = R_latest - μ_latest * gapMonths // 用最新速度把延迟期也消化掉

4. 剩余等待
   if R_now <= 0:
       状态 = 「已进入可能出结果区间」    // 按均值你已该轮到
   else:
       waitMonths = R_now / μ_latest
       预计完成 ≈ today + waitMonths

5. 区间（乐观/正常/保守），绝不给精确日
   μ_fast = 近 N 月最大 processed,  μ_slow = 近 N 月最小 processed
   乐观完成 = today + R_now / μ_fast
   正常完成 = today + R_now / μ_latest
   保守完成 = today + R_now / μ_slow
   // 若近月 received > processed（积压在涨），保守端再放宽

6. 输出转「上旬/中旬/下旬」标签（复用现有 DateLabels）
```

### 5.3 几个设计决定（可调，实现时定稿）
- **Q0 用提交当月末 `pending`**：近似「你提交时前面的队列」。更严谨可用期初未済，差一个月量级，影响小。
- **延迟期用 `μ_latest` 外推扣减**（步骤3）：让结果从「今天」起算、对用户直观，而非从 3 个月前的数据月起算。
- **「越来越准」在 A2 下打折**：数据靠每月人工更新；只要按时更新，每月重算时 ΣP 增大、外推成分减小，确实越来越准。漏更新则停在上次快照，外推误差随时间增长 → 这是 A2 相对竞品后端的固有差距，可接受。

### 5.4 置信度（沿用扣分制，新增因子）
- 数据新鲜度：`today - dataAsOf` 越久越低
- 提交日期精度：仅到月 −1
- `R_now` 接近 0 或为负：不确定性高 −1
- 该地区无数据（退回标准期间）：直接「较低」

### 5.5 边界 / 兜底
| 情况 | 处理 |
|------|------|
| 该 office 无数据 / OTHER | 退回旧逻辑（标准处理期间 4-6 月 + 现有 buffer） |
| 提交月早于序列 | Q0 用最早月 pending（保守） |
| `R_now <= 0` | 「已进入可能出结果区间」，不报负数 |
| `μ_latest == 0`（某月没处理） | 用近 N 月平均，避免除零 |
| 非审查中状态 | 同现状，不出预测 |

### 5.6 仍然给不了的
- **个案实时进度**（"你这件审到哪"）：入管不公开，本模型给的是「按平均你大概排到哪」，非真实个案。文案需说明是参考估算。
- 非 FIFO 偏差（复杂案/补资料打断）：用区间表达吸收，不给精确日。

---

## 6. 人工更新 SOP（每月）

1. 等 e-Stat 发布新月報（约每月中，覆盖 ~3 个月前的数据）。
2. 打开 [sid=0003288730](https://www.e-stat.go.jp/dbview?sid=0003288730)（受理处理）和 [sid=0003289203](https://www.e-stat.go.jp/dbview?sid=0003289203)（许可数）。
3. 抄各入管局「永住」的 新受/既済/未済；**把横滨从东京里拆出来**。
4. 更新 `public-data.json`：给每个 office 的 `monthly` 追加新月份；更新 `dataAsOf` / `updatedAt`；年初顺带更新 `permitsByYear`。
5. commit + push。
6. （可选）启动 App 确认拉到新数据、预测刷新。

> 负担提示：这是**每月**固定活儿，漏更新预测会逐渐失真。长期维护要扛得住。

---

## 7. App 端落地清单（实现时）

- [ ] 网络层：拉 JSON（Retrofit/Ktor），超时 + 失败处理
- [ ] 缓存：解析后存 Room；记录 `dataAsOf`
- [ ] 兜底：APK `assets/` 内置一份 `public-data.json`
- [ ] 降级链：raw → Room → assets
- [ ] 数据模型：`PublicDataDoc` / `OfficeData` / `MonthlyPoint`，替换/扩展现有 `staticdata/PublicData.kt`
- [ ] `PredictionEngine` 重写为 FIFO 模型，保留旧逻辑作 OTHER/无数据兜底
- [ ] 时间线共享：抽 `buildTimeline` 到 domain，首页只读摘要版 + 申请页完整版
- [ ] 公开数据卡 / 数据页：接 `permitsByYear` 真实趋势、地区许可数，删「暂无地区通过人数」
- [ ] UI：数据新鲜度标注「数据更新于 {dataAsOf}」、加载/错误/缓存态

---

## 8. 建议实现顺序

1. 数据结构 + JSON schema 落地 + 拉一批真实 e-Stat 数据填充（含横滨拆分验证）
2. 网络层 + 三层降级 + Room 缓存
3. 公开数据卡 / 数据页接真实数据（先把"看得见的真数据"交付）
4. 时间线共享重构
5. `PredictionEngine` 重写为 FIFO 模型（最后做，依赖前面的数据就位）

---

## 9. 参考

- 竞品 xiaofushi.com：同类功能，数据源 e-Stat sid=0003449073，算法即本文 FIFO 模型，后端每月拉 API（我们用 A2 人工替代）。
- e-Stat 总页：https://www.e-stat.go.jp/statistics/00250011
- 出入国在留管理庁 统计：https://www.moj.go.jp/isa/policies/statistics/index.html
