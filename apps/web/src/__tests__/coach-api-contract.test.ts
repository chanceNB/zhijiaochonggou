import { beforeEach, describe, expect, it, vi } from 'vitest'

const { post } = vi.hoisted(() => ({ post: vi.fn() }))

vi.mock('../api/client', () => ({
  apiClient: { post },
  parseApiEnvelope: (payload: unknown) => payload,
}))

import { sendCoachMessage } from '../api/student/coach'

describe('Coach message command contract', () => {
  beforeEach(() => {
    post.mockReset()
    post.mockResolvedValue({
      data: {
        code: 'OK', message: 'success', requestId: 'req-1',
        data: { assistantMessage: '回答', citations: [], actions: [], ragStatus: 'EMPTY' },
        timestamp: '2026-08-31T00:00:00Z',
      },
    })
  })

  it('sends a fresh Idempotency-Key for every message command', async () => {
    await sendCoachMessage('session-1', '为什么 BFS 要使用队列？')
    await sendCoachMessage('session-1', '请再解释一次')

    const firstKey = post.mock.calls[0][2].headers['Idempotency-Key']
    const secondKey = post.mock.calls[1][2].headers['Idempotency-Key']
    expect(firstKey).toMatch(/^coach-message-/)
    expect(secondKey).toMatch(/^coach-message-/)
    expect(secondKey).not.toBe(firstKey)
  })
})
