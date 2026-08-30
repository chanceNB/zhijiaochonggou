# Authority / Freeze

## A. 业务真源

按优先级：

1. 当前仓库实际后端实现与数据库迁移
2. `zhijiao_huiping_golden_demo_v2_codex_pack/04_API_INTERFACE_SPEC.md`
3. `00_GOLDEN_DEMO_V2_FREEZE.md`
4. `02_DOMAIN_BOUNDARIES_AND_CONTRACTS.md`
5. `03_DATA_MODEL_AND_SMARTBI_EXCHANGE.md`
6. `08_ERROR_AND_STATE_STANDARD.md`

发生冲突时，先报告冲突，不允许用旧前端逻辑覆盖新后端。

## B. 前端架构真源

- `05_FRONTEND_CONTRACT.md`
- `06_COMPONENT_LIBRARY.md`
- 本包 `proposed_contracts/` 中的 V3/V2 修订
- 本包 `03_UI_DESIGN_FREEZE_STUDENT_TEACHER.md`

F00 应把当前仓库契约与本包修订进行“合并式更新”，不得盲目覆盖未来新增事实。

## C. 视觉真源

旧项目只作为视觉真源：

- `legacy_visual_reference/`

允许复用：
- layout
- template composition
- CSS/SCSS
- spacing
- card density
- sidebar/header structure
- icon usage
-纯展示组件结构

禁止复用：
- old API
- old Store
- old Mock
- old DTO/types
- old permissions
- old route semantics
- old class/student business content
- old business calculations

## D. Golden Demo 学生主链（冻结）

历史/当前学习状态
→ AI Coach（StudentLearningSnapshot + RAG + Real LLM）
→ 生成 2 道诊断题
→ Practice Runner 真正作答
→ 可针对题目继续与 AI Coach 交流
→ 可基于真实 sourceAttemptId 生成相似题
→ 可显式加入错题本
→ complete PracticeSet
→ 后端更新权威 Learning State
→ Analytics Projection / smartbi_exchange
→ SmartBI 第一次分析
→ 教师侧闭环
→ Teacher Assignment
→ 学生 Today 收到任务
→ Practice / TransferValidation
→ InterventionOutcome
→ SmartBI 第二次分析
→ Growth

### 非常重要

固定：
- 诊断题 `questionCount = 2`

不固定：
- 正确题数
- 错误题数
- accuracy
- misconception
- wrong-book 数量
- mastery/confidence/forgettingRisk
- predictedLift/actualLift/deviation

演示脚本可以选择人为答成 1 对 1 错，但产品代码、测试、ViewModel 和页面不允许依赖这个结果。

## E. Golden Demo 教师主链（冻结）

工作台（当前演示聚焦小明，不做班级首页）
→ 数据洞察
→ SmartBI 第一次 Dashboard
→ SmartBI AIChat
→ MANUAL_CAPTURE 三个候选 Recommendation
→ 干预决策
→ 教师选择
→ B EffectEstimator
→ PROPOSED
→ APPROVED
→ COMMITTED / Assignment
→ 等待学生执行
→ 干预结果
→ 数据洞察
→ SmartBI 第二次 Dashboard
→ AIChat 复盘
→ Smart Report（只有真实平台资源验证后才显示可用）

班级/cohort 数据只作为 SmartBI 分析参照，不重新把教师 Web 工作台设计成班级管理 Dashboard。
