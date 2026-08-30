<template>
  <section class="student-ai-coach-page" data-testid="student-ai-coach-page">
    <div v-if="coach.error" class="coach-alert" :class="{ 'is-degraded': coach.state === 'DEGRADED' }" data-testid="coach-alert" role="status">
      <span>{{ coach.error }}</span>
      <button type="button" @click="retry">重试</button>
    </div>

    <section class="coach-brief" data-testid="coach-context-brief">
      <article>
        <span class="coach-brief__icon" aria-hidden="true">AI</span>
        <small>当前学习状态</small>
        <strong>{{ context ? formatPercent(context.mastery) : '—' }}</strong>
        <p>掌握度</p>
      </article>
      <article>
        <span class="coach-brief__icon" aria-hidden="true">!</span>
        <small>学习信号</small>
        <strong>{{ context ? formatPercent(context.confidence) : '—' }}</strong>
        <p>置信度</p>
      </article>
      <article>
        <span class="coach-brief__icon" aria-hidden="true">→</span>
        <small>下一步</small>
        <strong>两题诊断</strong>
        <p>结果按真实作答更新</p>
      </article>
    </section>

    <div class="coach-shell" data-testid="coach-shell">
      <CoachSessionRail
        :sessions="coach.sessions"
        :active-session-id="coach.activeSessionId"
        :loading="coach.state === 'LOADING'"
        @select="selectSession"
        @create="createSession"
      />

      <main class="coach-conversation-column">
        <CoachConversation :messages="coach.messages" :sending="coach.sending" />
        <DiagnosticStartCard
          v-if="showDiagnostic"
          :disabled="!coach.canStartDiagnostic"
          :loading="coach.diagnosticState === 'SUBMITTING'"
          :error="coach.diagnosticError"
          @start="startDiagnostic"
        />
        <CoachComposer ref="composerRef" :sending="coach.sending" @submit="sendMessage" />
      </main>

      <LearningContextPanel
        :context="context"
        :rag-status="latestRagStatus"
        :citations="latestCitations"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CoachComposer from '@/components/student/coach/CoachComposer.vue'
import CoachConversation from '@/components/student/coach/CoachConversation.vue'
import CoachSessionRail from '@/components/student/coach/CoachSessionRail.vue'
import DiagnosticStartCard from '@/components/student/coach/DiagnosticStartCard.vue'
import LearningContextPanel from '@/components/student/coach/LearningContextPanel.vue'
import { useCoachStore } from '@/stores/coachStore'
import { useStudentContextStore } from '@/stores/studentContextStore'

const route = useRoute()
const router = useRouter()
const coach = useCoachStore()
const studentContext = useStudentContextStore()
const composerRef = ref<{ clear: () => void } | null>(null)

const context = computed(() => coach.context)
const latestCitations = computed(() => coach.lastMessage?.citations ?? [])
const latestRagStatus = computed(() => coach.lastMessage?.ragStatus ?? coach.session?.ragStatus ?? 'EMPTY')
const showDiagnostic = computed(() => coach.canStartDiagnostic && (coach.messages.length === 0 || coach.lastActions.some((action) => action.type === 'START_DIAGNOSTIC')))
const formatPercent = (value: number) => `${Math.round(value * 100)}%`

onMounted(async () => {
  await studentContext.load()
  const sessionId = typeof route.params.sessionId === 'string' ? route.params.sessionId : null
  if (sessionId) {
    await coach.restoreSession(sessionId)
    return
  }
  if (!coach.activeSessionId) {
    await coach.createSession({
      studentId: studentContext.studentId,
      courseId: studentContext.courseId,
      knowledgePointId: studentContext.knowledgePointId,
      mode: 'TUTOR',
    })
  }
})

const retry = async () => {
  if (typeof route.params.sessionId === 'string') {
    await coach.restoreSession(route.params.sessionId)
  } else {
    await coach.createSession({
      studentId: studentContext.studentId,
      courseId: studentContext.courseId,
      knowledgePointId: studentContext.knowledgePointId,
      mode: 'TUTOR',
    })
  }
}

const createSession = async () => {
  const session = await coach.createSession({
    studentId: studentContext.studentId,
    courseId: studentContext.courseId,
    knowledgePointId: studentContext.knowledgePointId,
    mode: 'TUTOR',
  })
  if (session) await router.push(`/student/ai-coach/${encodeURIComponent(session.sessionId)}`)
}

const selectSession = async (sessionId: string) => {
  coach.selectSession(sessionId)
  await router.push(`/student/ai-coach/${encodeURIComponent(sessionId)}`)
}

const sendMessage = async (message: string) => {
  const sent = await coach.sendMessage(message)
  if (sent) composerRef.value?.clear()
}

const startDiagnostic = async () => {
  const practiceSetId = await coach.startDiagnostic()
  if (practiceSetId) await router.push(`/student/practice/${encodeURIComponent(practiceSetId)}`)
}
</script>

<style scoped>
.student-ai-coach-page { display: grid; grid-template-rows: auto minmax(0, 1fr); gap: 14px; min-width: 0; min-height: calc(100dvh - 118px); color: var(--color-text); }
.coach-alert { display: flex; align-items: center; justify-content: space-between; gap: 12px; min-height: 38px; padding: 8px 12px; border: 1px solid #f0d7aa; border-radius: 8px; color: #925b12; background: #fffaf2; font-size: 13px; }
.coach-alert button { flex: 0 0 auto; min-height: 28px; padding: 0 10px; border: 1px solid #deb977; border-radius: 6px; color: #925b12; background: #fff; cursor: pointer; font-size: 12px; }
.coach-alert.is-degraded { border-color: #f2d6d6; color: #a43c38; background: #fffafa; }
.coach-brief { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.coach-brief article { display: grid; grid-template-columns: 38px minmax(0, 1fr); gap: 3px 12px; align-items: center; min-width: 0; min-height: 86px; padding: 13px 16px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }
.coach-brief__icon { display: grid; grid-row: 1 / span 3; width: 38px; height: 38px; place-items: center; border-radius: 8px; color: #fff; background: var(--color-ai); font-size: 12px; font-weight: 800; }
.coach-brief article:nth-child(2) .coach-brief__icon { background: var(--color-warning); }
.coach-brief article:nth-child(3) .coach-brief__icon { background: #7554d8; }
.coach-brief small, .coach-brief p { margin: 0; color: var(--color-secondary); font-size: 11px; }
.coach-brief strong { overflow: hidden; color: var(--color-text); font-size: 18px; text-overflow: ellipsis; white-space: nowrap; }
.coach-shell { display: grid; grid-template-columns: 268px minmax(0, 1fr) 336px; gap: 12px; min-width: 0; min-height: 0; overflow: hidden; }
.coach-conversation-column { display: grid; grid-template-rows: minmax(0, 1fr) auto auto; gap: 9px; min-width: 0; min-height: 0; padding: 0 9px 9px; overflow: hidden; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }
.coach-conversation-column > :first-child { min-height: 0; overflow: auto; }
@media (max-width: 1250px) { .coach-shell { grid-template-columns: 230px minmax(0, 1fr) 290px; } }
@media (max-width: 1020px) { .student-ai-coach-page { min-height: 0; } .coach-shell { grid-template-columns: 232px minmax(0, 1fr); overflow: visible; } .learning-context-panel { grid-column: 1 / -1; } }
@media (max-width: 760px) { .coach-brief { grid-template-columns: 1fr; } .coach-shell { grid-template-columns: 1fr; } .coach-session-rail { max-height: 245px; } .coach-conversation-column { min-height: 620px; } }
</style>
