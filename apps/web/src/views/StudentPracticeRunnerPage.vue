<template>
  <section class="runner-page" data-testid="student-practice-runner">
    <div v-if="practice.state === 'LOADING' || practice.state === 'INITIAL'" class="state-card">正在加载练习题...</div>
    <div v-else-if="practice.state === 'ERROR' || practice.state === 'FORBIDDEN'" class="state-card state-card--error">
      <h1>练习暂时无法打开</h1><p>{{ practice.error }}</p><button type="button" @click="load">重新加载</button>
    </div>
    <div v-else-if="!practice.data || !practice.currentQuestion" class="state-card"><h1>暂无可作答题目</h1><p>当前练习组没有可用题目。</p></div>
    <template v-else>
      <header class="runner-header">
        <div><span class="eyebrow">{{ sourceLabel }}</span><h1>定向练习</h1><p>{{ knowledgePointLabel }}</p></div>
        <div class="runner-progress"><strong>{{ practice.answeredCount }}/{{ practice.data.questions.length }}</strong><span>已完成</span><div><i :style="{ width: `${progress}%` }" /></div></div>
      </header>
      <section class="question-layout">
        <aside class="question-rail">
          <h2>题目导航</h2>
          <button v-for="question in practice.data.questions" :key="question.questionId" type="button" :class="{ active: question.index === practice.activeIndex, answered: Boolean(practice.feedbackByQuestion[question.questionId]) }" @click="practice.setActiveIndex(question.index)">
            <span>{{ question.index + 1 }}</span><em>{{ practice.feedbackByQuestion[question.questionId] ? '已答' : '待答' }}</em>
          </button>
          <p>每道题提交后会保留真实答题记录。</p>
        </aside>
        <main class="question-card">
          <div class="question-card__top"><span>第 {{ practice.currentQuestion.index + 1 }} 题</span><span>{{ typeLabel }}</span></div>
          <h2>{{ practice.currentQuestion.stem }}</h2>
          <div class="option-list">
            <button v-for="option in practice.currentQuestion.options" :key="option.optionId" type="button" :disabled="isAnswered || submitting" :class="{ selected: selectedAnswer === option.optionId, correct: feedback?.correct && feedback.correctAnswer === option.optionId, wrong: feedback && !feedback.correct && selectedAnswer === option.optionId }" @click="selectedAnswer = option.optionId">
              <b>{{ option.optionId }}</b><span>{{ option.text }}</span>
            </button>
          </div>
          <p v-if="practice.error" class="inline-error">{{ practice.error }}</p>
          <section v-if="feedback" class="feedback" :class="{ 'feedback--correct': feedback.correct }" data-testid="practice-feedback">
            <div class="feedback__title"><strong>{{ feedback.correct ? '回答正确' : '需要再想一步' }}</strong><span v-if="feedback.misconceptionCode">{{ misconceptionLabel }}</span></div>
            <p>{{ feedback.explanation }}</p>
          </section>
          <footer class="question-actions">
            <button v-if="feedback?.canAddWrongBook" type="button" class="secondary-action" @click="addWrongBook">加入错题本</button>
            <button v-if="feedback?.canGenerateSimilar" type="button" class="secondary-action" :disabled="similarBusy" @click="generateSimilar">生成类似题</button>
            <button v-if="feedback" type="button" class="secondary-action" @click="openCoach">和 AI 教练讨论</button>
            <span class="action-spacer" />
            <button v-if="!feedback" type="button" class="primary-action" :disabled="!selectedAnswer || submitting" @click="submit">提交答案</button>
            <button v-else-if="!isLast" type="button" class="primary-action" @click="next">下一题</button>
            <button v-else type="button" class="primary-action" :disabled="practice.completing" @click="finish">查看结果</button>
          </footer>
        </main>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coachStore'
import { usePracticeStore } from '@/stores/practiceStore'
import { displayKnowledgePoint, displayMisconception } from '@/adapters/student/presentation'

