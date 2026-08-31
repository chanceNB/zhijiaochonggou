<template>
  <section class="result-page" data-testid="student-practice-result">
    <div v-if="loading" class="state-card">正在整理本次练习结果...</div>
    <div v-else-if="error" class="state-card state-card--error"><h1>结果暂时无法加载</h1><p>{{ error }}</p><button type="button" @click="load">重试</button></div>
    <template v-else-if="practice.outcome">
      <header class="result-header"><div><span class="eyebrow">练习结果</span><h1>本次练习已完成</h1><p>结果来自真实答题记录，继续用下一步巩固理解。</p></div><button type="button" class="outline-action" @click="router.push('/student/practice')">返回练习空间</button></header>
      <section class="result-summary" aria-label="练习结果摘要">
        <article><span>正确率</span><strong>{{ practice.outcome.accuracyPercent }}<small>%</small></strong><p>{{ practice.outcome.attemptCount }} 道题已记录</p></article>
        <article><span>学习状态</span><strong class="status-value">{{ practice.outcome.learningStateStatusLabel }}</strong><p>本次练习后的学习状态</p></article>
        <article><span>迁移验证</span><strong class="status-value">{{ practice.outcome.transferValidationLabel }}</strong><p>仅在任务有验证时展示</p></article>
        <article v-if="practice.outcome.interventionOutcomeId"><span>干预结果</span><strong class="status-value">已记录</strong><p>干预结果已记录</p></article>
      </section>
      <section class="result-next"><div><span class="eyebrow">下一步</span><h2>{{ nextStepTitle }}</h2><p>{{ nextStepDescription }}</p></div><button type="button" @click="router.push('/student/wrong-book')">查看错题本</button></section>
      <section v-if="practice.outcome.learningStateAfter" class="learning-after"><h2>学习状态更新</h2><div><span>掌握度 <strong>{{ percent(practice.outcome.learningStateAfter.mastery) }}%</strong></span><span>置信度 <strong>{{ percent(practice.outcome.learningStateAfter.confidence) }}%</strong></span><span>遗忘风险 <strong>{{ percent(practice.outcome.learningStateAfter.forgettingRisk) }}%</strong></span><span>证据数 <strong>{{ practice.outcome.learningStateAfter.evidenceCount }}</strong></span></div></section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePracticeStore } from '@/stores/practiceStore'
const route = useRoute(); const router = useRouter(); const practice = usePracticeStore(); const loading = ref(true); const error = ref<string | null>(null)
const practiceSetId = computed(() => String(route.params.practiceSetId)); const nextStepTitle = computed(() => practice.outcome?.accuracy === 1 ? '继续验证迁移能力' : '回到错题本继续复习'); const nextStepDescription = computed(() => practice.outcome?.accuracy === 1 ? '保持当前节奏，在相似题中确认知识点可以迁移。' : '先复盘错误原因，再通过真实复习记录更新状态。')
const percent = (value: number) => Math.round(value * 100)
async function load() { loading.value = true; error.value = null; await practice.load(practiceSetId.value); const outcome = await practice.complete(); if (!outcome) error.value = practice.error ?? '结果暂时无法生成'; loading.value = false }
onMounted(() => void load())
</script>

<style scoped>
.result-page { display: grid; gap: 16px; width: min(1160px, 100%); margin: 0 auto; padding: 28px 30px 38px; color: var(--color-text); }.state-card { display: grid; min-height: 360px; place-items: center; align-content: center; gap: 10px; padding: 28px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; text-align: center; }.state-card h1,.state-card p { margin: 0; }.state-card p { color: var(--color-secondary); }.state-card button { min-height: 36px; padding: 0 15px; border: 0; border-radius: 7px; color: #fff; background: #2f6de9; cursor: pointer; }.state-card--error { color: #a43c38; }.result-header { display: flex; justify-content: space-between; gap: 18px; align-items: start; }.eyebrow { margin: 0; color: #2f6de9; font-size: 12px; font-weight: 800; }.result-header h1 { margin: 5px 0 5px; color: #14264f; font-size: 27px; }.result-header p { margin: 0; color: var(--color-secondary); font-size: 13px; }.outline-action { min-height: 36px; padding: 0 14px; border: 1px solid #8eb3f4; border-radius: 7px; color: #2563eb; background: #fff; cursor: pointer; font-size: 13px; }.result-summary { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; }.result-summary article { display: grid; gap: 7px; min-height: 142px; padding: 18px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }.result-summary span { color: #64748b; font-size: 13px; }.result-summary strong { color: #163b78; font-size: 38px; line-height: 1; }.result-summary strong small { margin-left: 3px; font-size: 17px; }.result-summary .status-value { font-size: 22px; }.result-summary p { margin: 0; color: var(--color-secondary); font-size: 12px; }.result-next { display: flex; justify-content: space-between; gap: 18px; align-items: center; padding: 18px; border: 1px solid #cfe0fb; border-radius: 8px; background: #f4f8ff; }.result-next h2 { margin: 5px 0 4px; color: #173b77; font-size: 18px; }.result-next p { margin: 0; color: #60718b; font-size: 13px; }.result-next button { min-height: 36px; padding: 0 14px; border: 1px solid #7fa8ec; border-radius: 7px; color: #2563eb; background: #fff; cursor: pointer; font-size: 13px; font-weight: 700; }.learning-after { display: grid; gap: 12px; padding: 18px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; }.learning-after h2 { margin: 0; color: #1d335e; font-size: 16px; }.learning-after > div { display: grid; grid-template-columns: repeat(4,1fr); gap: 8px; }.learning-after span { display: grid; gap: 5px; padding: 10px; color: #64748b; background: #f7f9fc; font-size: 12px; }.learning-after strong { color: #245fc9; font-size: 18px; }
@media (max-width: 700px) { .result-page { padding: 18px 14px 26px; }.result-header,.result-next { align-items: start; flex-direction: column; }.result-summary { grid-template-columns: 1fr; }.learning-after > div { grid-template-columns: repeat(2,1fr); } }
</style>
