import { beforeEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { router } from '../router'
import StudentSidebar from '../components/StudentSidebar.vue'
import TeacherSidebar from '../components/TeacherSidebar.vue'

describe('StudentSidebar', () => {
  beforeEach(async () => {
    await router.push('/student/today')
  })

  it('renders the six frozen student navigation entries', () => {
    const wrapper = mount(StudentSidebar, {
      props: { collapsed: false },
      global: { plugins: [router] },
    })

    expect(wrapper.findAll('[data-testid="student-nav-link"]')).toHaveLength(6)
    expect(wrapper.text()).toContain('今日学习')
    expect(wrapper.text()).toContain('AI学习教练')
    expect(wrapper.text()).toContain('定向练习')
    expect(wrapper.text()).toContain('错题本')
    expect(wrapper.text()).toContain('我的成长')
    expect(wrapper.text()).toContain('学习资料')
  })

  it('marks the matching route active and emits collapse', async () => {
    await router.push('/student/practice/example-set')
    const wrapper = mount(StudentSidebar, {
      props: { collapsed: false },
      global: { plugins: [router] },
    })

    expect(wrapper.find('[data-testid="student-nav-link"]').exists()).toBe(true)
    expect(wrapper.findAll('.is-active')).toHaveLength(1)
    expect(wrapper.find('.is-active').text()).toContain('定向练习')

    await wrapper.get('[data-testid="student-sidebar-toggle"]').trigger('click')
    expect(wrapper.emitted('toggle')).toHaveLength(1)
  })
})

describe('TeacherSidebar', () => {
  beforeEach(async () => {
    await router.push('/teacher/workbench')
  })

  it('renders the five frozen teacher navigation entries', () => {
    const wrapper = mount(TeacherSidebar, {
      props: { collapsed: false },
      global: { plugins: [router] },
    })

    expect(wrapper.findAll('[data-testid="teacher-nav-link"]')).toHaveLength(5)
    expect(wrapper.text()).toContain('工作台')
    expect(wrapper.text()).toContain('数据洞察')
    expect(wrapper.text()).toContain('干预决策')
    expect(wrapper.text()).toContain('干预结果')
    expect(wrapper.text()).toContain('课程资源')
  })

  it('keeps decision and outcome links as separate contexts', async () => {
    const wrapper = mount(TeacherSidebar, {
      props: { collapsed: false },
      global: { plugins: [router] },
    })

    expect(wrapper.findAll('.is-active')).toHaveLength(1)
    expect(wrapper.find('.is-active').text()).toContain('工作台')

    await router.push('/teacher/interventions?section=outcome')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.is-active').text()).toContain('干预结果')

    await router.push('/teacher/interventions/int-1')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.is-active').text()).toContain('干预结果')

    await router.push('/teacher/interventions?recommendationId=rec-1')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.is-active').text()).toContain('干预决策')
  })

  it('emits collapse and exposes an accessible toggle', async () => {
    const wrapper = mount(TeacherSidebar, {
      props: { collapsed: false },
      global: { plugins: [router] },
    })

    const toggle = wrapper.get('[data-testid="teacher-sidebar-toggle"]')
    expect(toggle.attributes('aria-expanded')).toBe('true')
    await toggle.trigger('click')
    expect(wrapper.emitted('toggle')).toHaveLength(1)
  })
})
