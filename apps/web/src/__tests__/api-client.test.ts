import { describe, expect, it } from 'vitest'
import { parseApiEnvelope } from '../api/client'

describe('API client contract', () => {
  it('parses the unified envelope', () => {
    const envelope = parseApiEnvelope({
      code: 'OK',
      message: 'success',
      requestId: 'req-01',
      data: { status: 'UP' },
      timestamp: '2026-08-29T08:00:00Z',
    })

    expect(envelope.code).toBe('OK')
    expect(envelope.data).toEqual({ status: 'UP' })
  })

  it('rejects malformed envelopes', () => {
    expect(() => parseApiEnvelope({ code: 'OK' })).toThrow()
  })
})