const route = useRoute(); const router = useRouter(); const practice = usePracticeStore(); const coach = useCoachStore()
const selectedAnswer = ref(''); const submitting = ref(false); const similarBusy = ref(false)
const practiceSetId = computed(() => String(route.params.practiceSetId))
const feedback = computed(() => practice.currentQuestion ? practice.feedbackByQuestion[practice.currentQuestion.questionId] : null)
const isAnswered = computed(() => Boolean(feedback.value)); const isLast = computed(() => practice.data ? practice.activeIndex >= practice.data.questions.length - 1 : false)
const progress = computed(() => practice.data?.questions.length ? Math.round((practice.answeredCount / practice.data.questions.length) * 100) : 0)
const submittingQuestion = computed(() => practice.submittingQuestionId === practice.currentQuestion?.questionId)
const sourceLabel = computed(() => practice.data?.source === 'AI_COACH_SIMILAR' ? 'AI 教练 · 类似题' : practice.data?.source === 'TEACHER_ASSIGNMENT' ? '教师任务' : '诊断练习')
const knowledgePointLabel = computed(() => displayKnowledgePoint(practice.currentQuestion?.knowledgePointId, practice.currentQuestion?.knowledgePointName))
const misconceptionLabel = computed(() => displayMisconception(feedback.value?.misconceptionCode))
const typeLabel = computed(() => practice.currentQuestion?.questionType === 'SINGLE_CHOICE' ? '单选题' : practice.currentQuestion?.questionType ?? '练习题')
async function load() { await practice.load(practiceSetId.value, true); selectedAnswer.value = feedback.value?.selectedAnswer ?? '' }
async function submit() { if (!selectedAnswer.value) return; submitting.value = true; await practice.submitCurrent(selectedAnswer.value, 12); submitting.value = false }
function next() { practice.setActiveIndex(practice.activeIndex + 1); selectedAnswer.value = feedback.value?.selectedAnswer ?? '' }
async function finish() { const outcome = await practice.complete(); if (outcome) await router.push(`/student/practice/${encodeURIComponent(practiceSetId.value)}/result`) }
async function addWrongBook() { if (feedback.value) await practice.addWrongBook(feedback.value.attemptId) }
async function generateSimilar() { if (!feedback.value || !coach.activeSessionId) return; similarBusy.value = true; const id = await coach.generateSimilar(feedback.value.attemptId); similarBusy.value = false; if (id) await router.push(`/student/practice/${encodeURIComponent(id)}`) }
function openCoach() {
  if (feedback.value && practice.currentQuestion) {
    coach.setPracticeContext({ practiceSetId: practiceSetId.value, questionId: practice.currentQuestion.questionId, attemptId: feedback.value.attemptId, selectedAnswer: feedback.value.selectedAnswer })
  }
  void router.push('/student/ai-coach')
}
watch(() => practice.currentQuestion?.questionId, () => { selectedAnswer.value = feedback.value?.selectedAnswer ?? '' })
onMounted(() => void load())
</script>

