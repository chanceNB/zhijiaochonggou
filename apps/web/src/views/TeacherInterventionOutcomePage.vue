<template>
  <section class="outcome-page" data-testid="teacher-intervention-outcome-page">
    <header class="page-heading"><div><p class="eyebrow">Intervention outcome</p><h1>干预结果</h1><p>查看学生完成教师任务后的真实学习变化。</p></div><button class="secondary-action" type="button" @click="router.push('/teacher/interventions')">返回干预决策</button></header>
    <div v-if="loading" class="state-card">正在读取干预结果...</div>
    <div v-else-if="error" class="state-card state-card--error"><h2>干预结果暂时无法加载</h2><p>{{ error }}</p><button type="button" @click="load(true)">重新加载</button></div>
    <template v-else-if="store.intervention">
      <section class="context-card"><div><span class="eyebrow">当前干预</span><h2>{{ studentName }}</h2><p>{{ knowledgePointName }} · {{ strategyTitle }}</p></div><div class="context-status"><span>生命周期</span><strong>{{ statusLabel(store.intervention.status) }}</strong></div></section>
      <section v-if="store.outcomeState === 'EMPTY'" class="pending-panel" data-testid="outcome-pending"><span class="pending-dot" aria-hidden="true"></span><div><h2>等待学生完成任务</h2><p>Assignment 已真实下发，完成专项练习与迁移验证后，结果会自动回流到这里。</p><small>当前状态：{{ statusLabel(store.intervention.assignment?.status ?? 'PENDING_STUDENT') }}</small></div><button class="secondary-action" type="button" @click="router.push(`/student/practice/${encodeURIComponent(store.intervention.practiceSetId ?? '')}`)">查看学生练习</button></section>
      <template v-else-if="store.outcome">
        <section class="result-banner"><div><span class="eyebrow">Outcome ready</span><h2>学生任务已完成</h2><p>结果来自真实 Practice、迁移验证与学习快照。</p></div><span class="result-status">{{ statusLabel(store.outcome.transferValidation) }}</span></section>
        <section class="metrics-grid"><article><span>干预前掌握度</span><strong>{{ percent(store.outcome.masteryBefore) }}</strong></article><article><span>干预后掌握度</span><strong>{{ percent(store.outcome.masteryAfter) }}</strong></article><article><span>预测提升</span><strong>{{ signedPercent(store.outcome.predictedLift) }}</strong></article><article><span>实际提升</span><strong>{{ signedPercent(store.outcome.actualLift) }}</strong></article><article><span>预测偏差</span><strong>{{ signedPercent(store.outcome.predictionDeviation) }}</strong></article><article><span>练习正确率</span><strong>{{ percent(store.outcome.practiceAccuracyAfter) }}</strong></article></section>
        <section class="detail-panel"><header class="section-heading"><div><p class="eyebrow">Evidence comparison</p><h2>前后状态对比</h2></div><span class="source-badge">服务端结果</span></header><dl class="comparison-grid"><div><dt>置信度</dt><dd>{{ percent(store.outcome.confidenceBefore) }} → {{ percent(store.outcome.confidenceAfter) }}</dd></div><div><dt>遗忘风险</dt><dd>{{ percent(store.outcome.forgettingRiskBefore) }} → {{ percent(store.outcome.forgettingRiskAfter) }}</dd></div><div><dt>证据数量</dt><dd>{{ store.outcome.evidenceCountBefore }} → {{ store.outcome.evidenceCountAfter }}</dd></div><div><dt>迁移验证</dt><dd>{{ statusLabel(store.outcome.transferValidation) }}</dd></div><div><dt>完成时间</dt><dd>{{ formatTimestamp(store.outcome.completedAt) }}</dd></div><div><dt>结果来源</dt><dd>{{ store.outcome.dataOrigin === 'LIVE_DEMO' ? '真实演示业务数据' : store.outcome.dataOrigin }}</dd></div></dl></section>
        <section class="next-panel"><div><span class="eyebrow">Next context</span><h2>进入干预效果分析</h2><p>SmartBI 平台刷新仍需人工执行，当前 Exchange 已由后端更新。</p></div><button class="secondary-action" type="button" @click="router.push('/teacher/analytics/intervention-outcome')">打开数据洞察</button></section>
      </template>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatTimestamp, percent, signedPercent, statusLabel, strategyLabel } from '@/adapters/teacher/intervention'
import { getAnalysisRecommendation } from '@/api/teacher'
import { useTeacherInterventionStore } from '@/stores/teacherInterventionStore'
import { useTeacherStore } from '@/stores/teacherStore'

