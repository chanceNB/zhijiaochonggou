import { describe, expect, it } from 'vitest'
import { router } from '../router'
import RoutePlaceholderView from '../views/RoutePlaceholderView.vue'

describe('F04 SmartBI frontend RED gate', () => {
  it('replaces the teacher analytics route placeholder with the real center page', () => {
    const route = router.getRoutes().find((item) => item.path === '/teacher/analytics')
    expect(route?.components?.default).not.toBe(RoutePlaceholderView)
  })

  it('replaces the teacher SmartBI asset route placeholder with the real asset page', () => {
    const route = router.getRoutes().find((item) => item.path === '/teacher/analytics/:assetKey')
    expect(route?.components?.default).not.toBe(RoutePlaceholderView)
  })
})
