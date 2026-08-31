<template>
  <section class="intervention-page" data-testid="teacher-intervention-page">
    <header class="page-heading">
      <div><p class="eyebrow">Teacher decision</p><h1>干预决策</h1><p>基于真实学习证据选择方案，由教师确认后再下发任务。</p></div>
      <span class="source-badge">AI 建议 · 教师确认</span>
    </header>

    <div v-if="loading" class="state-card">正在读取当前分析建议...</div>
    <div v-else-if="error" class="state-card state-card--error"><h2>干预决策暂时不可用</h2><p>{{ error }}</p><button type="button" @click="load">重新加载</button></div>
    <template v-else-if="showOutcomeQueue">
      <section class="empty-panel" data-testid="intervention-outcome-queue">
        <p class="eyebrow">Outcome queue</p><h2>待跟进的干预结果</h2>
        <p v-if="!teacher.workbench?.pendingOutcomes.length">当前没有待学生完成的干预。</p>
        <button v-for="item in teacher.workbench?.pendingOutcomes ?? []" :key="item.interventionId" type="button" class="queue-item" @click="router.push(`/teacher/interventions/${encodeURIComponent(item.interventionId)}`)">
          <span><strong>{{ item.knowledgePointName }}</strong><small>{{ strategyLabel(item.strategy) }} · {{ statusLabel(item.status) }}</small></span><span aria-hidden="true">→</span>
        </button>
      </section>
    </template>
    <template v-else-if="store.recommendationState === 'EMPTY' || !store.recommendation">
      <section class="empty-panel" data-testid="recommendation-empty"><p class="eyebrow">Recommendation</p><h2>暂无待审核的分析建议</h2><p>请先在数据洞察中记录 SmartBI AIChat 的三个候选方案。</p><button type="button" class="secondary-action" @click="router.push('/teacher/analytics')">返回数据洞察</button></section>
    </template>
    <template v-else>
      <section class="context-card" data-testid="intervention-context">
        <div><span class="eyebrow">当前教学对象</span><h2>{{ currentStudent?.displayName ?? '当前学生' }}</h2></div>
        <dl><div><dt>课程</dt><dd>{{ currentStudent?.courseName ?? '当前课程' }}</dd></div><div><dt>班级</dt><dd>{{ currentStudent?.className ?? '当前班级' }}</dd></div><div><dt>知识点</dt><dd>{{ knowledgePointName }}</dd></div></dl>
      </section>

      <section class="evidence-panel"><header class="section-heading"><div><p class="eyebrow">Analysis snapshot</p><h2>分析建议</h2></div><span class="status-chip">{{ statusLabel(store.recommendation.status) }}</span></header><p class="summary">{{ store.recommendation.analysisSummary }}</p><div v-if="store.recommendation.evidenceRefs.length" class="evidence-list"><span v-for="ref in store.recommendation.evidenceRefs" :key="ref">证据：{{ ref }}</span></div></section>

      <section class="candidate-section"><header class="section-heading"><div><p class="eyebrow">Three candidates</p><h2>AIChat 候选教学方案</h2><h3>选择一个教学方案</h3><p>以下方案来自 SmartBI AIChat 分析，请选择一个作为本次正式教学干预。</p><p class="section-note">AI 建议仅作为参考，正式干预由教师确认。</p></div><span class="candidate-count">{{ store.recommendation.candidates.length }} 个方案</span></header>
        <div class="candidate-grid">
          <label v-for="candidate in store.recommendation.candidates" :key="candidate.strategyCode" class="strategy-card" data-testid="strategy-card" :class="{ selected: selectedStrategy === candidate.strategyCode, disabled: Boolean(store.intervention) }">
            <input v-model="selectedStrategy" type="radio" name="strategy" :value="candidate.strategyCode" :disabled="Boolean(store.intervention)" />
            <span class="strategy-card__index">{{ String.fromCharCode(64 + (candidate.candidateIndex ?? 1)) }}</span>
            <span class="strategy-card__body"><strong>{{ candidate.title }}</strong><em>{{ candidate.rationale }}</em><span>{{ candidate.actionDescription }}</span></span>
          </label>
        </div>
      </section>

      <section v-if="!store.intervention" class="decision-panel"><div><p class="eyebrow">Teacher confirmation</p><h2>确认你的教学判断</h2><p>请说明为什么选择这个方案，便于后续复盘。</p></div><textarea v-model="teacherRationale" data-testid="teacher-rationale" rows="3" placeholder="至少填写 10 个字符的教学判断" /><p v-if="validationError" class="form-error">{{ validationError }}</p><button class="primary-action" data-testid="propose-intervention" type="button" :disabled="store.interventionState === 'LOADING'" @click="propose">进入方案评估</button></section>

      <section v-else class="lifecycle-panel" data-testid="intervention-lifecycle">
        <header class="section-heading"><div><p class="eyebrow">Effect estimator</p><h2>干预生命周期</h2></div><span class="status-chip status-chip--active">{{ statusLabel(store.intervention.status) }}</span></header>
        <div class="estimate-grid"><div><span>预计提升</span><strong>{{ percent(store.intervention.predictedLift) }}</strong></div><div><span>预测区间</span><strong>{{ percent(store.intervention.predictionInterval.low) }} - {{ percent(store.intervention.predictionInterval.high) }}</strong></div><div><span>版本</span><strong>{{ store.intervention.version }}</strong></div></div>
        <p class="estimator-note">本结果由本地 EffectEstimator 生成，用于辅助教师决策。</p>
        <p v-if="store.error" class="form-error">{{ store.error }}</p>
        <div v-if="store.intervention.status === 'PROPOSED'" class="lifecycle-actions"><button class="primary-action" data-testid="approve-intervention" type="button" :disabled="store.interventionState === 'LOADING'" @click="approve">审核通过</button></div>
        <div v-else-if="store.intervention.status === 'APPROVED'" class="lifecycle-actions"><button class="primary-action" data-testid="commit-intervention" type="button" :disabled="store.interventionState === 'LOADING'" @click="commit">正式下发任务</button></div>
        <div v-else class="assignment-success" data-testid="assignment-success"><div><strong>{{ assignmentCompleted ? '任务已完成' : '已下发' }}</strong><span>学生：{{ currentStudent?.displayName ?? '当前学生' }}</span><span>方案：{{ selectedCandidate?.title ?? '已选择的教学方案' }}</span></div><dl><div><dt>Assignment</dt><dd>{{ store.intervention.assignmentId ?? '已生成' }}</dd></div><div><dt>Practice Set</dt><dd>{{ store.intervention.practiceSetId ?? '已生成' }}</dd></div><div><dt>状态</dt><dd>{{ statusLabel(store.intervention.assignment?.status ?? 'PENDING_STUDENT') }}</dd></div></dl><p>{{ assignmentCompleted ? '学生已完成任务，可查看服务端记录的真实干预结果。' : '等待学生完成任务后，这里会出现真实干预结果。' }}</p><div class="lifecycle-actions"><button class="secondary-action" type="button" @click="router.push(`/teacher/interventions/${encodeURIComponent(store.intervention!.interventionId)}`)">查看干预结果</button><button class="secondary-action" type="button" @click="router.push(`/student/practice/${encodeURIComponent(store.intervention!.practiceSetId!)}`)">查看练习入口</button></div></div>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTeacherStore } from '@/stores/teacherStore'
