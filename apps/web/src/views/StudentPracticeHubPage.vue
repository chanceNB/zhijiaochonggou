<template>
  <section class="practice-page" data-testid="student-practice-hub">
    <header class="practice-hero">
      <div>
        <p class="eyebrow">精准定位薄弱点，针对性练习</p>
        <h1>定向刷题</h1>
        <p>从今日学习、教师任务和 AI 教练会话进入真实练习。</p>
      </div>
      <div class="practice-hero__meta">
        <span class="meta-label">学习空间</span>
        <strong>{{ context.studentId }}</strong>
        <span>课程 {{ contextStore.courseId }}</span>
      </div>
    </header>

    <div v-if="loading" class="state-card" data-testid="practice-hub-loading">正在准备练习空间...</div>
    <div v-else-if="error" class="state-card state-card--error" data-testid="practice-hub-error">
      <h2>练习空间暂时不可用</h2>
      <p>{{ error }}</p>
      <button type="button" @click="load">重新加载</button>
    </div>
    <template v-else>
      <section class="flow-strip" aria-label="练习流程">
        <article class="flow-step flow-step--done"><span>01</span><div><strong>发现薄弱点</strong><small>来自学习证据</small></div></article>
        <i aria-hidden="true">›</i>
        <article class="flow-step flow-step--active"><span>02</span><div><strong>定向练习</strong><small>完成当前题组</small></div></article>
        <i aria-hidden="true">›</i>
        <article class="flow-step"><span>03</span><div><strong>迁移验证</strong><small>由真实结果解锁</small></div></article>
      </section>

      <section class="practice-grid" aria-label="进入练习">
        <article class="entry-card entry-card--primary">
          <div class="entry-card__icon">今</div>
          <div class="entry-card__body">
            <span class="eyebrow">今日下一步</span>
            <h2>{{ context.nextAction.title }}</h2>
            <p>知识点 {{ context.nextAction.knowledgePointId }} · 预计 {{ context.nextAction.estimatedMinutes }} 分钟</p>
          </div>
          <button type="button" :disabled="!assignmentPracticeSetId" @click="openPractice(assignmentPracticeSetId!)">开始练习</button>
        </article>

        <article class="entry-card">
          <div class="entry-card__icon entry-card__icon--coach">AI</div>
          <div class="entry-card__body">
            <span class="eyebrow">AI 教练会话</span>
            <h2>{{ coachPracticeSetId ? '继续诊断练习' : '从 AI 教练开始' }}</h2>
            <p>{{ coachPracticeSetId ? '当前会话已有真实练习题组。' : '完成会话中的诊断后，这里会出现练习题组。' }}</p>
          </div>
          <button type="button" :disabled="!coachPracticeSetId" @click="openPractice(coachPracticeSetId!)">继续练习</button>
        </article>

        <article class="entry-card entry-card--wrong">
          <div class="entry-card__icon entry-card__icon--wrong">错</div>
          <div class="entry-card__body">
            <span class="eyebrow">错题本</span>
            <h2>{{ wrongBookTotal ? `${wrongBookTotal} 道待复习` : '暂无待复习错题' }}</h2>
            <p>复习真实答题记录，巩固仍需理解的知识点。</p>
          </div>
          <button type="button" @click="router.push('/student/wrong-book')">打开错题本</button>
        </article>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coachStore'
import { useStudentContextStore } from '@/stores/studentContextStore'
import { useWrongBookStore } from '@/stores/wrongBookStore'

const router = useRouter()
const contextStore = useStudentContextStore()
const wrongBook = useWrongBookStore()
const coach = useCoachStore()
const loading = ref(true)
const error = ref<string | null>(null)
const context = computed(() => contextStore.data ?? {
  studentId: contextStore.studentId,
  courseId: contextStore.courseId,
  nextAction: { title: '查看当前学习任务', knowledgePointId: '当前知识点', estimatedMinutes: 0, type: 'NONE' },
})
const assignmentPracticeSetId = computed(() => contextStore.data?.teacherAssignment?.practiceSetId ?? null)
const coachPracticeSetId = computed(() => coach.currentPracticeSetId)
const wrongBookTotal = computed(() => wrongBook.data?.total ?? 0)

