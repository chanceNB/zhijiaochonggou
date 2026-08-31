import axios, { type AxiosError, type AxiosInstance } from 'axios'
import { z } from 'zod'
import { ApiEnvelopeSchema, type ApiEnvelope, type ApiErrorModel } from '@/types/api'

export const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  timeout: 10_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

export function parseApiEnvelope<T>(payload: unknown): ApiEnvelope<T> {
  return ApiEnvelopeSchema.parse(payload) as ApiEnvelope<T>
}

export function toApiError(error: unknown): ApiErrorModel {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<unknown>
    const parsed = ApiEnvelopeSchema.safeParse(axiosError.response?.data)
    if (parsed.success) {
      return parsed.data as ApiErrorModel
    }
  }

  if (error && typeof error === 'object' && 'code' in error && typeof (error as { code?: unknown }).code === 'string') {
    const candidate = error as Partial<ApiErrorModel>
    return {
      code: candidate.code as string,
      message: typeof candidate.message === 'string' ? candidate.message : '请求失败',
      requestId: typeof candidate.requestId === 'string' ? candidate.requestId : 'unknown',
      data: candidate.data ?? null,
      timestamp: typeof candidate.timestamp === 'string' ? candidate.timestamp : new Date().toISOString(),
    }
  }

  const message = error instanceof Error ? error.message : '网络请求失败'
  return {
    code: 'UPSTREAM_ERROR',
    message,
    requestId: 'unknown',
    data: null,
    timestamp: new Date().toISOString(),
  }
}

export async function getHealth(): Promise<ApiEnvelope<{ status: string; service: string }>> {
  const response = await apiClient.get('/health')
  return parseApiEnvelope(response.data)
}

export const isApiEnvelope = (payload: unknown): payload is ApiEnvelope<unknown> =>
  ApiEnvelopeSchema.safeParse(payload).success

export const isZodError = (error: unknown): error is z.ZodError => error instanceof z.ZodError
