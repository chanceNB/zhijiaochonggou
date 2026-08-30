# Test / Acceptance Standard

## 自动测试

最低要求：
- `npm run test:run`
- `npm run typecheck`
- `npm run build`

新增 UI 单测使用 Vitest + Vue Test Utils。

## 必测业务状态

### Student diagnostic

至少覆盖：
1. 2题0对
2. 2题1对
3. 2题2对

验证：
- 不存在 hard-coded 1/1
- result 使用真实 attempt/accuracy
- wrongbook/similar 按 feedback capability 渲染

### Today

- no assignment → AI Coach CTA
- PENDING_STUDENT assignment → TeacherAssignmentCard
- COMPLETED assignment → 不显示“待完成”

### AI Coach

- citations 有值
- citations 空
- RAG unavailable
- LLM unavailable
- diagnostic action 有/无

### Intervention

- PENDING recommendation
- PROPOSED
- APPROVED
- COMMITTED/PENDING_STUDENT
- COMPLETED outcome
- 409
- 412

### SmartBI

- IFRAME
- NEW_TAB
- UNVERIFIED
- 503 unavailable
- FRESH
- STALE

## 视觉验收

每个核心页截图：
- 1440×900
- 1672×941

检查：
- 无水平溢出
- Sidebar 宽度/active 风格贴近旧项目
- 卡片 radius 8~10px
- 无重阴影
- 页面标题不巨大
- 信息密度与旧页面同系
- AI 是助手模块，不成为整页视觉主题
- SmartBI iframe 占数据洞察主体

## Golden Demo 浏览器验收

真实 API，不用 Mock 伪主链：

学生：
Today → AI Coach → 2题 → feedback → AI讨论入口 → similar/wrongbook → complete

教师：
Workbench → SmartBI risk → AIChat → manual recommendation → intervention → approve → commit

学生：
Today Assignment → Practice → complete/transfer

教师：
Outcome → SmartBI intervention outcome

学生：
Growth

演示可以人为选择一对一错，但自动化测试和 UI 不能依赖它。
