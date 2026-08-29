# 技术架构 V2

## 1. 架构选择

采用 **模块化单体 + 外部 SmartBI Cloud**。一个开发者从零重写时，不引入微服务、消息中间件和多仓库运维成本；模块边界仍通过 Port、Published Fact 和 Outbox 保持清晰。

```text
Vue Student / Teacher / Admin
            |
        Spring Boot API
            |
  +---------+-----------+------------------+
  |                     |                  |
Lane A / Student     Lane B / Teacher   Lane C / Platform
Learning/Twin        Diagnosis          Knowledge/RAG
AI Coach             Intervention       Analytics Exchange
Practice             Assignment         SmartBI Integration
WrongBook            Outcome
TransferValidation
  |                     |                  |
  +---------- Published Facts -------------+
                       |
                 Outbox / Projection
                       |
             smartbi_exchange schema
                       |
            SmartBI Cloud Data Source
                       |
       ETL -> Model -> Dashboard -> AIChat
                       |
               AnalysisRecommendation
                       |
                  Teacher Review
```

## 2. 后端模块

建议 package：

- `identity`：登录、角色、AuthContext。
- `student.learning`：LearningEvent、StudentLearningSnapshot、TwinKT。
- `student.coach`：CoachSession、CoachOrchestrator、LLMGateway、ActiveDiagnosis。
- `student.practice`：PracticeSet、Question、Attempt、PracticeOutcome、TransferValidation。
- `student.wrongbook`：WrongBookItem。
- `student.growth`：GrowthReadModel。
- `knowledge`：Document、Chunk、Embedding、KnowledgeQueryPort、RAG。
- `teacher.diagnosis`：DiagnosisCase、Evidence。
- `teacher.recommendation`：AnalysisRecommendation、RecommendationReview。
- `teacher.intervention`：EffectEstimator、Intervention、Assignment、Outcome。
- `analytics`：PublishedFact、Outbox、SmartBiProjection、Freshness、CSV Export。
- `smartbi`：PlatformAsset、launch adapter、recommendation adapter seam。
- `demo`：BaselineVersion、DemoRun、DEMO-GRAPH-001 reset/trace。

## 3. AI Coach

用户入口是 AI Coach，但诊断题不是“聊天文本”。

`StudentLearningSnapshot + RAG Evidence + ActiveDiagnosis → Structured Question Candidate → Validator → PracticeSet`

LLM 只负责语言生成/解释/候选题；正式 Question 必须结构化、持久化并获得 `questionId`。

## 4. RAG

`KnowledgeQueryPort` 输入 courseId、knowledgePointId、query、topK；输出带 documentId、chunkId、title、content、score 的 `RagEvidence[]`。AI Coach 回复必须可追踪到引用来源。

## 5. Analytics Exchange

C 不在 Java 中替 SmartBI 完成最终 ETL/语义建模。C 只做：

- 事实投影；
- ID/时间/版本/trace 统一；
- Baseline 与 Live Demo 标记；
- 只读数据源；
- Freshness；
- CSV 可复现导出。

SmartBI 平台内完成比赛要求的清洗、转换、校验、Data Model、Metric Model、Dashboard、AIChat、归因和 Smart Report。

## 6. SmartBI 接入

优先：`DATABASE_GATEWAY`。SmartBI 官方云能力支持通过数据库网关连接本地数据库；挑战杯租户需实际验证权限。

保底：`CANONICAL_CSV`。CSV 与数据库 View 共用同一字段 Contract，保证现场环境权限不足时仍可复现。

禁止直接公网暴露 PostgreSQL 5432。

## 7. SmartBI Recommendation 回传

定义 `SmartBiRecommendationPort`，但不假设挑战杯租户有结构化 API。

实现顺序：
1. `MANUAL_CAPTURE`：老师在 SmartBI AIChat 看完建议后，由教师端按结构化表单/候选策略确认记录；
2. `PLATFORM_API`：只有在真实租户验证 API/MCP/回调后再实现 Adapter。

业务层只依赖 `AnalysisRecommendation`，因此物理方式变化不重写 B 域。

## 8. 持久化与事件

采用 PostgreSQL + transactional outbox：业务事务同时写业务表和 `domain_event_outbox`。后台投影任务幂等消费 outbox，更新 `smartbi_exchange`。不需要 Kafka。

## 9. 安全

- HTTPS + Bearer JWT；不使用模板中的 MD5 自定义签名。
- 所有创建型 POST 使用 `Idempotency-Key`。
- 状态迁移使用 `If-Match` / version 防并发覆盖。
- SmartBI reader 只拥有 exchange schema 的 SELECT 权限。
- LLM/API Key 只在后端环境变量。
