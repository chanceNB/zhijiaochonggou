import { describe, expect, it } from 'vitest'
import { router } from '../router'

describe('router foundation', () => {
  it('resolves all frozen student and teacher shell routes', () => {
    const routes = router.getRoutes().map((route) => route.path)

    expect(routes).toEqual(expect.arrayContaining([
      '/',
      '/student/today',
      '/student/ai-coach',
      '/student/ai-coach/:sessionId',
      '/student/practice',
      '/student/practice/:practiceSetId',
      '/student/practice/:practiceSetId/result',
      '/student/wrong-book',
      '/student/growth',
      '/student/resources',
      '/teacher/workbench',
      '/teacher/analytics',
      '/teacher/analytics/:assetKey',
      '/teacher/interventions',
      '/teacher/interventions/:interventionId',
      '/teacher/students/:studentId',
      '/teacher/diagnosis-cases/:caseId',
      '/teacher/resources',
    ]))
  })

  it('redirects role roots to their primary shell pages', async () => {
    await router.push('/student')
    expect(router.currentRoute.value.fullPath).toBe('/student/today')

    await router.push('/teacher')
    expect(router.currentRoute.value.fullPath).toBe('/teacher/workbench')
  })
})
