<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/ai-coach/CoachSessionList.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <aside class="coach-session-list">
    <header class="session-header">
      <h2>对话列表</h2>
      <button class="icon-button" type="button" aria-label="新建对话" @click="$emit('create')">
        <el-icon><Plus /></el-icon>
        <span class="icon-button__label">新建对话</span>
      </button>
    </header>

    <div v-if="loading" class="state-text">正在加载对话...</div>
    <div v-else-if="sessions.length === 0" class="state-text">还没有对话，先问 AI 一个问题吧。</div>
    <button
      v-for="session in sessions"
      v-else
      :key="session.id"
      class="session-card"
      :class="{ active: session.id === activeSessionId }"
      type="button"
      @click="$emit('select', session.id)"
    >
      <span class="session-title">{{ session.title }}</span>
      <span class="session-time">{{ formatCompactDateTime(session.updatedAt) }}</span>
      <small>{{ session.preview }}</small>
    </button>
    <a class="view-all" href="#all-sessions">查看全部对话</a>
  </aside>
</template>

<style scoped lang="scss">
.coach-session-list {
  display: grid;
  align-content: start;
  gap: 12px;
  height: 100%;
  padding: 16px;
  border: 1px solid #d9e5f7;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.86);
}
.session-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}
.session-card {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: center;
}
h2 {
  margin: 0;
  color: #10224f;
  font-size: 18px;
}
.icon-button {
  display: inline-flex;
  flex: 0 0 auto;
  width: auto;
  min-width: 88px;
  height: 36px;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 12px;
  border: 1px solid #adc8f8;
  border-radius: 8px;
  color: #235fd7;
  background: #ffffff;
  cursor: pointer;
  white-space: nowrap;
}
.icon-button__label {
  line-height: 1;
}
.session-card {
  width: 100%;
  min-height: 78px;
  padding: 12px;
  border: 1px solid #e0e7f2;
  border-radius: 8px;
  color: #526075;
  text-align: left;
  background: #ffffff;
  cursor: pointer;
}
.session-card.active {
  border-color: #78a8ff;
  background: #eef5ff;
}
.session-title {
  overflow: hidden;
  color: #16244a;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 700;
}
.session-time,
.state-text,
.session-card small {
  color: #7c8799;
}
.session-card small {
  grid-column: 1 / -1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.view-all {
  margin-top: auto;
  color: #2563eb;
  text-align: center;
  text-decoration: none;
}
</style>