async function load() {
  loading.value = true
  error.value = null
  const today = await contextStore.load()
  await wrongBook.load()
  if (!today && contextStore.error) error.value = contextStore.error
  loading.value = false
}
function openPractice(id: string) {
  void router.push(`/student/practice/${encodeURIComponent(id)}`)
}
onMounted(() => void load())
</script>

<style scoped>
.practice-page { display: grid; gap: 16px; width: min(1320px, 100%); margin: 0 auto; padding: 24px 28px 34px; color: var(--color-text); }
.practice-hero { display: flex; justify-content: space-between; gap: 24px; align-items: end; padding: 4px 2px 12px; }
.practice-hero h1 { margin: 5px 0 4px; color: #14264f; font-size: 28px; }
.practice-hero p { margin: 0; color: var(--color-secondary); font-size: 14px; }
.eyebrow { margin: 0; color: #2f6de9 !important; font-size: 12px !important; font-weight: 800; }
.practice-hero__meta { display: grid; gap: 3px; min-width: 210px; padding: 12px 15px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; text-align: right; }
.practice-hero__meta strong { color: #1d3c70; font-size: 14px; }
.practice-hero__meta span:last-child { color: var(--color-secondary); font-size: 12px; }
.meta-label { color: #2f6de9; font-size: 11px; font-weight: 800; }
.state-card { display: grid; min-height: 260px; place-items: center; align-content: center; gap: 10px; padding: 30px; border: 1px solid var(--color-border); border-radius: 9px; background: #fff; box-shadow: var(--shadow-card); text-align: center; }
.state-card h2, .state-card p { margin: 0; }.state-card p { color: var(--color-secondary); }.state-card button { min-height: 36px; padding: 0 15px; border: 0; border-radius: 7px; color: #fff; background: #2f6de9; cursor: pointer; }.state-card--error { color: #a43c38; }
.flow-strip { display: grid; grid-template-columns: 1fr auto 1fr auto 1fr; gap: 12px; align-items: center; }
.flow-strip > i { color: #a7b7cd; font-size: 28px; font-style: normal; }
.flow-step { display: flex; gap: 10px; align-items: center; min-height: 68px; padding: 11px 14px; border: 1px solid #dce5f1; border-radius: 9px; background: #fff; }.flow-step > span { display: grid; width: 26px; height: 26px; place-items: center; border-radius: 50%; color: #fff; background: #9aabc3; font-size: 11px; font-weight: 800; }.flow-step--done > span { background: #48a965; }.flow-step--active { border-color: #a9c8ff; background: #f6f9ff; }.flow-step--active > span { background: #e8991e; }.flow-step strong { display: block; color: #1d335e; font-size: 14px; }.flow-step small { display: block; margin-top: 3px; color: #72819a; font-size: 11px; }
.practice-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }.entry-card { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: 12px; align-content: start; min-width: 0; min-height: 208px; padding: 18px; border: 1px solid #dce5f1; border-radius: 9px; background: #fff; box-shadow: var(--shadow-card); }.entry-card__icon { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 8px; color: #fff; background: #2f6de9; font-size: 14px; font-weight: 800; }.entry-card__icon--coach { background: #0fa779; }.entry-card__icon--wrong { background: #e58a28; }.entry-card__body { min-width: 0; }.entry-card h2 { margin: 5px 0 6px; color: #17345f; font-size: 17px; }.entry-card p { margin: 0; color: var(--color-secondary); font-size: 12px; line-height: 1.6; }.entry-card button { grid-column: 1 / -1; align-self: end; min-height: 36px; border: 1px solid #8eb3f4; border-radius: 7px; color: #2563eb; background: #fff; cursor: pointer; font-size: 13px; font-weight: 700; }.entry-card button:hover:not(:disabled) { background: #f4f8ff; }.entry-card button:disabled { cursor: not-allowed; opacity: .48; }.entry-card--primary { border-color: #c4d8ff; background: #fbfdff; }.entry-card--wrong { border-color: #eed8b6; }
@media (max-width: 900px) { .practice-grid { grid-template-columns: 1fr; }.entry-card { min-height: 150px; }.practice-hero { align-items: start; flex-direction: column; }.practice-hero__meta { text-align: left; }.flow-strip { grid-template-columns: 1fr; }.flow-strip > i { display: none; } }
@media (max-width: 600px) { .practice-page { padding: 16px 12px 26px; }.practice-hero h1 { font-size: 24px; } }
</style>
