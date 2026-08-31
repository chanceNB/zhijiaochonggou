import { defineStore } from 'pinia'
import { toApiError } from '@/api/client'
import {
  approveIntervention,
  commitIntervention,
  getAnalysisRecommendation,
  getIntervention,
  getInterventionByRecommendation,
  getInterventionOutcome,
  proposeIntervention,
} from '@/api/teacher'
import type {
  InterventionOutcomeDto,
  InterventionResponseDto,
  RecommendationSnapshotDto,
} from '@/types/contracts/teacher'

type LoadState = 'INITIAL' | 'LOADING' | 'READY' | 'EMPTY' | 'ERROR' | 'FORBIDDEN' | 'CONFLICT'

export const useTeacherInterventionStore = defineStore('teacherIntervention', {
  state: () => ({
    recommendationState: 'INITIAL' as LoadState,
    recommendation: null as RecommendationSnapshotDto | null,
    interventionState: 'INITIAL' as LoadState,
    intervention: null as InterventionResponseDto | null,
    outcomeState: 'INITIAL' as LoadState,
    outcome: null as InterventionOutcomeDto | null,
    error: null as string | null,
    errorCode: null as string | null,
  }),
  actions: {
    clearError() { this.error = null; this.errorCode = null },
    classify(code: string): LoadState {
      if (code === 'FORBIDDEN') return 'FORBIDDEN'
      if (code === 'PRECONDITION_FAILED' || code === 'STATE_CONFLICT') return 'CONFLICT'
      return 'ERROR'
    },
    async loadRecommendation(recommendationId: string, force = false) {
      if (!recommendationId) { this.recommendationState = 'EMPTY'; return null }
      if (!force && this.recommendation?.recommendationId === recommendationId && this.recommendationState === 'READY') return this.recommendation
      this.recommendationState = 'LOADING'; this.clearError()
      try {
        const recommendation = await getAnalysisRecommendation(recommendationId)
        if (recommendation.candidates.length !== 3) {
          this.recommendation = null
          this.recommendationState = 'ERROR'
          this.error = `分析建议候选方案数量不符合约定（当前 ${recommendation.candidates.length} 个，应为 3 个）`
          this.errorCode = 'CONTRACT_VALIDATION_ERROR'
          return null
        }
        this.recommendation = recommendation
        this.recommendationState = 'READY'
        return this.recommendation
      } catch (error) {
        const apiError = toApiError(error)
        if (apiError.code === 'RESOURCE_NOT_FOUND') {
          this.recommendation = null
          this.recommendationState = 'EMPTY'
          this.clearError()
          return null
        }
        this.recommendation = null
        this.recommendationState = this.classify(apiError.code)
        this.errorCode = apiError.code
        this.error = apiError.code === 'UPSTREAM_ERROR' && /candidate/i.test(apiError.message)
          ? '分析建议候选方案未通过数据约束校验，请重新记录三个候选方案'
          : '分析建议暂时无法加载，请刷新后重试'
        return null
      }
    },
    async propose(body: { recommendationId: string; strategyCode: string; teacherRationale: string }) {
      this.interventionState = 'LOADING'; this.clearError()
      try { this.intervention = await proposeIntervention(body); this.interventionState = 'READY'; return this.intervention }
      catch (error) { const apiError = toApiError(error); this.interventionState = this.classify(apiError.code); this.errorCode = apiError.code; this.error = apiError.code === 'DOMAIN_RULE_VIOLATION' ? '当前建议已发生变化，请重新选择。' : '创建干预方案失败，请重试'; return null }
    },
    async approve() {
      if (!this.intervention) return null
      this.interventionState = 'LOADING'; this.clearError()
      try { this.intervention = await approveIntervention(this.intervention.interventionId, this.intervention.version); this.interventionState = 'READY'; return this.intervention }
      catch (error) { const apiError = toApiError(error); this.interventionState = this.classify(apiError.code); this.errorCode = apiError.code; this.error = apiError.code === 'PRECONDITION_FAILED' ? '服务器状态已更新，请刷新当前干预。' : '审核干预方案失败，请重试'; return null }
    },
    async commit(dueAt?: string | null) {
      if (!this.intervention) return null
      this.interventionState = 'LOADING'; this.clearError()
      try {
        this.intervention = dueAt === undefined
          ? await commitIntervention(this.intervention.interventionId, this.intervention.version)
          : await commitIntervention(this.intervention.interventionId, this.intervention.version, dueAt)
        this.interventionState = 'READY'; return this.intervention
      }
      catch (error) { const apiError = toApiError(error); this.interventionState = this.classify(apiError.code); this.errorCode = apiError.code; this.error = apiError.code === 'PRECONDITION_FAILED' ? '服务器状态已更新，请刷新当前干预。' : '下发干预任务失败，请重试'; return null }
    },
    async loadIntervention(interventionId: string, force = false) {
      if (!force && this.intervention?.interventionId === interventionId && this.interventionState === 'READY') return this.intervention
      this.interventionState = 'LOADING'; this.clearError()
      try { this.intervention = await getIntervention(interventionId); this.interventionState = 'READY'; return this.intervention }
      catch (error) { const apiError = toApiError(error); this.interventionState = this.classify(apiError.code); this.errorCode = apiError.code; this.error = '干预记录暂时无法加载，请重试'; return null }
    },
    async loadInterventionForRecommendation(recommendationId: string, force = false) {
      if (!recommendationId) return null
      if (!force && this.intervention?.recommendationId === recommendationId && this.interventionState === 'READY') return this.intervention
      this.interventionState = 'LOADING'; this.clearError()
      try {
        this.intervention = await getInterventionByRecommendation(recommendationId)
        this.interventionState = 'READY'; return this.intervention
      } catch (error) {
        const apiError = toApiError(error)
        if (apiError.code === 'RESOURCE_NOT_FOUND') { this.intervention = null; this.interventionState = 'INITIAL'; this.clearError(); return null }
        this.interventionState = this.classify(apiError.code); this.errorCode = apiError.code; this.error = '干预记录暂时无法加载，请重试'; return null
      }
    },
    async loadOutcome(interventionId: string, force = false) {
      if (!force && this.outcome?.interventionId === interventionId && this.outcomeState === 'READY') return this.outcome
      this.outcomeState = 'LOADING'; this.clearError()
      try { this.outcome = await getInterventionOutcome(interventionId); this.outcomeState = 'READY'; return this.outcome }
      catch (error) {
        const apiError = toApiError(error); this.outcome = null
        this.outcomeState = apiError.code === 'RESOURCE_NOT_FOUND' ? 'EMPTY' : this.classify(apiError.code)
        this.errorCode = apiError.code; this.error = apiError.code === 'RESOURCE_NOT_FOUND' ? null : '干预结果暂时无法加载，请重试'; return null
      }
    },
  },
})
