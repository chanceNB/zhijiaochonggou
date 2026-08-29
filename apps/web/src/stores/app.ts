import { defineStore } from 'pinia'
import type { ApiErrorModel } from '@/types/api'

export const useAppStore = defineStore('app', {
  state: () => ({
    lastError: null as ApiErrorModel | null,
  }),
  actions: {
    setError(error: ApiErrorModel) {
      this.lastError = error
    },
    clearError() {
      this.lastError = null
    },
  },
})
