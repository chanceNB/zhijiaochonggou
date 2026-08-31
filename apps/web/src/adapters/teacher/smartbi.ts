import { RecommendationCaptureContentSchema, type RecommendationCaptureContent, type SmartBiAssetDto, type SmartBiFreshnessDto } from '@/types/contracts/smartbi'

export type SmartBiAssetStatus = SmartBiAssetDto['status']
export type SmartBiLaunchMode = SmartBiAssetDto['launchMode']

export type SmartBiAssetVm = {
  assetKey: string
  type: string
  displayName: string
  status: SmartBiAssetStatus
  launchMode: SmartBiLaunchMode
  resourceUrl: string | null
}

export type SmartBiFreshnessVm = SmartBiFreshnessDto & {
  label: string
}

export function adaptSmartBiAsset(dto: SmartBiAssetDto): SmartBiAssetVm {
  return {
    assetKey: dto.assetKey,
    type: dto.type,
    displayName: dto.displayName,
    status: dto.status,
    launchMode: dto.launchMode,
    resourceUrl: dto.resourceUrl ?? null,
  }
}

export function adaptSmartBiFreshness(dto: SmartBiFreshnessDto): SmartBiFreshnessVm {
  const label = dto.status === 'FRESH' ? '数据新鲜' : dto.status === 'STALE' ? '数据可能滞后' : '暂无分析数据'
  return { ...dto, label }
}

export function parseRecommendationJson(value: string): { content: RecommendationCaptureContent } | { errors: string[] } {
  let parsed: unknown
  try {
    parsed = JSON.parse(value)
  } catch {
    return { errors: ['JSON 格式不正确，请检查括号和引号。'] }
  }
  const result = RecommendationCaptureContentSchema.safeParse(parsed)
  return result.success
    ? { content: result.data }
    : { errors: result.error.issues.map((issue) => `${issue.path.join('.') || '内容'}：${issue.message}`) }
}
