<template>
  <aside class="coach-session-rail" data-testid="coach-session-rail" aria-label="AI 教练会话">
    <header>
      <div>
        <p>AI 学习教练</p>
        <h2>会话</h2>
      </div>
      <button type="button" aria-label="开始新会话" @click="emit('create')">+</button>
    </header>
    <div v-if="loading" class="coach-session-rail__empty">正在连接...</div>
    <div v-else-if="!sessions.length" class="coach-session-rail__empty">当前浏览器暂无历史会话</div>
    <nav v-else class="coach-session-rail__list">
      <button v-for="session in sessions" :key="session.sessionId" type="button" :class="{ 'is-active': session.sessionId === activeSessionId }" @click="emit('select', session.sessionId)">
        <span class="coach-session-rail__dot" aria-hidden="true"></span>
        <span><strong>学习会话</strong><small>{{ session.mode === 'DIAGNOSTIC' ? '诊断模式' : '辅导模式' }}</small></span>
      </button>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import type { CoachSessionVm } from '@/types/contracts/student'

defineProps<{ sessions: CoachSessionVm[]; activeSessionId: string | null; loading: boolean }>()
const emit = defineEmits<{ select: [sessionId: string]; create: [] }>()
</script>

<style scoped>
.coach-session-rail { display: grid; align-content: start; gap: 14px; min-height: 0; padding: 14px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }
.coach-session-rail header { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid #edf0f5; }
.coach-session-rail p, .coach-session-rail h2 { margin: 0; }
.coach-session-rail p { color: var(--color-secondary); font-size: 11px; }
.coach-session-rail h2 { margin-top: 4px; color: var(--color-text); font-size: 18px; }
.coach-session-rail header button { display: grid; width: 30px; height: 30px; place-items: center; border: 1px solid #b8cdfc; border-radius: 7px; color: #245fc9; background: #f4f7ff; cursor: pointer; font-size: 20px; line-height: 1; }
.coach-session-rail__empty { padding: 18px 4px; color: var(--color-secondary); font-size: 12px; line-height: 1.6; }
.coach-session-rail__list { display: grid; gap: 8px; }
.coach-session-rail__list button { display: grid; grid-template-columns: 9px minmax(0, 1fr); gap: 9px; align-items: start; min-width: 0; padding: 11px; border: 1px solid #e0e7f1; border-radius: 8px; color: var(--color-text); background: #fff; cursor: pointer; text-align: left; }
.coach-session-rail__list button.is-active { border-color: #8db0ee; background: #f1f6ff; box-shadow: inset 3px 0 #2563eb; }
.coach-session-rail__dot { width: 8px; height: 8px; margin-top: 4px; border-radius: 50%; background: var(--color-student-primary); }
.coach-session-rail__list strong, .coach-session-rail__list small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.coach-session-rail__list strong { font-size: 13px; }
.coach-session-rail__list small { margin-top: 5px; color: var(--color-secondary); font-size: 11px; }
</style>
