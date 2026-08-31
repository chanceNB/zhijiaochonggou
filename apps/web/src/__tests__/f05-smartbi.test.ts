import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SmartBiEmbedPanel from '../components/teacher/SmartBiEmbedPanel.vue'
import TeacherSmartBiCenterPage from '../views/TeacherSmartBiCenterPage.vue'
import { adaptSmartBiAsset } from '../adapters/teacher/smartbi'
import { getSmartBiAssets, getSmartBiFreshness } from '../api/smartbi'
import { getTeacherWorkbench } from '../api/teacher'
import { router } from '../router'

vi.mock('../api/smartbi', () => ({
  getSmartBiAssets: vi.fn(),
  getSmartBiAsset: vi.fn(),
  getSmartBiFreshness: vi.fn(),
  captureAnalysisRecommendation: vi.fn(),
  getAnalysisRecommendation: vi.fn(),
}))

vi.mock('../api/teacher', () => ({
  getTeacherWorkbench: vi.fn(),
  getTeacherProfile: vi.fn(),
  getTeacherDiagnosis: vi.fn(),
}))

const aiChatAsset = {
  assetKey: 'aichat',
  type: 'AICHAT',
  displayName: 'AI 分析助手',
  status: 'VERIFIED' as const,
  launchMode: 'IFRAME' as const,
  resourceUrl: 'https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/aichat/proxy/#/canvas/chat',
}

describe('F05 real SmartBI platform assets', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(getTeacherWorkbench).mockResolvedValue({ currentStudent: null, priorityItems: [], pendingRecommendations: [], pendingOutcomes: [] })
    vi.mocked(getSmartBiAssets).mockResolvedValue([aiChatAsset])
    vi.mocked(getSmartBiFreshness).mockResolvedValue({ items: [], lagSeconds: 0, status: 'NO_DATA' })
  })

  it('adapts the backend AIChat asset without introducing a frontend URL', () => {
    expect(adaptSmartBiAsset(aiChatAsset)).toEqual(aiChatAsset)
  })

  it('renders the configured AIChat asset as a real iframe', () => {
    const wrapper = mount(SmartBiEmbedPanel, { props: { asset: aiChatAsset } })
    expect(wrapper.get('[data-testid="smartbi-state-iframe"] iframe').attributes('src')).toBe(aiChatAsset.resourceUrl)
    expect(wrapper.get('[data-testid="smartbi-state-iframe"] iframe').attributes('title')).toBe('AI 分析助手')
  })

  it('binds the teacher analytics AIChat panel to the Asset API response', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const wrapper = mount(TeacherSmartBiCenterPage, { global: { plugins: [pinia, router] } })
    await flushPromises()
    const aiSection = wrapper.get('[data-testid="aichat-section"]')
    expect(aiSection.get('[data-testid="smartbi-state-iframe"] iframe').attributes('src')).toBe(aiChatAsset.resourceUrl)
    expect(aiSection.text()).toContain('已连接')
  })
})
