import { defineStore } from 'pinia'
import { getTeacherDiagnosis, getTeacherProfile, getTeacherWorkbench } from '@/api/teacher'
import { toApiError } from '@/api/client'
import type { TeacherDiagnosisDto, TeacherProfileDto, TeacherWorkbenchDto } from '@/types/contracts/teacher'

type LoadState = 'INITIAL' | 'LOADING' | 'READY' | 'EMPTY' | 'FORBIDDEN' | 'NOT_FOUND' | 'ERROR'

export const useTeacherStore = defineStore('teacherReadModel', {
  state: () => ({
    workbenchState: 'INITIAL' as LoadState,
    workbench: null as TeacherWorkbenchDto | null,
    profileState: 'INITIAL' as LoadState,
    profile: null as TeacherProfileDto | null,
    diagnosisState: 'INITIAL' as LoadState,
    diagnosis: null as TeacherDiagnosisDto | null,
    error: null as string | null,
  }),
  actions: {
    classify(code: string) { return code === 'FORBIDDEN' ? 'FORBIDDEN' : code === 'RESOURCE_NOT_FOUND' ? 'NOT_FOUND' : 'ERROR' as LoadState },
    async loadWorkbench(force = false) {
      if (!force && (this.workbenchState === 'LOADING' || this.workbenchState === 'READY' || this.workbenchState === 'EMPTY')) return this.workbench
      this.workbenchState = 'LOADING'; this.error = null
      try { this.workbench = await getTeacherWorkbench(); this.workbenchState = this.workbench.currentStudent ? 'READY' : 'EMPTY'; return this.workbench }
      catch (error) { const apiError = toApiError(error); this.workbenchState = this.classify(apiError.code); this.error = apiError.message; return null }
    },
    async loadProfile(studentId: string, courseId: string, force = false) {
      if (!force && (this.profileState === 'LOADING' || this.profileState === 'READY')) return this.profile
      this.profileState = 'LOADING'; this.error = null
      try { this.profile = await getTeacherProfile(studentId, courseId); this.profileState = 'READY'; return this.profile }
      catch (error) { const apiError = toApiError(error); this.profileState = this.classify(apiError.code); this.error = apiError.message; return null }
    },
    async loadDiagnosis(caseId: string, force = false) {
      if (!force && (this.diagnosisState === 'LOADING' || this.diagnosisState === 'READY')) return this.diagnosis
      this.diagnosisState = 'LOADING'; this.error = null
      try { this.diagnosis = await getTeacherDiagnosis(caseId); this.diagnosisState = 'READY'; return this.diagnosis }
      catch (error) { const apiError = toApiError(error); this.diagnosisState = this.classify(apiError.code); this.error = apiError.message; return null }
    },
  },
})
