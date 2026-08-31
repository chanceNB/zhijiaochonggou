<template>
  <section class="smartbi-page" data-testid="teacher-smartbi-center-page">
    <header class="page-heading">
      <div><p class="eyebrow">Teacher intelligence</p><h1>数据洞察</h1><p>基于当前学生的真实学习数据进行分析，并记录可执行教学建议。</p></div>
      <DataFreshnessBadge :status="freshnessBadgeStatus" :lag-seconds="smartbi.freshness?.lagSeconds ?? 0" />
    </header>

    <section class="context-card" data-testid="smartbi-analysis-context">
      <div class="context-card__heading"><div><p class="eyebrow">Current analysis context</p><h2>当前分析对象</h2></div><span class="context-status" :class="{ 'is-ready': Boolean(context) }">{{ context ? '已绑定真实对象' : '暂无当前对象' }}</span></div>
      <div v-if="context" class="context-grid">
        <div><span>当前学生</span><strong>{{ context.displayName }}</strong></div>
        <div><span>课程</span><strong>{{ context.courseName }}</strong></div>
        <div><span>班级</span><strong>{{ context.className }}</strong></div>
        <div><span>当前知识点</span><strong>{{ knowledgePointName ?? '暂无当前知识点' }}</strong></div>
      </div>
      <div v-else class="context-empty"><strong>暂无当前分析对象</strong><span>请先让真实教学对象进入 ACTIVE demo run。</span></div>
    </section>

    <section class="analysis-section" data-testid="student-risk-section">
      <header class="section-heading"><div><p class="eyebrow">01 / SmartBI dashboard</p><h2>学习风险分析</h2><p>查看当前学生学习状态、练习证据和知识点风险。</p></div><span class="section-chip">真实平台分析</span></header>
      <SmartBiEmbedPanel :asset="studentRiskAsset" :loading="smartbi.assetsState === 'LOADING'" :error-state="smartbi.assetsState === 'DEGRADED' ? 'DEGRADED' : smartbi.assetsState === 'FORBIDDEN' ? 'FORBIDDEN' : null" pending-message="学习风险分析平台入口尚未完成配置。" />
    </section>

    <section class="analysis-section ai-section" data-testid="aichat-section">
      <header class="section-heading"><div><p class="eyebrow">02 / AI analysis</p><h2>AI 分析助手</h2><p>让 SmartBI AIChat 基于当前数据模型分析当前对象，并给出三个候选教学方案。</p></div><span class="section-chip section-chip--soft">人工记录</span></header>
      <SmartBiEmbedPanel :asset="aichatAsset" pending-message="AI 分析入口尚未完成平台配置。" />
      <div class="capture-action">
        <div><strong>已经在 AIChat 中看到建议？</strong><p>在本地记录分析摘要与三个候选方案，作为后续干预决策的真实输入。</p></div>
        <button class="primary-button" type="button" :disabled="!captureContext" @click="drawerOpen = true">记录分析建议 <span aria-hidden="true">→</span></button>
      </div>
      <p v-if="!captureContext" class="capture-disabled">当前分析对象还没有完整知识点上下文，暂不能记录建议。</p>
      <RecommendationCaptureDrawer
        :open="drawerOpen"
        :context-label="contextLabel"
        :submitting="smartbi.captureState === 'SUBMITTING'"
        :submit-error="smartbi.captureState === 'ERROR' ? smartbi.error : null"
        :success="captureSuccess"
        @close="drawerOpen = false"
        @submit="submitCapture"
        @continue="continueToInterventions"
      />
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DataFreshnessBadge from '@/components/teacher/DataFreshnessBadge.vue'
import RecommendationCaptureDrawer from '@/components/teacher/RecommendationCaptureDrawer.vue'
import SmartBiEmbedPanel from '@/components/teacher/SmartBiEmbedPanel.vue'
import { useSmartBiStore } from '@/stores/smartbiStore'
import { useTeacherStore } from '@/stores/teacherStore'
import type { RecommendationCaptureContent } from '@/types/contracts/smartbi'

const router = useRouter()
const teacher = useTeacherStore()
const smartbi = useSmartBiStore()
const drawerOpen = ref(false)
const captureSuccess = ref<{ recommendationId: string; candidates: RecommendationCaptureContent['candidates'] } | null>(null)

const context = computed(() => teacher.workbench?.currentStudent ?? null)
const profileState = computed(() => teacher.profile?.learningState ?? null)
const knowledgePointName = computed(() => profileState.value?.knowledgePointName
  ?? teacher.workbench?.priorityItems.find((item) => item.knowledgePointName)?.knowledgePointName
  ?? teacher.workbench?.pendingRecommendations.find((item) => item.knowledgePointName)?.knowledgePointName
  ?? null)
const knowledgePointId = computed(() => profileState.value?.knowledgePointId
  ?? teacher.workbench?.pendingRecommendations.find((item) => item.knowledgePointId)?.knowledgePointId
  ?? null)
