import { defineStore } from 'pinia'
import { getStudentToday } from '@/api/student/today'
import { toApiError } from '@/api/client'
import { toTodayVm } from '@/adapters/student/today'
import type { StudentUiState, TodayVm } from '@/types/contracts/student'

const defaultCourseId = () => import.meta.env.VITE_DEFAULT_COURSE_ID ?? 'course-data-structures'
const defaultStudentId = () => import.meta.env.VITE_DEFAULT_STUDENT_ID ?? 'stu-xiaoming'

export const useStudentContextStore = defineStore('studentContext', {
  state: () => ({
    state: 'INITIAL' as StudentUiState,
    data: null as TodayVm | null,
    error: null as string | null,
    errorCode: null as string | null,
  }),
  getters: {
    studentId: (store) => store.data?.studentId ?? defaultStudentId(),
    courseId: () => defaultCourseId(),
    knowledgePointId: (store) => store.data?.nextAction.knowledgePointId ?? null,
  },
  actions: {
    async load(options: { force?: boolean } = {}) {
      if (!options.force && (this.state === 'LOADING' || this.state === 'READY')) return this.data
      this.state = 'LOADING'
      this.error = null
      this.errorCode = null
      try {
        const dto = await getStudentToday({ studentId: defaultStudentId(), courseId: defaultCourseId() })
        this.data = toTodayVm(dto)
        this.state = 'READY'
        return this.data
      } catch (error) {
        const apiError = toApiError(error)
        this.state = apiError.code === 'FORBIDDEN' ? 'FORBIDDEN' : 'ERROR'
        this.error = apiError.code === 'FORBIDDEN' ? '当前账号没有访问该资源的权限' : '今日学习暂时无法加载，请稍后重试'
        this.errorCode = apiError.code
        return null
      }
    },
  },
})
