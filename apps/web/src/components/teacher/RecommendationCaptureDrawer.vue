<template>
  <aside v-if="open" class="capture-drawer" role="dialog" aria-modal="true" aria-labelledby="capture-title" data-testid="recommendation-capture-drawer">
    <header class="capture-drawer__header">
      <div><span class="eyebrow">MANUAL_CAPTURE</span><h2 id="capture-title">记录 AI 分析建议</h2></div>
      <button class="icon-button" type="button" aria-label="关闭记录建议" @click="emit('close')">×</button>
    </header>
    <div class="capture-drawer__body">
      <p class="capture-intro">请将 AIChat 给出的分析摘要和三个候选方案记录到本地，确认后进入干预决策。</p>
      <div class="context-bound" data-testid="capture-context-bound"><span class="context-bound__dot" aria-hidden="true"></span><span>已绑定当前教学对象</span><strong v-if="contextLabel">{{ contextLabel }}</strong></div>

      <section v-if="success" class="capture-success" data-testid="capture-success">
        <span class="success-mark" aria-hidden="true">✓</span>
        <h3>分析建议已记录</h3>
        <p>已保存 {{ success.candidates.length }} 个候选策略，可在干预决策中继续审核。</p>
        <div class="success-candidates"><span v-for="candidate in success.candidates" :key="candidate.strategyCode">{{ candidate.title }}</span></div>
        <button class="primary-button" type="button" @click="emit('continue', success.recommendationId)">进入干预决策 <span aria-hidden="true">→</span></button>
      </section>

      <template v-else>
        <section class="capture-section">
          <div class="section-heading"><div><span class="eyebrow">OPTIONAL IMPORT</span><h3>粘贴结构化 JSON</h3></div><span class="section-hint">自动填入下方表单</span></div>
          <textarea v-model="jsonInput" class="json-input" rows="7" placeholder="粘贴 AIChat 输出的 JSON" aria-label="结构化建议 JSON"></textarea>
          <button class="secondary-button" type="button" @click="importJson">解析并填入</button>
          <ul v-if="jsonErrors.length" class="field-errors" data-testid="json-errors"><li v-for="error in jsonErrors" :key="error">{{ error }}</li></ul>
        </section>

        <form class="capture-form" @submit.prevent="submit">
          <section class="capture-section">
            <div class="section-heading"><div><span class="eyebrow">REQUIRED</span><h3>分析摘要</h3></div></div>
            <textarea v-model="form.analysisSummary" rows="3" placeholder="记录 AIChat 对当前学习证据的分析" aria-label="分析摘要"></textarea>
            <p v-if="fieldErrors.analysisSummary" class="field-error">{{ fieldErrors.analysisSummary }}</p>
          </section>

          <section class="capture-section">
            <div class="section-heading"><div><span class="eyebrow">EVIDENCE</span><h3>证据引用</h3></div><span class="section-hint">每行一条，可选</span></div>
            <textarea v-model="evidenceText" rows="2" placeholder="例如：诊断题反馈、学习状态记录" aria-label="证据引用"></textarea>
          </section>

          <section class="capture-section">
            <div class="section-heading"><div><span class="eyebrow">THREE CANDIDATES</span><h3>候选教学方案</h3></div><span class="candidate-count">固定 3 个</span></div>
            <article v-for="(candidate, index) in form.candidates" :key="index" class="candidate-editor">
              <div class="candidate-editor__heading"><span class="candidate-index">{{ String.fromCharCode(65 + index) }}</span><strong>方案 {{ String.fromCharCode(65 + index) }}</strong></div>
              <div class="candidate-grid">
                <label>策略代码<input v-model="candidate.strategyCode" required placeholder="例如 CONCEPT_REMEDIATION" /></label>
                <label>方案标题<input v-model="candidate.title" required placeholder="可读的方案名称" /></label>
                <label>方案理由<textarea v-model="candidate.rationale" required rows="2" placeholder="为什么建议采用"></textarea></label>
                <label>行动描述<textarea v-model="candidate.actionDescription" required rows="2" placeholder="教师将如何执行"></textarea></label>
              </div>
              <p v-if="candidateErrors[index]?.length" class="field-error">{{ candidateErrors[index].join('；') }}</p>
            </article>
          </section>

          <p v-if="submitError" class="submit-error" data-testid="capture-submit-error">{{ submitError }}</p>
          <button class="primary-button capture-submit" type="submit" :disabled="submitting">{{ submitting ? '正在记录…' : '确认并记录建议' }} <span v-if="!submitting" aria-hidden="true">→</span></button>
        </form>
      </template>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { parseRecommendationJson } from '@/adapters/teacher/smartbi'
