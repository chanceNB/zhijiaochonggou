import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SmartBiEmbedPanel from '../components/teacher/SmartBiEmbedPanel.vue'
import DataFreshnessBadge from '../components/teacher/DataFreshnessBadge.vue'
import RecommendationCaptureDrawer from '../components/teacher/RecommendationCaptureDrawer.vue'
import { useSmartBiStore } from '../stores/smartbiStore'
import { SmartBiAssetSchema } from '../types/contracts/smartbi'
import { captureAnalysisRecommendation } from '../api/smartbi'

vi.mock('../api/smartbi', () => ({
  getSmartBiAssets: vi.fn(),
  getSmartBiAsset: vi.fn(),
  getSmartBiFreshness: vi.fn(),
  captureAnalysisRecommendation: vi.fn(),
  getAnalysisRecommendation: vi.fn(),
}))

const pendingAsset = { assetKey: 'student-risk', type: 'DASHBOARD', displayName: '学生风险分析', status: 'PLATFORM_PENDING' as const, launchMode: 'UNVERIFIED' as const, resourceUrl: null }
const iframeAsset = { assetKey: 'student-risk', type: 'DASHBOARD', displayName: '学生风险分析', status: 'VERIFIED' as const, launchMode: 'IFRAME' as const, resourceUrl: 'https://smartbi.example/student-risk' }
const newTabAsset = { assetKey: 'intervention-outcome', type: 'DASHBOARD', displayName: '干预成效', status: 'VERIFIED' as const, launchMode: 'NEW_TAB' as const, resourceUrl: 'https://smartbi.example/intervention-outcome' }

const validJson = JSON.stringify({
  analysisSummary: '当前学习证据显示需要验证图遍历迁移能力。',
  evidenceRefs: ['诊断题反馈', '学习状态记录'],
  candidates: [
    { strategyCode: 'CONCEPT_REMEDIATION', title: '概念补强', rationale: '先校准概念边界', actionDescription: '概念辨析练习' },
    { strategyCode: 'VISUAL_TRANSFER_PRACTICE', title: '可视化迁移', rationale: '通过过程建立迁移', actionDescription: '图结构变式练习' },
    { strategyCode: 'AI_GUIDED_VARIATION', title: 'AI 引导变式', rationale: '持续提供分层反馈', actionDescription: '分层变式练习' },
  ],
})

describe('F04 SmartBI embed and freshness states', () => {
  it('validates complete asset transport data', () => {
    expect(SmartBiAssetSchema.parse(iframeAsset).launchMode).toBe('IFRAME')
    expect(() => SmartBiAssetSchema.parse({ ...iframeAsset, launchMode: 'IFRAME', resourceUrl: 'not-a-url' })).toThrow()
  })

  it('renders a real iframe only for a verified iframe asset', () => {
    const wrapper = mount(SmartBiEmbedPanel, { props: { asset: iframeAsset } })
    expect(wrapper.get('[data-testid="smartbi-state-iframe"]').find('iframe').attributes('src')).toBe(iframeAsset.resourceUrl)
    expect(wrapper.find('[data-testid="smartbi-state-new-tab"]').exists()).toBe(false)
  })

  it('renders a new-tab resource without an iframe', () => {
    const wrapper = mount(SmartBiEmbedPanel, { props: { asset: newTabAsset } })
    expect(wrapper.get('[data-testid="smartbi-state-new-tab"]').find('a').attributes('href')).toBe(newTabAsset.resourceUrl)
    expect(wrapper.find('iframe').exists()).toBe(false)
  })

  it('renders an unverified pending state without a fake link or chart', () => {
    const wrapper = mount(SmartBiEmbedPanel, { props: { asset: pendingAsset } })
    expect(wrapper.get('[data-testid="smartbi-state-unverified"]').text()).toContain('尚未完成配置')
    expect(wrapper.find('a').exists()).toBe(false)
    expect(wrapper.find('canvas').exists()).toBe(false)
  })

  it('maps FRESH and STALE to visible freshness states', () => {
    expect(mount(DataFreshnessBadge, { props: { status: 'FRESH' } }).text()).toContain('数据新鲜')
    expect(mount(DataFreshnessBadge, { props: { status: 'STALE', lagSeconds: 90 } }).text()).toContain('数据可能滞后')
  })

  it('keeps SmartBI platform failures as a degraded state', () => {
    const wrapper = mount(SmartBiEmbedPanel, { props: { asset: null, errorState: 'DEGRADED' } })
    expect(wrapper.get('[data-testid="smartbi-state-degraded"]').text()).toContain('暂时不可用')
  })
})