const route = useRoute(); const router = useRouter(); const store = useTeacherInterventionStore(); const teacher = useTeacherStore(); const loading = ref(true); const error = ref<string | null>(null)
const interventionId = computed(() => String(route.params.interventionId ?? ''))
const studentName = computed(() => teacher.workbench?.currentStudent?.displayName ?? '当前学生')
const knowledgePointName = computed(() => teacher.workbench?.pendingRecommendations.find((item) => item.recommendationId === store.intervention?.recommendationId)?.knowledgePointName ?? teacher.profile?.learningState?.knowledgePointName ?? '当前知识点')
const strategyTitle = computed(() => store.recommendation?.candidates.find((candidate) => candidate.strategyCode === store.intervention?.strategyCode)?.title ?? strategyLabel(store.intervention?.strategyCode ?? ''))
async function load(force = false) {
  loading.value = true; error.value = null
  await teacher.loadWorkbench(force)
  const intervention = await store.loadIntervention(interventionId.value, force)
  if (!intervention) { error.value = store.error ?? '干预记录暂时无法加载'; loading.value = false; return }
  try { store.recommendation = await getAnalysisRecommendation(intervention.recommendationId) } catch { /* outcome remains readable when the recommendation snapshot is unavailable */ }
  await store.loadOutcome(intervention.interventionId, force)
  if (store.error) error.value = store.error
  loading.value = false
}
onMounted(() => void load())
defineExpose({ load })
</script>

<style scoped>
.outcome-page { display: grid; gap: 18px; min-width: 0; color: #172238; }.page-heading,.section-heading { display: flex; align-items: end; justify-content: space-between; gap: 18px; }.page-heading h1 { margin: 4px 0 5px; color: #172a4b; font-size: 28px; }.page-heading p:last-child { margin: 0; color: #71819a; font-size: 13px; }.eyebrow { margin: 0; color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; }.source-badge,.result-status { display: inline-flex; min-height: 28px; align-items: center; padding: 0 10px; border: 1px solid #dce6f3; border-radius: 999px; color: #52709d; background: #f7faff; font-size: 11px; font-weight: 700; white-space: nowrap; }.secondary-action { min-height: 38px; padding: 0 14px; border: 1px solid #a9c3ed; border-radius: 6px; color: #265fc8; background: #fff; cursor: pointer; font-size: 12px; font-weight: 700; }.state-card { display: grid; min-height: 300px; place-items: center; align-content: center; gap: 10px; padding: 30px; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; text-align: center; }.state-card h2,.state-card p { margin: 0; }.state-card p { color: #71819a; }.state-card button { min-height: 36px; padding: 0 14px; border: 1px solid #a9c3ed; border-radius: 6px; color: #265fc8; background: #fff; cursor: pointer; }.state-card--error { color: #a43c38; }.context-card,.pending-panel,.result-banner,.detail-panel,.next-panel { display: grid; gap: 16px; padding: 18px 20px; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; box-shadow: 0 5px 18px rgb(37 61 99 / 5%); }.context-card { grid-template-columns: 1fr auto; align-items: center; }.context-card h2,.result-banner h2,.detail-panel h2,.next-panel h2,.pending-panel h2 { margin: 4px 0 0; color: #1d335e; font-size: 18px; }.context-card p,.result-banner p,.next-panel p,.pending-panel p { margin: 5px 0 0; color: #71819a; font-size: 12px; }.context-status { display: grid; gap: 5px; justify-items: end; }.context-status span { color: #8190a6; font-size: 11px; }.context-status strong { color: #18704e; font-size: 13px; }.pending-panel { grid-template-columns: auto minmax(0,1fr) auto; align-items: center; border-color: #f0d8ad; background: #fffaf0; }.pending-dot { width: 12px; height: 12px; border-radius: 50%; background: #e79a19; box-shadow: 0 0 0 5px #fff0cf; }.pending-panel small { display: block; margin-top: 8px; color: #9a6415; font-size: 11px; }.result-banner { grid-template-columns: 1fr auto; align-items: center; border-color: #c3ead6; background: #f5fcf8; }.result-status { color: #18704e; border-color: #bfe7d4; background: #fff; }.metrics-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; }.metrics-grid article { display: grid; gap: 8px; min-height: 104px; padding: 15px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; }.metrics-grid span { color: #71819a; font-size: 12px; }.metrics-grid strong { color: #245fc9; font-size: 26px; }.comparison-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 1px; margin: 0; background: #e7edf5; }.comparison-grid div { display: grid; gap: 7px; padding: 13px; background: #fbfdff; }.comparison-grid dt { color: #8190a6; font-size: 11px; }.comparison-grid dd { margin: 0; color: #2b466d; font-size: 13px; font-weight: 700; }.next-panel { grid-template-columns: 1fr auto; align-items: center; }
@media (max-width: 760px) { .page-heading,.section-heading,.context-card,.result-banner,.next-panel { align-items: start; flex-direction: column; }.pending-panel { grid-template-columns: auto 1fr; }.pending-panel .secondary-action { grid-column: 2; justify-self: start; }.metrics-grid { grid-template-columns: 1fr 1fr; }.comparison-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 480px) { .metrics-grid,.comparison-grid { grid-template-columns: 1fr; }.context-card,.pending-panel,.result-banner,.detail-panel,.next-panel { padding: 16px; } }
</style>
