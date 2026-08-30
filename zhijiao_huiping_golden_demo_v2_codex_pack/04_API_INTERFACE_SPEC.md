# 智教慧评 Golden Demo V2 RESTful API 接口规范
版本：2.0.0  
Base URL：`http://localhost:8080/api/v1`（本地）  
生产必须使用 HTTPS。
## 1、简介
本文档定义从零重写后的学生 AI Coach、RAG、练习/错题、教师干预、Analytics Exchange 与 SmartBI 集成接口。接口格式参考用户提供的接口规范模板：保留“简介、接口规范、测试地址、公共内容、接口请求详细、返回码”等章节，但认证采用 JWT/HTTPS，不复制模板中的 MD5/字段加密示例。
## 2、接口规范
### 2.1 测试地址
`http://localhost:8080/api/v1`
### 2.2 认证、幂等与并发
- Authorization: Bearer JWT。
- 创建/命令型 POST 使用 `Idempotency-Key`。
- 状态迁移使用 `If-Match` 对资源 version 做乐观并发控制。
### 2.3 加密解密
业务 JSON 不再自定义字段级加密；传输使用 HTTPS/TLS。密码、JWT、LLM Key、SmartBI凭据不得记录日志。
### 2.4 其它公共内容
统一 Envelope、UTC 时间、分页、错误码见 `08_ERROR_AND_STATE_STANDARD.md`。
### 2.5 接口请求详细
#### 2.5.1 认证
##### 2.5.1.1 登录
**功能描述**：学生、教师、管理员登录并获取 JWT。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/auth/login`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| username | 账号 | String | 必填 |
| password | 密码 | String | 必填 |

请求示例：
```json
{
  "username": "xiaoming",
  "password": "******"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| accessToken | JWT | String | 必返 |
| expiresIn | 有效秒数 | Integer | 必返 |
| user | 用户摘要 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "accessToken": "eyJ...",
    "expiresIn": 7200,
    "user": {
      "userId": "stu-xiaoming",
      "displayName": "小明",
      "role": "STUDENT"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.1.2 当前用户
**功能描述**：获取当前登录用户、角色和权限。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/auth/me`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| userId | 用户ID | String | 必返 |
| displayName | 显示名 | String | 必返 |
| role | 角色 | Enum | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "userId": "stu-xiaoming",
    "displayName": "小明",
    "role": "STUDENT"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
#### 2.5.2 学生端
##### 2.5.2.1 今日首页
**功能描述**：返回下一最佳学习行动、当前学习状态和教师 Assignment。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/student/today`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| courseId | 课程ID | String | 选填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| nextAction | 下一行动 | Object | 必返 |
| teacherAssignment | 教师任务 | Object/null | 可空 |
| learningState | 学习状态 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "studentId": "stu-xiaoming",
    "nextAction": {
      "type": "AI_COACH_DIAGNOSTIC",
      "title": "确认图遍历 BFS / DFS 薄弱点",
      "knowledgePointId": "kp-graph-bfs-dfs",
      "estimatedMinutes": 10
    },
    "teacherAssignment": null,
    "learningState": {
      "mastery": 0.62,
      "confidence": 0.71,
      "forgettingRisk": 0.57
    },
    "demoCaseId": "DEMO-GRAPH-001"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.2 学习状态
**功能描述**：读取权威 StudentLearningSnapshot；前端不得自行计算 mastery。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/student/learning-state`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| courseId | 课程ID | String | 必填 |
| knowledgePointId | 知识点ID | String | 选填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| mastery | 掌握度 | Number | 0..1 |
| confidence | 置信度 | Number | 0..1 |
| forgettingRisk | 遗忘风险 | Number | 0..1 |
| weakKnowledgePoints | 薄弱点 | Array | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "studentId": "stu-xiaoming",
    "courseId": "course-data-structures",
    "mastery": 0.62,
    "confidence": 0.71,
    "forgettingRisk": 0.57,
    "weakKnowledgePoints": [
      {
        "knowledgePointId": "kp-graph-bfs-dfs",
        "name": "图遍历 BFS / DFS",
        "mastery": 0.62
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.3 创建 AI Coach 会话
**功能描述**：创建真实 AI Coach 会话并绑定课程/当前知识点上下文。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/coach/sessions`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| studentId | 学生ID（未接入认证时显式传入） | String | 必填 |
| courseId | 课程ID | String | 必填 |
| knowledgePointId | 知识点ID | String | 选填 |
| mode | 会话模式 | Enum | TUTOR/DIAGNOSTIC |

请求示例：
```json
{
  "studentId": "stu-xiaoming",
  "courseId": "course-data-structures",
  "knowledgePointId": "kp-graph-bfs-dfs",
  "mode": "DIAGNOSTIC"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| sessionId | 会话ID | String | 必返 |
| context | 学习上下文 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "sessionId": "coach-001",
    "context": {
      "mastery": 0.62,
      "weakKnowledgePoint": "图遍历 BFS / DFS"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.4 AI Coach 对话
**功能描述**：发送消息。后端执行 Twin Context + RAG + Real LLM，引用必须来自真实检索结果。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/coach/sessions/{sessionId}/messages`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| sessionId | 会话ID | Path | 必填 |
| message | 用户消息 | String | 必填 |

请求示例：
```json
{
  "message": "为什么 BFS 要用队列？"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| assistantMessage | AI回复 | String | 必返 |
| citations | RAG引用 | Array | 可空 |
| actions | 可执行动作 | Array | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "assistantMessage": "BFS需要按层扩展节点，因此队列可以保持先进先出的访问顺序。",
    "citations": [
      {
        "documentId": "doc-ds-01",
        "chunkId": "chunk-119",
        "title": "数据结构课程讲义·图遍历",
        "excerpt": "广度优先搜索使用队列维护待访问顶点。"
      }
    ],
    "actions": [
      {
        "type": "START_DIAGNOSTIC",
        "label": "用两道题确认一下"
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.5 生成 2 道诊断题
**功能描述**：通过 ActiveDiagnosis + RAG + LLM 生成结构化诊断 PracticeSet，默认 2 题。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/coach/sessions/{sessionId}/diagnostic-sets`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| knowledgePointId | 知识点ID | String | 必填 |
| questionCount | 题数 | Integer | 固定2 |

请求示例：
```json
{
  "knowledgePointId": "kp-graph-bfs-dfs",
  "questionCount": 2
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| practiceSetId | 练习集ID | String | 必返 |
| questionCount | 题数 | Integer | 必返 |
| selectionEvidence | 选题依据 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "practiceSetId": "ps-diag-001",
    "questionCount": 2,
    "selectionEvidence": {
      "target": "区分BFS访问时机与DFS回溯理解",
      "source": "ACTIVE_DIAGNOSIS"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.6 生成相似题
**功能描述**：根据原题、知识点、误概念、难度和 RAG 生成新的结构化题目，不返回纯聊天文本题。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/coach/sessions/{sessionId}/similar-questions`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| sourceAttemptId | 原作答ID | String | 必填 |
| count | 题数 | Integer | 1..3 |

请求示例：
```json
{
  "sourceAttemptId": "attempt-101",
  "count": 1
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| practiceSetId | 新练习集ID | String | 必返 |
| questions | 结构化题目 | Array | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "practiceSetId": "ps-similar-001",
    "questions": [
      {
        "questionId": "q-sim-01",
        "type": "SINGLE_CHOICE",
        "stem": "以下哪种结构最适合BFS的待访问节点管理？",
        "options": [
          "栈",
          "队列",
          "堆",
          "哈希表"
        ]
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.7 获取练习集
**功能描述**：获取题目、进度和来源上下文。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/student/practice-sets/{practiceSetId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| practiceSetId | 练习集ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| questions | 题目 | Array | 必返 |
| source | 来源 | Enum | AI_COACH_DIAGNOSTIC/AI_COACH_SIMILAR/TEACHER_ASSIGNMENT |
| context | 关联上下文 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "practiceSetId": "ps-diag-001",
    "source": "AI_COACH_DIAGNOSTIC",
    "questions": [
      {
        "questionId": "q-001",
        "type": "SINGLE_CHOICE",
        "stem": "BFS通常使用哪种结构？",
        "options": [
          "栈",
          "队列",
          "集合",
          "树"
        ]
      },
      {
        "questionId": "q-002",
        "type": "TRUE_FALSE",
        "stem": "DFS递归返回时会发生回溯。"
      }
    ],
    "context": {
      "coachSessionId": "coach-001",
      "demoCaseId": "DEMO-GRAPH-001"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.8 提交单题作答
**功能描述**：提交结构化作答并返回权威判定和解释。每道题产生唯一 attemptId。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/practice-sets/{practiceSetId}/attempts`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| questionId | 题目ID | String | 必填 |
| answer | 答案 | String/Object | 必填 |
| durationSeconds | 用时 | Integer | 必填 |

请求示例：
```json
{
  "questionId": "q-001",
  "answer": "栈",
  "durationSeconds": 36
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| attemptId | 作答ID | String | 必返 |
| correct | 是否正确 | Boolean | 必返 |
| feedback | 反馈 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "attemptId": "attempt-101",
    "correct": false,
    "feedback": {
      "summary": "BFS通常使用队列。",
      "misconceptionCode": "BFS_QUEUE_ORDER",
      "canAddWrongBook": true,
      "canGenerateSimilar": true
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.9 完成练习集
**功能描述**：结束本次练习；生成 PracticeOutcome，并触发学习状态更新/Analytics Outbox。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/practice-sets/{practiceSetId}/complete`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| practiceSetId | 练习集ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| outcomeId | 结果ID | String | 必返 |
| accuracy | 正确率 | Number | 必返 |
| learningStateAfter | 更新后学习状态 | Object | 必返 |
| transferValidation | 迁移验证 | Enum/null | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "outcomeId": "po-001",
    "accuracy": 0.0,
    "learningStateAfter": {
      "mastery": 0.56,
      "confidence": 0.68,
      "forgettingRisk": 0.63
    },
    "transferValidation": null
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.10 加入错题本
**功能描述**：学生显式将某次错误 attempt 加入错题本；幂等，重复添加返回同一 wrongItemId。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/practice-attempts/{attemptId}/wrong-book`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| attemptId | 作答ID | Path | 必填 |
| reason | 学生备注 | String | 选填 |

请求示例：
```json
{
  "reason": "我把BFS和DFS的数据结构记反了"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| wrongItemId | 错题项ID | String | 必返 |
| status | 状态 | Enum | TO_REVIEW |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "wrongItemId": "wrong-001",
    "status": "TO_REVIEW"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.11 错题本列表
**功能描述**：分页查询错题，支持知识点和状态过滤。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/student/wrong-book`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| knowledgePointId | 知识点ID | String | 选填 |
| status | 状态 | Enum | 选填 |
| page | 页码 | Integer | 默认1 |
| size | 页大小 | Integer | 默认20 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| items | 错题列表 | Array | 必返 |
| page | 分页 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "items": [
      {
        "wrongItemId": "wrong-001",
        "questionId": "q-001",
        "knowledgePointName": "图遍历 BFS / DFS",
        "reason": "BFS_QUEUE_ORDER",
        "status": "TO_REVIEW",
        "reviewCount": 0
      }
    ],
    "page": {
      "page": 1,
      "size": 20,
      "total": 1
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.12 复习错题
**功能描述**：记录一次错题复习/重做结果。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/student/wrong-book/{wrongItemId}/review`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| answer | 本次答案 | String/Object | 必填 |
| durationSeconds | 用时 | Integer | 必填 |

请求示例：
```json
{
  "answer": "队列",
  "durationSeconds": 18
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| correct | 是否正确 | Boolean | 必返 |
| status | 错题状态 | Enum | 必返 |
| reviewCount | 复习次数 | Integer | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "correct": true,
    "status": "LEARNING",
    "reviewCount": 1
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.2.13 成长页
**功能描述**：读取学生长期 GrowthReadModel，展示学习、错题修复和干预前后变化。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/student/growth`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| courseId | 课程ID | String | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| mastery | 掌握度 | Number | 必返 |
| trend | 趋势 | Array | 必返 |
| completedTasks | 完成任务 | Integer | 必返 |
| repairedMisconceptions | 修复误概念 | Integer | 必返 |
| latestIntervention | 最近干预 | Object/null | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "mastery": 0.69,
    "trend": [
      {
        "date": "2026-08-27",
        "mastery": 0.62
      },
      {
        "date": "2026-08-29",
        "mastery": 0.69
      }
    ],
    "completedTasks": 4,
    "repairedMisconceptions": 1,
    "latestIntervention": {
      "strategyCode": "VISUAL_TRANSFER_PRACTICE",
      "masteryBefore": 0.56,
      "masteryAfter": 0.69,
      "transferValidation": "PASS"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
#### 2.5.3 教师端
##### 2.5.3.1 教师工作台
**功能描述**：教师待办、重点学生、待确认建议和待验证干预。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/teacher/workbench`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| priorityItems | 重点待办 | Array | 必返 |
| pendingRecommendations | 待确认建议 | Integer | 必返 |
| pendingOutcomes | 待验证结果 | Integer | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "priorityItems": [
      {
        "type": "SMARTBI_RECOMMENDATION",
        "studentId": "stu-xiaoming",
        "studentName": "小明",
        "title": "图遍历风险需要干预",
        "recommendationId": "rec-001"
      }
    ],
    "pendingRecommendations": 1,
    "pendingOutcomes": 0
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.2 学生画像
**功能描述**：教师查看单学生业务画像；聚合 cohort 分析跳 SmartBI。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/teacher/students/{studentId}/profile`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| studentId | 学生ID | Path | 必填 |
| courseId | 课程ID | String | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| student | 学生 | Object | 必返 |
| learningState | 学习状态 | Object | 必返 |
| recentAttempts | 近期作答 | Array | 必返 |
| diagnosis | 诊断 | Object/null | 可空 |
| intervention | 干预 | Object/null | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "student": {
      "studentId": "stu-xiaoming",
      "name": "小明",
      "className": "2024级计算机1班"
    },
    "learningState": {
      "mastery": 0.56,
      "forgettingRisk": 0.63
    },
    "recentAttempts": [
      {
        "attemptId": "attempt-101",
        "correct": false
      },
      {
        "attemptId": "attempt-102",
        "correct": false
      }
    ],
    "diagnosis": {
      "caseId": "case-001",
      "severity": "HIGH"
    },
    "intervention": null
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.3 诊断 Case 详情
**功能描述**：读取诊断假设、支持证据、反向证据和状态。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/teacher/diagnosis-cases/{caseId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| caseId | Case ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| primaryHypothesis | 主要假设 | Object | 必返 |
| evidence | 支持证据 | Array | 必返 |
| counterEvidence | 反向证据 | Array | 必返 |
| severity | 严重度 | Enum | 必返 |
| confidence | 置信度 | Number | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "caseId": "case-001",
    "severity": "HIGH",
    "confidence": 0.82,
    "primaryHypothesis": {
      "code": "GRAPH_TRAVERSAL_CONFUSION",
      "text": "BFS访问时机与DFS回溯理解不稳定"
    },
    "evidence": [
      "attempt-101",
      "attempt-102"
    ],
    "counterEvidence": []
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.4 记录 SmartBI AIChat 建议
**功能描述**：把 SmartBI AIChat 已展示的分析建议结构化记录到业务系统。初期为 MANUAL_CAPTURE；未来可由平台 Adapter 调同一 Service。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/teacher/analysis-recommendations`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| studentId | 学生ID | String | 必填 |
| knowledgePointId | 知识点ID | String | 必填 |
| analysisSummary | 分析摘要 | String | 必填 |
| evidenceRefs | 证据引用 | Array<String> | 可选 |
| candidates | 三个候选方案 | Array | 固定3 |
| source | 来源 | Enum | SMARTBI_AICHAT |

请求示例：
```json
{
  "studentId": "stu-xiaoming",
  "courseId": "course-data-structures",
  "classId": "class-cs-2024-01",
  "knowledgePointId": "kp-graph-bfs-dfs",
  "analysisSummary": "两道诊断题均错误，主要集中在访问顺序和回溯。",
  "candidates": [
    {
      "strategyCode": "CONCEPT_REMEDIATION",
      "title": "方案A",
      "rationale": "先纠正概念边界",
      "actionDescription": "概念辨析+低难度专项"
    },
    {
      "strategyCode": "VISUAL_TRANSFER_PRACTICE",
      "title": "方案B",
      "rationale": "用可视化过程建立正确迁移",
      "actionDescription": "BFS/DFS过程演示+变式练习"
    },
    {
      "strategyCode": "AI_GUIDED_VARIATION",
      "title": "方案C",
      "rationale": "持续个性化反馈",
      "actionDescription": "AI Coach引导+分层变式"
    }
  ],
  "source": "SMARTBI_AICHAT"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| recommendationId | 建议ID | String | 必返 |
| status | 状态 | Enum | PENDING_TEACHER_REVIEW |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "recommendationId": "rec-001",
    "status": "PENDING_TEACHER_REVIEW"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```

> 注意：不得宣称 SmartBI 已自动回调；挑战杯租户 API 能力未验证前由教师人工确认录入/选择。
##### 2.5.3.4a 查询 SmartBI AIChat 建议
**功能描述**：读取 MANUAL_CAPTURE 的不可变候选快照，供教师审核选择；不会返回已批准或已下发的业务状态。
**接口方式**：HTTP GET
**接口地址**：`/api/v1/teacher/analysis-recommendations/{recommendationId}`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| candidates | 候选方案快照 | Array | 固定3，不可变 |
| source | 来源 | Enum | SMARTBI_AICHAT |
| captureMode | 捕获方式 | Enum | MANUAL |
| status | 状态 | Enum | PENDING_TEACHER_REVIEW |

##### 2.5.3.5 创建正式干预
**功能描述**：教师从 Recommendation 中选择一个 strategy；B 运行 EffectEstimator 后创建 PROPOSED Intervention。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/teacher/interventions`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| recommendationId | 建议ID | String | 必填 |
| strategyCode | 选择策略 | String | 必填 |
| teacherRationale | 教师理由 | String | 必填，>=10字 |

请求示例：
```json
{
  "recommendationId": "rec-001",
  "strategyCode": "VISUAL_TRANSFER_PRACTICE",
  "teacherRationale": "小明当前主要问题是遍历过程混淆，优先用可视化和迁移题验证。"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| interventionId | 干预ID | String | 必返 |
| predictedLift | 预测提升 | Number | B权威事实 |
| predictionInterval | 预测区间 | Object | 必返 |
| status | 状态 | Enum | PROPOSED |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "interventionId": "int-001",
    "predictedLift": 0.11,
    "predictionInterval": {
      "low": 0.07,
      "high": 0.15
    },
    "status": "PROPOSED",
    "version": 1
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.6 批准干预
**功能描述**：教师审批 PROPOSED → APPROVED。需要 If-Match。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/teacher/interventions/{interventionId}/approve`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| interventionId | 干预ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| status | 状态 | Enum | APPROVED |
| version | 版本 | Integer | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "interventionId": "int-001",
    "status": "APPROVED",
    "version": 2
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.7 提交并下发干预
**功能描述**：APPROVED → COMMITTED，并创建 InterventionAssignment 给学生 Today。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/teacher/interventions/{interventionId}/commit`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| interventionId | 干预ID | Path | 必填 |
| dueAt | 截止时间 | DateTime | 选填 |

请求示例：
```json
{
  "dueAt": "2026-08-29T18:00:00+08:00"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| assignmentId | 任务ID | String | 必返 |
| practiceSetId | 练习集ID | String | 必返 |
| status | 干预状态 | Enum | COMMITTED |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "assignmentId": "assign-001",
    "practiceSetId": "ps-int-001",
    "status": "COMMITTED",
    "version": 3
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.8 干预详情
**功能描述**：读取完整生命周期、预测、Assignment 和学生执行状态。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/teacher/interventions/{interventionId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| interventionId | 干预ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| status | 状态 | Enum | 必返 |
| strategy | 策略 | Object | 必返 |
| effectEstimate | 效果估计 | Object | 必返 |
| assignment | 任务 | Object/null | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "interventionId": "int-001",
    "status": "COMMITTED",
    "strategy": {
      "strategyCode": "VISUAL_TRANSFER_PRACTICE"
    },
    "effectEstimate": {
      "predictedLift": 0.11,
      "low": 0.07,
      "high": 0.15
    },
    "assignment": {
      "assignmentId": "assign-001",
      "practiceSetId": "ps-int-001",
      "status": "PENDING_STUDENT"
    },
    "version": 3
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.3.9 干预结果
**功能描述**：学生完成后读取 B 权威 InterventionOutcome。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/teacher/interventions/{interventionId}/outcome`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| interventionId | 干预ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| predictedLift | 预测提升 | Number | 必返 |
| actualLift | 实际提升 | Number | 必返 |
| predictionDeviation | 偏差 | Number | 必返 |
| transferValidation | 迁移验证 | Enum | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "interventionId": "int-001",
    "predictedLift": 0.11,
    "actualLift": 0.13,
    "predictionDeviation": 0.02,
    "transferValidation": "PASS",
    "masteryBefore": 0.56,
    "masteryAfter": 0.69
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
#### 2.5.4 知识库
##### 2.5.4.1 上传课程资料
**功能描述**：上传 PDF/DOCX/TXT/MD，建立课程知识库。使用 multipart/form-data。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/admin/knowledge/documents`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| courseId | 课程ID | Form | 必填 |
| file | 文件 | File | 必填 |
| title | 标题 | String | 选填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| documentId | 文档ID | String | 必返 |
| status | 状态 | Enum | UPLOADED |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "documentId": "doc-ds-01",
    "status": "UPLOADED"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.4.2 课程资料列表
**功能描述**：查看解析/索引状态。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/admin/knowledge/documents`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| courseId | 课程ID | String | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| items | 资料列表 | Array | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "items": [
      {
        "documentId": "doc-ds-01",
        "title": "数据结构课程讲义",
        "status": "INDEXED",
        "chunkCount": 126
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.4.3 索引课程资料
**功能描述**：解析、切块、Embedding 并写入 pgvector；幂等重建。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/admin/knowledge/documents/{documentId}/index`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| documentId | 文档ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| jobId | 任务ID | String | 必返 |
| status | 状态 | Enum | QUEUED/RUNNING/SUCCEEDED/FAILED |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "jobId": "rag-job-001",
    "status": "QUEUED"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
#### 2.5.5 Analytics/SmartBI
##### 2.5.5.1 数据集清单
**功能描述**：返回 canonical dataset 元数据、字段版本和可用行数。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/analytics/smartbi/datasets`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| datasets | 数据集 | Array | 必返 |
| contractVersion | 契约版本 | String | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "contractVersion": "smartbi-exchange-v2",
    "datasets": [
      {
        "name": "sb_fact_learning_state",
        "rows": 2480
      },
      {
        "name": "sb_fact_practice_attempt",
        "rows": 7421
      },
      {
        "name": "sb_fact_intervention_outcome",
        "rows": 12
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.5.2 数据新鲜度
**功能描述**：查看业务事实投影到 exchange 的延迟。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/analytics/smartbi/freshness`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| lastBusinessEventAt | 最后业务事件 | DateTime | 可空 |
| lastProjectedAt | 最后投影 | DateTime | 可空 |
| lagSeconds | 延迟秒 | Integer | 必返 |
| status | 状态 | Enum | NO_DATA/FRESH/STALE |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "lastBusinessEventAt": "2026-08-29T08:10:00Z",
    "lastProjectedAt": "2026-08-29T08:10:02Z",
    "lagSeconds": 2,
    "status": "FRESH"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.5.3 创建 CSV 导出
**功能描述**：生成 Baseline + active demo run 的 canonical CSV 包，作为 SmartBI 上传保底。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/analytics/smartbi/exports`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| scope | 导出范围 | Enum | ACTIVE_DEMO/CURRENT_ALL |
| demoRunId | 演示Run | String | ACTIVE_DEMO必填 |

请求示例：
```json
{
  "scope": "ACTIVE_DEMO",
  "demoRunId": "demo-run-001"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| exportId | 导出任务 | String | 必返 |
| status | 状态 | Enum | QUEUED |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "exportId": "export-001",
    "status": "QUEUED"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.5.4 查询 CSV 导出
**功能描述**：查询导出状态与下载文件清单。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/analytics/smartbi/exports/{exportId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| exportId | 导出ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| status | 状态 | Enum | 必返 |
| files | 文件 | Array | 成功时返回 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "exportId": "export-001",
    "status": "SUCCEEDED",
    "files": [
      "sb_dim_course.csv",
      "sb_dim_class.csv",
      "sb_dim_student.csv",
      "sb_dim_knowledge_point.csv",
      "sb_fact_learning_state.csv",
      "sb_fact_practice_attempt.csv",
      "sb_fact_wrong_book.csv",
      "sb_fact_diagnosis.csv",
      "sb_fact_intervention.csv",
      "sb_fact_intervention_outcome.csv",
      "manifest.json"
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
本地基础实现以同步方式生成 canonical CSV 包，成功状态为 `SUCCEEDED`；返回的 `files` 同时包含 10 个数据集 CSV 与 `manifest.json`。SmartBI 正式接入仍直接读取 PostgreSQL `smartbi_exchange`，不改为 REST 拉取。
##### 2.5.5.5 Demo Trace
**功能描述**：按 demoCaseId 汇总全链 trace，供验收/调试，不作为普通学生UI。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/analytics/demo-traces/{demoCaseId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| demoCaseId | 演示Case | Path | 必填 |
| demoRunId | 演示Run | String | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| events | 链路事件 | Array | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "demoCaseId": "DEMO-GRAPH-001",
    "demoRunId": "demo-run-001",
    "events": [
      {
        "stage": "AI_COACH_DIAGNOSTIC",
        "ref": "ps-diag-001"
      },
      {
        "stage": "SMARTBI_RECOMMENDATION",
        "ref": "rec-001"
      },
      {
        "stage": "INTERVENTION",
        "ref": "int-001"
      },
      {
        "stage": "TRANSFER_VALIDATION",
        "ref": "tv-001"
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.5.6 SmartBI 资产列表
**功能描述**：返回四 Dashboard、AIChat/报告入口的实际平台资产状态。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/integrations/smartbi/assets`

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| assets | 资产 | Array | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "assets": [
      {
        "assetKey": "student-risk",
        "type": "DASHBOARD",
        "displayName": "学生风险分析",
        "status": "PLATFORM_PENDING",
        "launchMode": "UNVERIFIED"
      },
      {
        "assetKey": "intervention-outcome",
        "type": "DASHBOARD",
        "displayName": "干预成效",
        "status": "PLATFORM_PENDING",
        "launchMode": "UNVERIFIED"
      }
    ]
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```

> 注意：真实 resourceUrl/iframe 只有比赛租户验证后配置，不得造假。
##### 2.5.5.7 SmartBI 资产详情
**功能描述**：根据 assetKey 返回验证后的 SmartBI URL/launchMode。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/integrations/smartbi/assets/{assetKey}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| assetKey | 资产Key | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| status | 平台状态 | Enum | 必返 |
| launchMode | 打开模式 | Enum | UNVERIFIED/NEW_TAB/IFRAME |
| resourceUrl | 资源URL | String/null | 可空 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "assetKey": "student-risk",
    "displayName": "学生风险分析",
    "status": "VERIFIED",
    "launchMode": "NEW_TAB",
    "resourceUrl": "https://tiaozhanbei.cloud.smartbi.com.cn/..."
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
#### 2.5.6 演示支撑
##### 2.5.6.1 创建 Demo Run
**功能描述**：创建一轮可重复 Golden Demo；不重建 baseline。仅 DEMO profile 可用。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/demo/runs`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| demoCaseId | 案例ID | String | 固定DEMO-GRAPH-001 |
| baselineVersion | 基线版本 | String | 必填 |

请求示例：
```json
{
  "demoCaseId": "DEMO-GRAPH-001",
  "baselineVersion": "baseline-ds-v1"
}
```

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| demoRunId | 运行ID | String | 必返 |
| studentId | 主演示学生 | String | 必返 |
| status | 状态 | Enum | ACTIVE |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "demoRunId": "demo-run-001",
    "studentId": "stu-xiaoming",
    "status": "ACTIVE"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.6.2 查询 Demo Run
**功能描述**：查询当前演示阶段和关键实体引用。  
**接口方式**：HTTP GET  
**接口地址**：`/api/v1/demo/runs/{demoRunId}`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| demoRunId | 运行ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| stage | 当前阶段 | Enum | 必返 |
| refs | 实体引用 | Object | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "demoRunId": "demo-run-001",
    "stage": "STUDENT_INITIAL_DIAGNOSTIC",
    "refs": {
      "coachSessionId": "coach-001"
    }
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
##### 2.5.6.3 重置 Demo Run
**功能描述**：关闭旧 run 并创建新 active run；baseline 不删除，旧 run 不计入当前聚合。  
**接口方式**：HTTP POST  
**接口地址**：`/api/v1/demo/runs/{demoRunId}/reset`

请求字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| demoRunId | 旧运行ID | Path | 必填 |

返回字段：

| 参数名 | 中文描述 | 类型 | 约束 |
|---|---|---|---|
| newDemoRunId | 新运行ID | String | 必返 |
| baselineVersion | 基线 | String | 必返 |

返回示例：
```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01JXYZ",
  "data": {
    "newDemoRunId": "demo-run-002",
    "baselineVersion": "baseline-ds-v1"
  },
  "timestamp": "2026-08-29T08:00:00Z"
}
```
### 2.6 返回码说明

| 序号 | 返回码 | HTTP | 说明 |
|---:|---|---:|---|
| 1 | `OK` | 200 | 请求成功 |
| 2 | `VALIDATION_ERROR` | 400 | 参数校验失败 |
| 3 | `AUTH_REQUIRED` | 401 | 未登录或Token失效 |
| 4 | `FORBIDDEN` | 403 | 权限不足 |
| 5 | `RESOURCE_NOT_FOUND` | 404 | 资源不存在 |
| 6 | `STATE_CONFLICT` | 409 | 业务状态冲突 |
| 7 | `PRECONDITION_FAILED` | 412 | If-Match/version不一致 |
| 8 | `DOMAIN_RULE_VIOLATION` | 422 | 违反业务规则 |
| 9 | `AI_RATE_LIMITED` | 429 | AI上游限流 |
| 10 | `AI_UPSTREAM_ERROR` | 502 | LLM/Embedding上游失败 |
| 11 | `SMARTBI_UNAVAILABLE` | 503 | SmartBI暂不可用 |
| 12 | `RAG_UNAVAILABLE` | 503 | 知识库检索不可用 |
| 13 | `UPSTREAM_TIMEOUT` | 504 | 上游超时 |