import { useTeacherInterventionStore } from '@/stores/teacherInterventionStore'
import { formatTimestamp, percent, statusLabel, strategyLabel } from '@/adapters/teacher/intervention'

const route = useRoute(); const router = useRouter(); const teacher = useTeacherStore(); const store = useTeacherInterventionStore()
const loading = ref(true); const error = ref<string | null>(null); const selectedStrategy = ref(''); const teacherRationale = ref(''); const validationError = ref<string | null>(null)
const currentStudent = computed(() => teacher.workbench?.currentStudent ?? null)
const recommendationId = computed(() => String(route.query.recommendationId ?? teacher.workbench?.pendingRecommendations[0]?.recommendationId ?? ''))
const showOutcomeQueue = computed(() => route.query.section === 'outcome')
const knowledgePointName = computed(() => store.recommendation?.candidates.length ? (teacher.workbench?.pendingRecommendations.find((item) => item.recommendationId === store.recommendation?.recommendationId)?.knowledgePointName ?? '当前知识点') : '当前知识点')
const selectedCandidate = computed(() => store.recommendation?.candidates.find((candidate) => candidate.strategyCode === selectedStrategy.value) ?? null)
const assignmentCompleted = computed(() => store.intervention?.assignment?.status === 'COMPLETED')

const interventionId = computed(() => String(route.query.interventionId ?? ''))
async function load() {
  loading.value = true; error.value = null
  await teacher.loadWorkbench(true)
  if (showOutcomeQueue.value) { loading.value = false; return }
  const recommendation = await store.loadRecommendation(recommendationId.value, true)
  if (recommendation && interventionId.value) await store.loadIntervention(interventionId.value, true)
  else if (recommendation) await store.loadInterventionForRecommendation(recommendation.recommendationId, true)
  if (store.intervention) selectedStrategy.value = store.intervention.strategyCode
  if (!recommendation && store.error) error.value = store.error
  loading.value = false
}
async function propose() {
  validationError.value = null
  if (!selectedStrategy.value) { validationError.value = '请选择一个教学方案'; return }
  if (teacherRationale.value.trim().length < 10) { validationError.value = '请填写至少 10 个字符的教学判断'; return }
  if (!store.recommendation) return
  const intervention = await store.propose({ recommendationId: store.recommendation.recommendationId, strategyCode: selectedStrategy.value, teacherRationale: teacherRationale.value.trim() })
  if (intervention) await router.replace({ path: '/teacher/interventions', query: { recommendationId: store.recommendation.recommendationId, interventionId: intervention.interventionId } })
}
async function approve() { const intervention = await store.approve(); if (intervention) await router.replace({ path: '/teacher/interventions', query: { recommendationId: intervention.recommendationId, interventionId: intervention.interventionId } }) }
async function commit() { const intervention = await store.commit(); if (intervention) await router.replace({ path: '/teacher/interventions', query: { recommendationId: intervention.recommendationId, interventionId: intervention.interventionId } }) }
watch(recommendationId, () => { if (!showOutcomeQueue.value) void load() })
onMounted(() => void load())
defineExpose({ load, formatTimestamp })
</script>