<style scoped>
.runner-page { min-height: 100%; padding: 24px 30px 32px; color: var(--color-text); }.state-card { display: grid; min-height: 360px; place-items: center; align-content: center; gap: 10px; padding: 28px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; text-align: center; }.state-card h1,.state-card p { margin: 0; }.state-card p { color: var(--color-secondary); }.state-card button { min-height: 36px; padding: 0 15px; border: 0; border-radius: 7px; color: #fff; background: #2f6de9; cursor: pointer; }.state-card--error { color: #a43c38; }.runner-header { display: flex; justify-content: space-between; gap: 24px; align-items: end; max-width: 1180px; margin: 0 auto 18px; }.eyebrow { margin: 0; color: #2f6de9; font-size: 12px; font-weight: 800; }.runner-header h1 { margin: 5px 0 4px; color: #14264f; font-size: 26px; }.runner-header p { margin: 0; color: var(--color-secondary); font-size: 13px; }.runner-progress { display: grid; grid-template-columns: auto auto; gap: 3px 8px; min-width: 180px; }.runner-progress strong { color: #1d3c70; font-size: 17px; }.runner-progress span { align-self: end; color: var(--color-secondary); font-size: 12px; }.runner-progress div { grid-column: 1 / -1; height: 7px; margin-top: 5px; overflow: hidden; border-radius: 999px; background: #e7edf6; }.runner-progress i { display: block; height: 100%; border-radius: inherit; background: #2f6de9; transition: width 180ms ease; }.question-layout { display: grid; grid-template-columns: 220px minmax(0, 800px); gap: 14px; max-width: 1180px; margin: 0 auto; align-items: start; }.question-rail { display: grid; gap: 8px; padding: 15px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; }.question-rail h2 { margin: 0 0 5px; color: #1d335e; font-size: 15px; }.question-rail button { display: flex; justify-content: space-between; gap: 8px; min-height: 38px; align-items: center; padding: 0 10px; border: 1px solid transparent; border-radius: 6px; color: #4d6384; background: #f8faff; cursor: pointer; font-size: 12px; }.question-rail button span { font-weight: 800; }.question-rail button em { color: #94a3b8; font-size: 11px; font-style: normal; }.question-rail button.active { border-color: #a9c8ff; color: #235eca; background: #eef4ff; }.question-rail button.answered em { color: #2c9a63; }.question-rail p { margin: 8px 0 0; color: var(--color-secondary); font-size: 11px; line-height: 1.6; }.question-card { display: grid; gap: 18px; padding: 24px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }.question-card__top { display: flex; justify-content: space-between; color: #68809e; font-size: 12px; }.question-card h2 { margin: 0; color: #142b58; font-size: 21px; line-height: 1.55; }.option-list { display: grid; gap: 10px; }.option-list button { display: grid; grid-template-columns: 34px minmax(0, 1fr); gap: 11px; align-items: center; min-height: 54px; padding: 0 14px; border: 1px solid #d8e2ef; border-radius: 8px; color: #365174; background: #fff; cursor: pointer; text-align: left; }.option-list button:hover:not(:disabled), .option-list button.selected { border-color: #87abf3; background: #f4f8ff; }.option-list button b { display: grid; width: 28px; height: 28px; place-items: center; border-radius: 50%; color: #557093; background: #edf2f9; font-size: 12px; }.option-list button.selected b { color: #fff; background: #2f6de9; }.option-list button.correct { border-color: #9ed3b4; background: #f3fbf5; }.option-list button.correct b { color: #fff; background: #2c9a63; }.option-list button.wrong { border-color: #e9b3b0; background: #fff7f6; }.option-list button.wrong b { color: #fff; background: #c95a51; }.option-list button:disabled { cursor: default; }.feedback { display: grid; gap: 6px; padding: 13px 15px; border: 1px solid #efc3c0; border-radius: 7px; color: #9f443e; background: #fff8f7; }.feedback--correct { border-color: #a9d9ba; color: #25734d; background: #f4fbf6; }.feedback__title { display: flex; justify-content: space-between; gap: 12px; }.feedback__title span { font-size: 11px; }.feedback p { margin: 0; color: inherit; font-size: 13px; line-height: 1.6; }.inline-error { margin: 0; color: #b42318; font-size: 12px; }.question-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding-top: 4px; border-top: 1px solid #edf0f5; }.action-spacer { flex: 1; }.primary-action,.secondary-action { min-height: 36px; padding: 0 13px; border-radius: 7px; cursor: pointer; font-size: 12px; font-weight: 700; }.primary-action { border: 1px solid #2f6de9; color: #fff; background: #2f6de9; }.secondary-action { border: 1px solid #b9cdf0; color: #265fc8; background: #fff; }.primary-action:disabled,.secondary-action:disabled { cursor: not-allowed; opacity: .5; }
@media (max-width: 850px) { .runner-page { padding: 18px 14px 26px; }.runner-header { align-items: start; flex-direction: column; }.question-layout { grid-template-columns: 1fr; }.question-rail { grid-template-columns: repeat(2, minmax(0,1fr)); }.question-rail h2,.question-rail p { grid-column: 1 / -1; } }
@media (max-width: 540px) { .question-card { padding: 16px; }.question-card h2 { font-size: 18px; }.question-actions { align-items: stretch; }.action-spacer { display: none; }.primary-action { width: 100%; }.question-actions .secondary-action { flex: 1; } }
</style>
