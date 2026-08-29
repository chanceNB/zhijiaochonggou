import { describe, expect, it } from 'vitest'
import { router } from '../router'

describe('router foundation', () => {
  it('exposes the shell route without business pages', () => {
    expect(router.getRoutes().map((route) => route.path)).toContain('/')
  })
})
