import { z } from 'zod'

const Timestamp = z.string().datetime({ offset: true })

export const SmartBiAssetSchema = z.object({
  assetKey: z.string().min(1),
  type: z.string().min(1),
  displayName: z.string().min(1),
  status: z.enum(['VERIFIED', 'PLATFORM_PENDING', 'FORBIDDEN', 'DEGRADED', 'ERROR']),
  launchMode: z.enum(['UNVERIFIED', 'NEW_TAB', 'IFRAME']),
  resourceUrl: z.string().url().nullable().optional(),
})

export const SmartBiAssetListSchema = z.object({
  assets: z.array(SmartBiAssetSchema),
})

export const SmartBiFreshnessItemSchema = z.object({
  datasetKey: z.string().min(1),
  latestSourceEventTime: Timestamp.nullable().optional(),
  latestProjectionTime: Timestamp.nullable().optional(),
  observedAt: Timestamp,
  rowCount: z.number().int().nonnegative(),
  sourceVersion: z.string().min(1),
})

export const SmartBiFreshnessSchema = z.object({
  items: z.array(SmartBiFreshnessItemSchema),
  lastBusinessEventAt: Timestamp.nullable().optional(),
  lastProjectedAt: Timestamp.nullable().optional(),
  lagSeconds: z.number().int().nonnegative(),
  status: z.enum(['NO_DATA', 'FRESH', 'STALE']),
})

export const RecommendationCandidateSchema = z.object({
  strategyCode: z.string().min(1),
  title: z.string().min(1),
  rationale: z.string().min(1),
  actionDescription: z.string().min(1),
})

export const RecommendationCaptureContentSchema = z.object({
  analysisSummary: z.string().trim().min(1, '请填写分析摘要'),
  evidenceRefs: z.array(z.string().trim().min(1)).default([]),
  candidates: z.array(RecommendationCandidateSchema).length(3, '必须填写 3 个候选方案'),
})

export const RecommendationCaptureResponseSchema = z.object({
  recommendationId: z.string().min(1),
  status: z.string().min(1),
})

export const RecommendationSnapshotSchema = z.object({
  recommendationId: z.string().min(1),
  analysisSummary: z.string().optional(),
  evidenceRefs: z.array(z.string()).optional(),
  candidates: z.array(RecommendationCandidateSchema).length(3),
  source: z.literal('SMARTBI_AICHAT'),
  captureMode: z.literal('MANUAL'),
  status: z.literal('PENDING_TEACHER_REVIEW'),
})

export type SmartBiAssetDto = z.infer<typeof SmartBiAssetSchema>
export type SmartBiAssetListDto = z.infer<typeof SmartBiAssetListSchema>
export type SmartBiFreshnessDto = z.infer<typeof SmartBiFreshnessSchema>
export type RecommendationCandidate = z.infer<typeof RecommendationCandidateSchema>
export type RecommendationCaptureContent = z.infer<typeof RecommendationCaptureContentSchema>
export type RecommendationCaptureResponse = z.infer<typeof RecommendationCaptureResponseSchema>
export type RecommendationSnapshot = z.infer<typeof RecommendationSnapshotSchema>