import { RecommendationCaptureContentSchema, type RecommendationCandidate } from '@/types/contracts/smartbi'

type CandidateForm = RecommendationCandidate
type FormState = { analysisSummary: string; candidates: CandidateForm[] }
type SuccessState = { recommendationId: string; candidates: CandidateCandidate[] }
type CandidateCandidate = RecommendationCandidate

const props = withDefaults(defineProps<{
  open: boolean
  contextLabel?: string
  submitting?: boolean
  submitError?: string | null
  success?: SuccessState | null
}>(), { contextLabel: '', submitting: false, submitError: null, success: null })

const emit = defineEmits<{
  close: []
  submit: [content: { analysisSummary: string; evidenceRefs: string[]; candidates: CandidateForm[] }]
  continue: [recommendationId: string]
}>()

const emptyCandidate = (): CandidateForm => ({ strategyCode: '', title: '', rationale: '', actionDescription: '' })
const emptyForm = (): FormState => ({ analysisSummary: '', candidates: [emptyCandidate(), emptyCandidate(), emptyCandidate()] })
const form = reactive<FormState>(emptyForm())
const evidenceText = ref('')
const jsonInput = ref('')
const jsonErrors = ref<string[]>([])
const fieldErrors = reactive<{ analysisSummary?: string }>({})
const candidateErrors = ref<string[][]>([[], [], []])

const contextLabel = computed(() => props.contextLabel || '当前学生与课程')

function reset() {
  const initial = emptyForm()
  form.analysisSummary = initial.analysisSummary
  form.candidates.splice(0, form.candidates.length, ...initial.candidates)
  evidenceText.value = ''
  jsonInput.value = ''
  jsonErrors.value = []
  fieldErrors.analysisSummary = undefined
  candidateErrors.value = [[], [], []]
}

watch(() => props.open, (open) => { if (open && !props.success) reset() })

function importJson() {
  const result = parseRecommendationJson(jsonInput.value)
  if ('errors' in result) {
    jsonErrors.value = result.errors
    return
  }
  jsonErrors.value = []
  form.analysisSummary = result.content.analysisSummary
  evidenceText.value = result.content.evidenceRefs.join('\n')
  form.candidates.splice(0, form.candidates.length, ...result.content.candidates.map((candidate) => ({ ...candidate })))
  candidateErrors.value = [[], [], []]
}

function validate() {
  fieldErrors.analysisSummary = undefined
  candidateErrors.value = [[], [], []]
  const content = {
    analysisSummary: form.analysisSummary,
    evidenceRefs: evidenceText.value.split(/\r?\n/).map((item) => item.trim()).filter(Boolean),
    candidates: form.candidates,
  }
  const result = RecommendationCaptureContentSchema.safeParse(content)
  if (result.success) return result.data
  for (const issue of result.error.issues) {
    if (issue.path[0] === 'analysisSummary') fieldErrors.analysisSummary = issue.message
    if (issue.path[0] === 'candidates' && typeof issue.path[1] === 'number') {
      candidateErrors.value[issue.path[1]] ??= []
      candidateErrors.value[issue.path[1]].push(issue.message)
    }
  }
  return null
}

function submit() {
  const content = validate()
  if (content) emit('submit', content)
}
</script>

