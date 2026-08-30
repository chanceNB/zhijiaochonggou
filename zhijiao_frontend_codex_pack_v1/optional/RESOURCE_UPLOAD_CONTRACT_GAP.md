# Optional — Student/Teacher Resource Upload Contract Extension

这不是当前 Golden Demo blocker。

现状：
正式知识库 CRUD 在 `/api/v1/admin/knowledge/*`。

需求：
学生“学习资料”希望可上传供自己的 AI Coach RAG 使用；
教师“课程资源”希望管理课程知识库。

不能：
直接让 STUDENT/TEACHER token 调 admin API。

## 推荐分开设计

### Student personal resources
新增 student-scoped 资源域：
- ownerStudentId
- courseId
- visibility = PERSONAL
- RAG retrieval scope = course official + current student personal

需要新的 API 契约、权限、索引归属和删除策略。

### Teacher course resources
新增 teacher-scoped course resource endpoint：
- 教师必须有 course ownership/permission
- 复用现有 knowledge ingestion service
- 不复制 indexing 算法

## 在用户明确批准这个后端扩展前

前端只显示契约受限状态，不实现假上传。
