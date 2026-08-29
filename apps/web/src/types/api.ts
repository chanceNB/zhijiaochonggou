import { z } from 'zod'

export const ApiErrorDetailsSchema = z.record(z.string(), z.unknown()).nullable().optional()

export const ApiEnvelopeSchema = z.object({
  code: z.string().min(1),
  message: z.string(),
  requestId: z.string().min(1),
  data: z.unknown().nullable(),
  details: ApiErrorDetailsSchema,
  timestamp: z.string().datetime({ offset: true }),
})

export type ApiEnvelope<T> = Omit<z.infer<typeof ApiEnvelopeSchema>, 'data'> & { data: T }

export type ApiErrorModel = ApiEnvelope<null>
