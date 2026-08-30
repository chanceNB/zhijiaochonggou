# F02 — Practice Hub + Runner + Result + WrongBook

目标：
把真正的做题闭环接上，并保证结果完全 runtime-driven。

## Implement

1. `/student/practice` Practice Hub
2. `/student/practice/:practiceSetId`
3. `/student/practice/:practiceSetId/result`
4. `/student/wrong-book`

## Practice Hub

不新增后端练习列表。
真实组合：
- today nextAction
- teacherAssignment
- current coach practiceSet
- wrongbook 待复习

无数据 → EMPTY。

## Runner

GET practice set。

逐题：
POST attempts
- Idempotency-Key
- 保存真实 attemptId
- 禁止重复提交
- 409 → refetch/current state

反馈：
- correct
- feedback.summary
- misconceptionCode
- canGenerateSimilar
- canAddWrongBook

按钮：
1. 和 AI 教练讨论
   - 导航到当前 coach
   - 带 question/attempt context 进入 store
2. 生成类似题
   - POST similar-questions
   - sourceAttemptId = 当前真实 attemptId
3. 加入错题本
   - POST practice-attempts/{attemptId}/wrong-book

## Result

POST complete。

必须使用真实：
- accuracy
- learningStateAfter
- transferValidation
- interventionOutcomeId

诊断练习：
- 0/2, 1/2, 2/2 全合法
- 不固定一对一错

Teacher Assignment：
- 只有后端返回时显示 transfer/outcome

## WrongBook

GET list
POST review

视觉贴旧项目：
tabs + list + details/drawer + actions。

## Tests

至少：
- 0/2
- 1/2
- 2/2
- wrongbook idempotent
- similar uses sourceAttemptId
- duplicate attempt conflict
- complete teacher assignment with PASS
- complete diagnostic without interventionOutcome

Acceptance：
test/typecheck/build PASS。
真实 API 完成一次 2题流程。
演示人工可选择 1对1错，但测试证明代码不依赖。

Commit:
`feat(frontend): implement runtime-driven practice flow`
