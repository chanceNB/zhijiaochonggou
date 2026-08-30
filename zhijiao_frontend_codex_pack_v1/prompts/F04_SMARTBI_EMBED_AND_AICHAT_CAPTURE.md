# F04 — SmartBI Embedded Data Insights + AIChat Manual Capture

目标：
数据洞察成为真正的 SmartBI 内嵌工作区。

## Read

- `06_SMARTBI_EMBED_SPEC.md`
- API SmartBI datasets/freshness/assets
- current backend SmartBI asset registry/config
- 不先假设某 assetKey，先读取真实实现/响应

## Page

`/teacher/analytics`

tabs：
1. 学习风险分析
2. AI数据分析
3. 干预效果分析

顶部：
- current student/course/KP context
- DataFreshnessBadge

## SmartBiEmbedPanel

只使用 Asset API 返回：
- status
- launchMode
- resourceUrl

IFRAME：
真正 iframe。

NEW_TAB：
资源卡 + open external。

UNVERIFIED：
标准文案。

503：
DEGRADED，不画 fake chart。

## Dashboard target

已知租户目标 URL 形式：
`/smartbi/vision/openresource.jsp?resid={RESID}`

但不要在 Vue 硬编码。
如果 backend 当前没有 verified resourceUrl：
- 保持 UNVERIFIED
- 报告平台配置 blocker
- 不造 resid

## AIChat target

目标入口：
`/smartbi/vision/aichat/proxy/#/`

验证 iframe：
- frame policy
- session
- third-party cookies

如果后台 asset registry 尚未暴露 AIChat：
只在已有配置机制中增加可配置 asset；不要把凭据放前端。

## RecommendationCaptureDrawer

AIChat iframe 外：
`记录分析建议`

支持：
- 粘贴 JSON
- Zod parse
- 人工编辑
- candidates 恰好 3
- source SMARTBI_AICHAT

POST `/teacher/analysis-recommendations`
使用 Idempotency-Key。

绝对禁止：
- iframe DOM scraping
- 自动声称 callback
- AIChat 直接 predictedLift

成功：
保存 recommendationId 到 teacherWork/intervention context
CTA → 干预决策。

## Tests

- asset IFRAME
- NEW_TAB
- UNVERIFIED
- 503
- FRESH/STALE
- capture valid 3 candidates
- invalid JSON
- candidates != 3 rejected
- no DOM access logic

Acceptance：
test/typecheck/build PASS。
如果租户允许，浏览器真实 iframe PASS；否则 fallback 行为 PASS 并准确报告。

Commit:
`feat(frontend): embed smartbi analytics workspace`
