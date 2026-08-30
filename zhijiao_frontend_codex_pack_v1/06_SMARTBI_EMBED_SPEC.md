# SmartBI Embed Spec

## 1. 目标

SmartBI 是教师端“数据洞察”的真实分析引擎，不是外链附件，也不是 Vue 仿图。

目标体验：

Teacher Shell
→ 数据洞察
→ SmartBI Dashboard iframe
→ SmartBI AIChat iframe（验证后）
→ MANUAL_CAPTURE
→ 本地 Intervention

## 2. Dashboard

已知目标访问形式：

`https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/openresource.jsp?resid={RESID}`

resid 来自 SmartBI 资源节点 ID。

但组件不得写死 URL。
必须通过后端 SmartBI Asset API 获取 `resourceUrl`。

## 3. AIChat

已知租户入口：

`https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/aichat/proxy/#/`

验证重点：
- 是否允许 iframe
- SmartBI session 是否保留
- 第三方 Cookie
- CSP / X-Frame-Options
- 刷新后登录态

验证通过后由 Asset API 返回 IFRAME。
未通过使用 NEW_TAB/UNVERIFIED。

## 4. SmartBiEmbedPanel

Props：
- asset
- contextLabel

状态：
- LOADING
- READY_IFRAME
- READY_NEW_TAB
- UNVERIFIED
- FORBIDDEN
- ERROR

READY_IFRAME：
- iframe 100% 宽
- 最小 600px 高
- sandbox/allow 按平台实际需要最小化配置
- 右上保留“在 SmartBI 中打开”

不得：
- 读取 iframe DOM
- 注入脚本进 SmartBI
- 绕 CSP
- 传前端明文凭据

## 5. AIChat MANUAL_CAPTURE

AIChat iframe 外部提供：
`记录分析建议`

Drawer：
- analysisSummary
- evidenceRefs
- candidate A/B/C
  - strategyCode
  - title
  - rationale
  - actionDescription
- source fixed `SMARTBI_AICHAT`

支持：
- 手填
- 粘贴结构化 JSON → Zod parse → 表单回填

提交前必须教师确认。

## 6. 第一次 / 第二次分析

第一次：
- asset: student-risk（以真实 Asset API 为准）
- 页面 context = current student/course/KP
- cohort 参照只存在 SmartBI

第二次：
- asset: intervention-outcome
- 页面 context = current intervention/outcome

## 7. Freshness

DataFreshnessBadge 读取 `/analytics/smartbi/freshness`。

FRESH：
绿色

STALE：
橙色，并保留 Dashboard，但提示“分析数据可能滞后”

NO_DATA：
空态

SmartBI 503：
DEGRADED，不影响业务主链。
