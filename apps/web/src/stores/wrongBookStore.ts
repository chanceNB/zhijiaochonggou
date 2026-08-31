import { defineStore } from 'pinia'
import { toApiError } from '@/api/client'
import { getWrongBook, reviewWrongBookItem } from '@/api/student/wrongBook'
import { toWrongBookPageVm, type WrongBookItemVm, type WrongBookPageVm } from '@/adapters/student/practice'
import type { StudentUiState } from '@/types/contracts/student'

export const useWrongBookStore = defineStore('wrongBook', {
  state: () => ({
    state: 'INITIAL' as StudentUiState,
    data: null as WrongBookPageVm | null,
    error: null as string | null,
    selectedWrongItemId: null as string | null,
    actionFeedback: null as string | null,
    lastReviewCorrect: null as boolean | null,
    reviewing: false,
  }),
  getters: {
    items: (store) => store.data?.items ?? [],
    selectedItem: (store): WrongBookItemVm | null => {
      const items = store.data?.items ?? []
      return items.find((item) => item.wrongItemId === store.selectedWrongItemId) ?? items[0] ?? null
    },
  },
  actions: {
    async load(force = false) {
      if (!force && this.state === 'READY') return this.data
      this.state = 'LOADING'
      this.error = null
      try {
        this.data = toWrongBookPageVm(await getWrongBook({ page: 1, size: 50 }))
        this.selectedWrongItemId = this.selectedWrongItemId && this.data.items.some((item) => item.wrongItemId === this.selectedWrongItemId)
          ? this.selectedWrongItemId : this.data.items[0]?.wrongItemId ?? null
        this.state = this.data.items.length ? 'READY' : 'EMPTY'
        return this.data
      } catch (error) {
        this.error = toApiError(error).message || '错题本暂时无法加载'
        this.state = 'ERROR'
        return null
      }
    },
    select(wrongItemId: string) {
      this.selectedWrongItemId = wrongItemId
    },
    focusItem(wrongItemId: string) {
      this.selectedWrongItemId = wrongItemId
    },
    async review(answer: string, durationSeconds = 10) {
      const item = this.selectedItem
      if (!item || this.reviewing) return null
      this.reviewing = true
      this.actionFeedback = null
      this.lastReviewCorrect = null
      try {
        const result = await reviewWrongBookItem({ wrongItemId: item.wrongItemId, answer, durationSeconds })
        this.lastReviewCorrect = result.correct
        this.actionFeedback = result.correct ? '✓ 回答正确\n本题已掌握' : '✕ 回答错误\n仍需继续复习'
        await this.load(true)
        return result
      } catch (error) {
        this.actionFeedback = toApiError(error).message || '复习提交失败，请重试'
        return null
      } finally {
        this.reviewing = false
      }
    },
  },
})
