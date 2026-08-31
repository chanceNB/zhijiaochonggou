import { apiClient, parseApiEnvelope } from '@/api/client'
import {
  RecommendationCaptureContentSchema,
  RecommendationCaptureResponseSchema,
  RecommendationSnapshotSchema,
  SmartBiAssetListSchema,
  SmartBiAssetSchema,
  SmartBiFreshnessSchema,
  type RecommendationCaptureContent,
  type RecommendationCaptureResponse,
  type RecommendationSnapshot,
  type SmartBiAssetDto,
  type SmartBiFreshnessDto,
} from '@/types/contracts/smartbi'

export async function getSmartBiAssets(): Promise<SmartBiAssetDto[]> {
  const response = await apiClient.get('/integrations/smartbi/assets')
  return SmartBiAssetListSchema.parse(parseApiEnvelope(response.data).data).assets
}

export async function getSmartBiAsset(assetKey: string): Promise<SmartBiAssetDto> {
  const response = await apiClient.get(`/integrations/smartbi/assets/${encodeURIComponent(assetKey)}`)
  return SmartBiAssetSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getSmartBiFreshness(): Promise<SmartBiFreshnessDto> {
  const response = await apiClient.get('/analytics/smartbi/freshness')
  return SmartBiFreshnessSchema.parse(parseApiEnvelope(response.data).data)
}

export async function captureAnalysisRecommendation(
  content: RecommendationCaptureContent,
  context: { studentId: string; courseId: string; classId: string; knowledgePointId: string; demoRunId?: string | null; demoCaseId?: string | null; correlationId?: string | null },
  idempotencyKey: string,
): Promise<RecommendationCaptureResponse> {
  const body = {
    ...RecommendationCaptureContentSchema.parse(content),
    ...context,
    source: 'SMARTBI_AICHAT' as const,
  }
  const response = await apiClient.post('/teacher/analysis-recommendations', body, {
    headers: { 'Idempotency-Key': idempotencyKey },
  })
  return RecommendationCaptureResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getAnalysisRecommendation(recommendationId: string): Promise<RecommendationSnapshot> {
  const response = await apiClient.get(`/teacher/analysis-recommendations/${encodeURIComponent(recommendationId)}`)
  return RecommendationSnapshotSchema.parse(parseApiEnvelope(response.data).data)
}
