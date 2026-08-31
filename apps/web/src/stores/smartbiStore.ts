import { defineStore } from 'pinia'
import { captureAnalysisRecommendation, getAnalysisRecommendation, getSmartBiAsset, getSmartBiAssets, getSmartBiFreshness } from '@/api/smartbi'
import { toApiError } from '@/api/client'
import { adaptSmartBiAsset, adaptSmartBiFreshness, type SmartBiAssetVm, type SmartBiFreshnessVm } from '@/adapters/teacher/smartbi'
import type { RecommendationCaptureContent, RecommendationCaptureResponse, RecommendationSnapshot } from '@/types/contracts/smartbi'

export type SmartBiLoadState = 'INITIAL' | 'LOADING' | 'READY' | 'EMPTY' | 'STALE' | 'DEGRADED' | 'FORBIDDEN' | 'ERROR'
export type SmartBiCaptureState = 'IDLE' | 'SUBMITTING' | 'SUCCESS' | 'ERROR'

function idempotencyKey(prefix: string) {
  const random = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${prefix}-${random}`
}

export const useSmartBiStore = defineStore('smartbi', {
  state: () => ({
    assetsState: 'INITIAL' as SmartBiLoadState,
    assets: [] as SmartBiAssetVm[],
    assetState: 'INITIAL' as SmartBiLoadState,
    asset: null as SmartBiAssetVm | null,
    freshnessState: 'INITIAL' as SmartBiLoadState,
    freshness: null as SmartBiFreshnessVm | null,
    captureState: 'IDLE' as SmartBiCaptureState,
    capturedRecommendation: null as RecommendationCaptureResponse | null,
    capturedSnapshot: null as RecommendationSnapshot | null,
    error: null as string | null,
  }),
  actions: {
    classify(code: string): SmartBiLoadState {
      if (code === 'FORBIDDEN') return 'FORBIDDEN'
      if (code === 'NO_DATA') return 'EMPTY'
      return 'ERROR'
    },
    async loadAssets(force = false) {
      if (!force && (this.assetsState === 'LOADING' || this.assetsState === 'READY')) return this.assets
      this.assetsState = 'LOADING'
      this.error = null
      try {
        this.assets = (await getSmartBiAssets()).map(adaptSmartBiAsset)
        this.assetsState = this.assets.length ? 'READY' : 'EMPTY'
        return this.assets
      } catch (error) {
        const apiError = toApiError(error)
        this.assetsState = this.classify(apiError.code)
        this.error = apiError.message
        return []
      }
    },
    async loadAsset(assetKey: string, force = false) {
      if (!force && this.asset?.assetKey === assetKey && (this.assetState === 'READY' || this.assetState === 'EMPTY')) return this.asset
      this.assetState = 'LOADING'
      this.error = null
      try {
        this.asset = adaptSmartBiAsset(await getSmartBiAsset(assetKey))
        this.assetState = 'READY'
        return this.asset
      } catch (error) {
        const apiError = toApiError(error)
        this.asset = null
        this.assetState = this.classify(apiError.code)
        this.error = apiError.code === 'RESOURCE_NOT_FOUND' ? '分析资产不存在或尚未开放' : '分析资产暂时无法加载，请稍后重试'
        return null
      }
    },
    async loadFreshness(force = false) {
      if (!force && (this.freshnessState === 'LOADING' || this.freshnessState === 'READY' || this.freshnessState === 'STALE')) return this.freshness
      this.freshnessState = 'LOADING'
      this.error = null
      try {
        this.freshness = adaptSmartBiFreshness(await getSmartBiFreshness())
        this.freshnessState = this.freshness.status === 'STALE' ? 'STALE' : this.freshness.status === 'NO_DATA' ? 'EMPTY' : 'READY'
        return this.freshness
      } catch (error) {
        const apiError = toApiError(error)
        this.freshnessState = 'DEGRADED'
        this.error = apiError.message
        return null
      }
    },
    async loadOverview(force = false) {
      await Promise.all([this.loadAssets(force), this.loadFreshness(force)])
      return { assets: this.assets, freshness: this.freshness }
    },
    async captureRecommendation(content: RecommendationCaptureContent, context: {
      studentId: string
      courseId: string
      classId: string
      knowledgePointId: string
      demoRunId?: string | null
      demoCaseId?: string | null
      correlationId?: string | null
    }) {
      this.captureState = 'SUBMITTING'
      this.error = null
      try {
        this.capturedRecommendation = await captureAnalysisRecommendation(content, context, idempotencyKey('smartbi-capture'))
        this.captureState = 'SUCCESS'
        return this.capturedRecommendation
      } catch (error) {
        const apiError = toApiError(error)
        this.captureState = 'ERROR'
        this.error = apiError.code === 'VALIDATION_ERROR' ? apiError.message : '分析建议暂时无法记录，请检查后重试'
        return null
      }
    },
    async loadRecommendation(recommendationId: string) {
      try {
        this.capturedSnapshot = await getAnalysisRecommendation(recommendationId)
        return this.capturedSnapshot
      } catch {
        this.capturedSnapshot = null
        return null
      }
    },
    clearCapture() {
      this.captureState = 'IDLE'
      this.capturedRecommendation = null
      this.capturedSnapshot = null
      this.error = null
    },
  },
})