<style scoped>
.intervention-page { display: grid; gap: 18px; min-width: 0; color: #172238; }.page-heading,.section-heading { display: flex; align-items: end; justify-content: space-between; gap: 18px; }.page-heading h1 { margin: 4px 0 5px; color: #172a4b; font-size: 28px; }.page-heading p:last-child,.section-heading p:last-child { margin: 0; color: #71819a; font-size: 13px; }.eyebrow { margin: 0; color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; }.source-badge,.status-chip,.candidate-count { display: inline-flex; min-height: 28px; align-items: center; padding: 0 10px; border: 1px solid #dce6f3; border-radius: 999px; color: #52709d; background: #f7faff; font-size: 11px; font-weight: 700; white-space: nowrap; }.status-chip--active { color: #18704e; border-color: #bfe7d4; background: #f0fbf5; }.context-card,.evidence-panel,.candidate-section,.decision-panel,.lifecycle-panel,.empty-panel { display: grid; gap: 16px; padding: 18px 20px; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; box-shadow: 0 5px 18px rgb(37 61 99 / 5%); }.context-card { grid-template-columns: 1fr 2fr; align-items: center; }.context-card h2,.evidence-panel h2,.candidate-section h2,.decision-panel h2,.lifecycle-panel h2,.empty-panel h2 { margin: 4px 0 0; color: #1d335e; font-size: 18px; }.context-card dl { display: grid; grid-template-columns: repeat(3,1fr); gap: 1px; margin: 0; background: #e7edf5; }.context-card dl div { display: grid; gap: 5px; padding: 11px 13px; background: #fbfdff; }.context-card dt { color: #8190a6; font-size: 11px; }.context-card dd { overflow: hidden; margin: 0; color: #2b466d; font-size: 13px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }.summary { margin: 0; color: #435875; font-size: 14px; line-height: 1.7; }.evidence-list { display: flex; flex-wrap: wrap; gap: 7px; }.evidence-list span { padding: 5px 8px; border: 1px solid #e3ebf5; border-radius: 5px; color: #67809f; background: #f8fbff; font-size: 11px; }.section-heading { align-items: start; }.candidate-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; }.strategy-card { display: grid; grid-template-columns: auto auto minmax(0,1fr); gap: 10px; min-width: 0; padding: 15px; border: 1px solid #dce5f1; border-radius: 8px; background: #fff; cursor: pointer; }.strategy-card.selected { border-color: #84aaf2; background: #f5f8ff; box-shadow: 0 0 0 2px rgb(47 109 233 / 10%); }.strategy-card.disabled { cursor: default; }.strategy-card input { margin: 3px 0 0; accent-color: #2f6de9; }.strategy-card__index { display: grid; width: 25px; height: 25px; place-items: center; border-radius: 50%; color: #fff; background: #8ba2c3; font-size: 11px; font-weight: 800; }.strategy-card.selected .strategy-card__index { background: #2f6de9; }.strategy-card__body { display: grid; gap: 5px; min-width: 0; }.strategy-card__body strong { color: #1d3c70; font-size: 15px; }.strategy-card__body small { color: #5a77a1; font-size: 11px; }.strategy-card__body em,.strategy-card__body span { color: #70829b; font-size: 12px; font-style: normal; line-height: 1.5; }.decision-panel { grid-template-columns: minmax(0,1fr) minmax(250px, 420px) auto; align-items: end; }.decision-panel p { margin: 0; color: #71819a; font-size: 12px; line-height: 1.6; }.decision-panel textarea { width: 100%; min-height: 76px; resize: vertical; padding: 10px 12px; border: 1px solid #d4dfed; border-radius: 6px; color: #304b72; font: inherit; font-size: 12px; }.primary-action,.secondary-action { min-height: 38px; padding: 0 14px; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 700; white-space: nowrap; }.primary-action { border: 1px solid #2563eb; color: #fff; background: #2563eb; }.secondary-action { border: 1px solid #a9c3ed; color: #265fc8; background: #fff; }.primary-action:disabled { cursor: not-allowed; opacity: .5; }.form-error { margin: 0; color: #b42318 !important; font-size: 12px !important; }.estimate-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 10px; }.estimate-grid div { display: grid; gap: 6px; padding: 12px; background: #f7f9fc; }.estimate-grid span { color: #71819a; font-size: 11px; }.estimate-grid strong { color: #245fc9; font-size: 19px; }.estimator-note { margin: 0; color: #71819a; font-size: 11px; }.lifecycle-actions { display: flex; justify-content: flex-end; gap: 8px; }.assignment-success { display: grid; gap: 14px; padding: 15px; border: 1px solid #c3ead6; border-radius: 7px; background: #f5fcf8; }.assignment-success > div:first-child { display: grid; gap: 4px; }.assignment-success > div:first-child strong { color: #18704e; font-size: 16px; }.assignment-success > div:first-child span { color: #4f6f63; font-size: 12px; }.assignment-success dl { display: grid; grid-template-columns: repeat(3,1fr); gap: 1px; margin: 0; background: #dcefe5; }.assignment-success dl div { display: grid; gap: 5px; padding: 10px; background: #fbfffc; }.assignment-success dt { color: #6c8d7c; font-size: 10px; }.assignment-success dd { margin: 0; overflow: hidden; color: #315e4d; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }.assignment-success p { margin: 0; color: #587568; font-size: 12px; }.empty-panel { min-height: 280px; align-content: center; justify-items: start; }.empty-panel p:not(.eyebrow) { margin: 0; color: #71819a; font-size: 13px; }.queue-item { display: flex; width: 100%; justify-content: space-between; gap: 15px; align-items: center; padding: 13px 14px; border: 1px solid #dce5f1; border-radius: 7px; color: #294b7b; background: #fbfdff; cursor: pointer; text-align: left; }.queue-item span:first-child { display: grid; gap: 4px; }.queue-item small { color: #71819a; font-size: 11px; }
@media (max-width: 980px) { .context-card { grid-template-columns: 1fr; }.candidate-grid { grid-template-columns: 1fr; }.decision-panel { grid-template-columns: 1fr; align-items: stretch; }.decision-panel .primary-action { justify-self: start; }.assignment-success dl { grid-template-columns: 1fr; } }
@media (max-width: 620px) { .page-heading,.section-heading { align-items: start; flex-direction: column; }.context-card,.evidence-panel,.candidate-section,.decision-panel,.lifecycle-panel,.empty-panel { padding: 16px; }.context-card dl { grid-template-columns: 1fr; }.estimate-grid { grid-template-columns: 1fr; }.lifecycle-actions { justify-content: stretch; flex-wrap: wrap; }.lifecycle-actions button { flex: 1; } }
.candidate-section h3 { margin: 8px 0 0; color: #294a78; font-size: 15px; }
.section-note { color: #8190a6 !important; font-size: 12px !important; }
</style>