const contextLabel = computed(() => context.value ? `${context.value.displayName} · ${context.value.courseName}` : '')
const captureContext = computed(() => context.value && knowledgePointId.value ? {
  studentId: context.value.studentId,
  courseId: context.value.courseId,
  classId: context.value.classId,
  knowledgePointId: knowledgePointId.value,
  demoRunId: context.value.demoRunId,
  demoCaseId: context.value.demoCaseId,
  correlationId: context.value.correlationId,
} : null)
const studentRiskAsset = computed(() => smartbi.assets.find((asset) => asset.assetKey === 'student-risk') ?? null)
const aichatAsset = computed(() => smartbi.assets.find((asset) => asset.assetKey === 'aichat') ?? null)
const freshnessBadgeStatus = computed(() => smartbi.freshnessState === 'DEGRADED' ? 'DEGRADED' as const : smartbi.freshnessState === 'LOADING' || smartbi.freshnessState === 'INITIAL' ? 'LOADING' as const : smartbi.freshness?.status ?? 'NO_DATA')

async function load() {
  await teacher.loadWorkbench(true)
  if (context.value) await teacher.loadProfile(context.value.studentId, context.value.courseId, true)
  await smartbi.loadOverview(true)
}

async function submitCapture(content: RecommendationCaptureContent) {
  if (!captureContext.value) return
  const result = await smartbi.captureRecommendation(content, captureContext.value)
  if (result) captureSuccess.value = { recommendationId: result.recommendationId, candidates: content.candidates }
}

function continueToInterventions(recommendationId: string) {
  drawerOpen.value = false
  void router.push({ path: '/teacher/interventions', query: { recommendationId } })
}

onMounted(() => void load())
</script>

<style scoped>
.smartbi-page { display: grid; gap: 22px; min-width: 0; color: #172238; }
.page-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; }
.page-heading h1 { margin: 4px 0 5px; color: #172a4b; font-size: 28px; }
.page-heading p:last-child { margin: 0; color: #6e7c92; font-size: 13px; }
.eyebrow { margin: 0; color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; }
.context-card { display: grid; gap: 17px; padding: 18px 20px; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; box-shadow: 0 5px 18px rgb(37 61 99 / 5%); }
.context-card__heading, .section-heading { display: flex; align-items: end; justify-content: space-between; gap: 16px; }
.context-card h2, .section-heading h2 { margin: 4px 0 0; color: #1d335e; font-size: 18px; }
.context-status, .section-chip { display: inline-flex; min-height: 26px; align-items: center; padding: 0 9px; border: 1px solid #e3e9f1; border-radius: 999px; color: #7d8ca2; background: #f8fafc; font-size: 11px; font-weight: 700; white-space: nowrap; }
.context-status.is-ready { border-color: #c3ead6; color: #18704e; background: #f1fbf5; }
.context-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1px; overflow: hidden; border: 1px solid #e7edf5; border-radius: 6px; background: #e7edf5; }
.context-grid div { display: grid; gap: 7px; min-width: 0; padding: 12px 14px; background: #fbfdff; }
.context-grid span { color: #8190a6; font-size: 11px; }
.context-grid strong { overflow: hidden; color: #2b466d; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.context-empty { display: grid; justify-items: center; gap: 6px; padding: 24px; border: 1px dashed #d5dfeb; border-radius: 6px; color: #75859e; background: #fbfdff; text-align: center; }
.context-empty strong { color: #355071; font-size: 14px; }
.context-empty span { font-size: 12px; }
.analysis-section { display: grid; gap: 14px; min-width: 0; }
.section-heading { align-items: start; }
.section-heading p:last-child { margin: 5px 0 0; color: #71819a; font-size: 12px; line-height: 1.6; }
.section-chip { margin-top: 3px; color: #3164b2; border-color: #d7e5fb; background: #f4f8ff; }
.section-chip--soft { color: #237c67; border-color: #d1eee2; background: #f3fbf7; }
.ai-section { padding-top: 2px; }
.capture-action { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 14px 16px; border: 1px solid #dfe7f2; border-radius: 7px; background: #fff; }
.capture-action strong { color: #284566; font-size: 13px; }
.capture-action p { margin: 4px 0 0; color: #7a8aa2; font-size: 12px; }
.primary-button { min-height: 38px; padding: 0 14px; border: 0; border-radius: 6px; color: #fff; background: #2563eb; cursor: pointer; font-size: 12px; font-weight: 700; white-space: nowrap; }
.primary-button:disabled { cursor: not-allowed; opacity: .5; }
.capture-disabled { margin: -4px 0 0; color: #a06b28; font-size: 11px; }
@media (max-width: 900px) { .context-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .page-heading, .section-heading, .capture-action { align-items: flex-start; flex-direction: column; } .context-grid { grid-template-columns: 1fr; } .capture-action .primary-button { width: 100%; } }
</style>
