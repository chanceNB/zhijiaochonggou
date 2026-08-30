<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/ai-coach/CoachConversation.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="coach-conversation">
    <div v-if="loading" class="state-text">AI 学习教练正在整理上下文...</div>
    <div v-else-if="messages.length === 0" class="empty-chat">
      <strong>把题目、知识点或不懂的地方发给我。</strong>
      <span>我会结合课程资料、错题记录和当前掌握度给你下一步建议。</span>
    </div>
    <article
      v-for="message in renderedMessages"
      v-else
      :key="message.id"
      class="message-row"
      :class="message.role"
    >
      <div class="avatar">{{ message.role === 'student' ? '李' : 'AI' }}</div>
      <div class="bubble">
        <p v-if="message.role === 'student'">{{ message.content }}</p>
        <template v-else>
          <div
            v-if="!props.realMode && message.answer?.sourceMode === 'course_material'"
            class="linked-list-diagram"
          >
            <span class="node muted">dummy</span>
            <i></i>
            <span class="node">head</span>
            <i></i>
            <span class="node target">x</span>
            <i></i>
            <span class="node">next</span>
            <small>prev.next = cur.next</small>
          </div>
          <div class="markdown-body" v-html="message.html"></div>
        </template>

<style scoped lang="scss">
.coach-conversation {
  display: grid;
  align-content: start;
  gap: 12px;
  min-height: 0;
  padding: 12px;
  overflow: auto;
}
.state-text,
.empty-chat {
  display: grid;
  gap: 8px;
  color: #667085;
}
.workflow-status {
  padding: 10px 12px;
  border: 1px solid #d4e2f7;
  border-radius: 8px;
  color: #344054;
  background: #f8fbff;
}
.empty-chat {
  place-items: center;
  align-content: center;
  min-height: 0;
  margin-block: auto;
  text-align: center;
}
.message-row {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}
.message-row.student {
  grid-template-columns: minmax(0, 1fr) 38px;
}
.message-row.student .avatar {
  grid-column: 2;
  background: #e8f1ff;
}
.message-row.student .bubble {
  grid-column: 1;
  grid-row: 1;
  justify-self: end;
  max-width: 620px;
  background: #dbeafe;
}
.avatar {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 1px solid #c9daf5;
  border-radius: 50%;
  color: #235fd7;
  font-weight: 800;
  background: #f8fbff;
}
.bubble {
  max-width: 760px;
  padding: 12px 14px;
  border: 1px solid #dfe7f3;
  border-radius: 8px;
  background: #ffffff;
}
.bubble p {
  margin: 0;
}
time {
  display: block;
  margin-top: 8px;
  color: #98a2b3;
  font-size: 12px;
}
.answer-actions {
  position: sticky;
  bottom: 0;
  margin-top: auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(128px, 1fr));
  gap: 8px;
  width: 100%;
  padding: 12px 0 4px;
  background: linear-gradient(180deg, rgba(245, 249, 255, 0), #f5f9ff 35%);
}
.answer-actions button {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 36px;
  padding: 0 10px;
  border: 1px solid #adc8f8;
  border-radius: 8px;
  color: #1849a9;
  background: #ffffff;
  cursor: pointer;
  white-space: nowrap;
}
.answer-actions small {
  color: #7c8799;
}
.linked-list-diagram {
  display: grid;
  grid-template-columns: auto 20px auto 20px auto 20px auto;
  gap: 6px;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #c9daf5;
  border-radius: 8px;
  background: #f8fbff;
}
.linked-list-diagram i {
  height: 2px;
  background: #7ba7ef;
}
.node {
  min-width: 44px;
  padding: 5px 8px;
  border: 1px solid #95b8f0;
  border-radius: 7px;
  color: #17428f;
  text-align: center;
  background: #ffffff;
  font-size: 12px;
  font-weight: 800;
}
.node.muted {
  color: #667085;
  background: #f2f4f7;
}
.node.target {
  border-color: #f59e0b;
  color: #92400e;
  background: #fff7ed;
}
.linked-list-diagram small {
  grid-column: 1 / -1;
  color: #475467;
  font-size: 12px;
}
:deep(.markdown-body) {
  color: #1f2a44;
  line-height: 1.58;
}
:deep(.markdown-body ol) {
  margin: 8px 0 14px;
  padding-left: 22px;
}
:deep(.markdown-body pre) {
  overflow: auto;
  padding: 14px;
  border: 1px solid #dfe7f3;
  border-radius: 8px;
  background: #f8fafc;
}
:deep(.markdown-body code) {
  font-family: 'Cascadia Code', Consolas, monospace;
}
@media (max-width: 1180px) {
  .answer-actions {
    grid-template-columns: repeat(3, minmax(0, auto));
  }
}
@media (max-width: 720px) {
  .message-row,
  .message-row.student {
    grid-template-columns: 36px minmax(0, 1fr);
  }
  .message-row.student .avatar,
  .message-row.student .bubble {
    grid-column: auto;
    grid-row: auto;
  }
  .answer-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