describe('RecommendationCaptureDrawer', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('accepts valid JSON and emits exactly three candidates', async () => {
    const wrapper = mount(RecommendationCaptureDrawer, { props: { open: true, contextLabel: '小明 · 数据结构' } })
    await wrapper.get('.json-input').setValue(validJson)
    await wrapper.get('.secondary-button').trigger('click')
    expect(wrapper.findAll('.candidate-editor')).toHaveLength(3)
    await wrapper.get('form').trigger('submit')
    const emitted = wrapper.emitted('submit')
    expect(emitted).toHaveLength(1)
    expect(emitted?.[0]?.[0]).toMatchObject({ candidates: expect.any(Array) })
    expect((emitted?.[0]?.[0] as { candidates: unknown[] }).candidates).toHaveLength(3)
  })

  it('rejects invalid JSON and candidate counts other than three', async () => {
    const wrapper = mount(RecommendationCaptureDrawer, { props: { open: true } })
    await wrapper.get('.json-input').setValue('{broken')
    await wrapper.get('.secondary-button').trigger('click')
    expect(wrapper.get('[data-testid="json-errors"]').text()).toContain('JSON 格式不正确')

    const twoCandidates = JSON.parse(validJson) as { candidates: unknown[] }
    twoCandidates.candidates = twoCandidates.candidates.slice(0, 2)
    await wrapper.get('.json-input').setValue(JSON.stringify(twoCandidates))
    await wrapper.get('.secondary-button').trigger('click')
    expect(wrapper.get('[data-testid="json-errors"]').text()).toContain('3 个候选方案')
  })

  it('keeps manual editor fixed at three candidate sections', () => {
    const wrapper = mount(RecommendationCaptureDrawer, { props: { open: true } })
    expect(wrapper.findAll('.candidate-editor')).toHaveLength(3)
    expect(wrapper.findAll('input[placeholder="例如 CONCEPT_REMEDIATION"]')).toHaveLength(3)
  })
})

describe('SmartBI capture store', () => {
  it('injects active context, fixed source, and idempotency key while preserving recommendation id', async () => {
    setActivePinia(createPinia())
    vi.mocked(captureAnalysisRecommendation).mockResolvedValue({ recommendationId: 'rec-real-1', status: 'PENDING_TEACHER_REVIEW' })
    const store = useSmartBiStore()
    const content = {
      analysisSummary: '真实分析摘要',
      evidenceRefs: ['attempt evidence'],
      candidates: [
        { strategyCode: 'A', title: '方案 A', rationale: '理由 A', actionDescription: '行动 A' },
        { strategyCode: 'B', title: '方案 B', rationale: '理由 B', actionDescription: '行动 B' },
        { strategyCode: 'C', title: '方案 C', rationale: '理由 C', actionDescription: '行动 C' },
      ],
    }
    const result = await store.captureRecommendation(content, {
      studentId: 'stu-real', courseId: 'course-real', classId: 'class-real', knowledgePointId: 'kp-real',
      demoRunId: 'run-real', demoCaseId: 'case-real', correlationId: 'corr-real',
    })
    await flushPromises()
    expect(result?.recommendationId).toBe('rec-real-1')
    expect(store.captureState).toBe('SUCCESS')
    expect(captureAnalysisRecommendation).toHaveBeenCalledWith(content, expect.objectContaining({ demoRunId: 'run-real' }), expect.stringMatching(/^smartbi-capture-/))
  })
})