<style scoped>
.capture-drawer { position: fixed; z-index: 20; top: 0; right: 0; display: grid; grid-template-rows: auto 1fr; width: min(600px, 100vw); height: 100vh; border-left: 1px solid #dbe4f1; background: #fff; box-shadow: -10px 0 28px rgb(28 54 92 / 12%); }
.capture-drawer__header { display: flex; min-height: 78px; align-items: center; justify-content: space-between; gap: 18px; padding: 0 24px; border-bottom: 1px solid #e8eef6; }
.capture-drawer__header h2 { margin: 4px 0 0; color: #1d335e; font-size: 20px; }
.eyebrow { margin: 0; color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .08em; }
.icon-button { width: 34px; height: 34px; border: 1px solid #d8e2ef; border-radius: 7px; color: #4a5c77; background: #fff; cursor: pointer; font-size: 22px; line-height: 1; }
.capture-drawer__body { overflow-y: auto; padding: 20px 24px 34px; }
.capture-intro { margin: 0 0 14px; color: #657691; font-size: 13px; line-height: 1.7; }
.context-bound { display: flex; min-height: 34px; align-items: center; gap: 8px; margin-bottom: 18px; padding: 0 11px; border: 1px solid #dce9fb; border-radius: 6px; color: #31527e; background: #f6f9ff; font-size: 12px; }
.context-bound strong { margin-left: auto; color: #54709a; font-weight: 700; }
.context-bound__dot { width: 7px; height: 7px; border-radius: 50%; background: #2ca875; }
.capture-section { display: grid; gap: 10px; margin-top: 20px; }
.section-heading { display: flex; align-items: end; justify-content: space-between; gap: 12px; }
.section-heading h3 { margin: 4px 0 0; color: #213858; font-size: 14px; }
.section-hint, .candidate-count { color: #8796aa; font-size: 11px; }
textarea, input { width: 100%; border: 1px solid #d6e0ed; border-radius: 6px; color: #263b5c; background: #fff; font: inherit; font-size: 12px; outline: none; }
textarea { padding: 10px 11px; resize: vertical; line-height: 1.6; }
input { min-height: 34px; padding: 0 9px; }
textarea:focus, input:focus { border-color: #7da5ef; box-shadow: 0 0 0 3px rgb(37 99 235 / 10%); }
textarea::placeholder, input::placeholder { color: #a2adbc; }
.json-input { background: #f9fbfe; font-family: Consolas, monospace; font-size: 11px; }
.secondary-button, .primary-button { min-height: 36px; padding: 0 14px; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 700; }
.secondary-button { width: max-content; border: 1px solid #b9cdf0; color: #2563eb; background: #fff; }
.primary-button { border: 0; color: #fff; background: #2563eb; }
.primary-button:disabled { cursor: wait; opacity: .65; }
.field-errors { display: grid; gap: 4px; margin: 0; padding-left: 18px; color: #b24a42; font-size: 11px; }
.field-error, .submit-error { margin: 0; color: #b24a42; font-size: 11px; line-height: 1.5; }
.candidate-editor { display: grid; gap: 12px; padding: 14px; border: 1px solid #e0e8f2; border-radius: 7px; background: #fbfdff; }
.candidate-editor__heading { display: flex; align-items: center; gap: 8px; color: #2b466d; font-size: 13px; }
.candidate-index { display: grid; width: 24px; height: 24px; place-items: center; border-radius: 50%; color: #2563eb; background: #eaf2ff; font-size: 11px; font-weight: 800; }
.candidate-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.candidate-grid label { display: grid; gap: 5px; color: #72839d; font-size: 11px; }
.candidate-grid label:nth-child(n+3) { grid-column: span 2; }
.capture-submit { width: 100%; margin-top: 22px; }
.capture-success { display: grid; justify-items: center; gap: 9px; padding: 36px 20px; border: 1px solid #d3eedf; border-radius: 8px; background: #f4fcf7; text-align: center; }
.success-mark { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 50%; color: #16724d; background: #d9f4e4; font-size: 22px; font-weight: 800; }
.capture-success h3 { margin: 0; color: #1d5a42; font-size: 18px; }
.capture-success p { margin: 0; color: #648577; font-size: 12px; line-height: 1.6; }
.success-candidates { display: grid; width: 100%; gap: 6px; margin: 8px 0; }
.success-candidates span { padding: 8px 10px; border: 1px solid #d9eee2; border-radius: 5px; color: #2b6950; background: #fff; font-size: 12px; text-align: left; }
@media (max-width: 520px) { .capture-drawer__header, .capture-drawer__body { padding-inline: 16px; } .candidate-grid { grid-template-columns: 1fr; } .candidate-grid label:nth-child(n+3) { grid-column: auto; } }
</style>
