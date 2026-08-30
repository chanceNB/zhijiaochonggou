<template>
  <section class="student-today-page" data-testid="student-today-page">
    <div v-if="store.state === 'LOADING' || store.state === 'INITIAL'" class="today-state today-state--loading" data-testid="today-loading">
      <span class="today-state__spinner" aria-hidden="true"></span>
      <p>正在加载今日学习状态...</p>
    </div>

    <div v-else-if="store.state === 'ERROR' || store.state === 'FORBIDDEN'" class="today-state today-state--error" data-testid="today-error">
      <h1>{{ store.state === 'FORBIDDEN' ? '当前账号没有访问该资源的权限' : '今日学习暂时无法加载' }}</h1>
      <p>{{ store.error ?? '请稍后重试。' }}</p>
      <button type="button" @click="store.load({ force: true })">重新加载</button>
    </div>

    <template v-else-if="store.data">
      <TodayActionCard
        :action="store.data.nextAction"
        :assignment="store.data.teacherAssignment"
        :busy="navigationBusy"
        @start="startAction"
      />
      <div class="today-workspace">
        <main class="today-workspace__main">
          <LearningStatePanel :state="store.data.learningState" />
          <LearningPath />
        </main>
        <aside class="today-workspace__rail" aria-label="今日学习侧栏">
          <TeacherAssignmentCard
            v-if="store.data.teacherAssignment"
            :assignment="store.data.teacherAssignment"
            @start="openAssignment"
          />
          <section v-else class="today-rail-card today-rail-card--coach" data-testid="today-coach-cta">
            <span class="today-rail-card__icon" aria-hidden="true">AI</span>
            <h2>需要一起梳理吗？</h2>
            <p>AI 学习教练会基于当前知识点开始一次真实会话。</p>
            <button type="button" @click="openCoach">打开 AI 学习教练</button>
          </section>
          <section class="today-rail-card" data-testid="today-context-card">
            <h2>学习上下文</h2>
            <dl>
              <div><dt>知识点</dt><dd>{{ store.data.learningState.knowledgePointId }}</dd></div>
              <div><dt>证据数</dt><dd>{{ store.data.learningState.evidenceCount }}</dd></div>
            </dl>
          </section>
        </aside>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import TodayActionCard from '@/components/student/today/TodayActionCard.vue'
import LearningStatePanel from '@/components/student/today/LearningStatePanel.vue'
import LearningPath from '@/components/student/today/LearningPath.vue'
import TeacherAssignmentCard from '@/components/student/today/TeacherAssignmentCard.vue'
import { useStudentContextStore } from '@/stores/studentContextStore'

const store = useStudentContextStore()
const router = useRouter()
const navigationBusy = ref(false)

onMounted(() => {
  void store.load()
})

const openCoach = async () => {
  navigationBusy.value = true
  await router.push('/student/ai-coach')
}

const openAssignment = async () => {
  if (!store.data?.teacherAssignment) return
  navigationBusy.value = true
  await router.push(`/student/practice/${encodeURIComponent(store.data.teacherAssignment.practiceSetId)}`)
}

const startAction = () => (store.data?.teacherAssignment ? openAssignment() : openCoach())
</script>

<style scoped>
.student-today-page { display: grid; gap: 16px; min-width: 0; color: var(--color-text); }
.today-state { display: grid; min-height: 380px; place-items: center; align-content: center; gap: 12px; padding: 24px; border: 1px solid var(--color-border); border-radius: 10px; background: #fff; box-shadow: var(--shadow-card); text-align: center; }
.today-state p, .today-state h1 { margin: 0; }
.today-state p { color: var(--color-secondary); font-size: 14px; }
.today-state h1 { color: var(--color-text); font-size: 20px; }
.today-state button { min-height: 38px; padding: 0 16px; border: 1px solid var(--color-student-primary); border-radius: 8px; color: #fff; background: var(--color-student-primary); cursor: pointer; font-size: 13px; font-weight: 700; }
.today-state__spinner { width: 24px; height: 24px; border: 3px solid #dbe8ff; border-top-color: var(--color-student-primary); border-radius: 50%; animation: spin 800ms linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.today-workspace { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, 326px); gap: 16px; align-items: start; }
.today-workspace__main, .today-workspace__rail { display: grid; gap: 16px; min-width: 0; }
.today-rail-card { display: grid; gap: 12px; padding: 18px; border: 1px solid var(--color-border); border-radius: 10px; background: #fff; box-shadow: var(--shadow-card); }
.today-rail-card h2, .today-rail-card p { margin: 0; }
.today-rail-card h2 { color: var(--color-text); font-size: 16px; }
.today-rail-card p { color: var(--color-secondary); font-size: 13px; line-height: 1.6; }
.today-rail-card button { min-height: 36px; border: 1px solid #b8cdfc; border-radius: 8px; color: #245fc9; background: #f4f7ff; cursor: pointer; font-size: 13px; font-weight: 700; }
.today-rail-card--coach { border-color: #b8ead8; background: #f8fffb; }
.today-rail-card__icon { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 8px; color: #fff; background: var(--color-ai); font-size: 12px; font-weight: 800; }
.today-rail-card dl { display: grid; gap: 10px; margin: 0; padding-top: 10px; border-top: 1px solid #edf0f5; }
.today-rail-card dl div { display: flex; justify-content: space-between; gap: 12px; }
.today-rail-card dt { color: var(--color-secondary); font-size: 12px; }
.today-rail-card dd { max-width: 180px; margin: 0; overflow-wrap: anywhere; color: var(--color-text); font-size: 12px; font-weight: 700; text-align: right; }
@media (max-width: 980px) { .today-workspace { grid-template-columns: 1fr; } }
</style>
