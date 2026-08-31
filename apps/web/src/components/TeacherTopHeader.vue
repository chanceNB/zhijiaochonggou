<template>
  <header class="teacher-top-header" data-testid="teacher-top-header">
    <span class="teacher-top-header__title">{{ title }}</span>
    <div class="teacher-top-header__actions">
      <div class="teacher-top-header__search" role="search" aria-label="搜索教师工作区（暂不可用）">
        <input type="search" aria-label="搜索教师工作区" placeholder="搜索学生、课程、内容..." disabled />
        <span class="teacher-top-header__search-icon" aria-hidden="true">
          <Search aria-hidden="true" />
        </span>
      </div>
      <span class="teacher-top-header__icon-button" aria-label="教师通知（暂不可用）">
        <Bell aria-hidden="true" />
      </span>
      <div class="teacher-top-header__profile" aria-label="当前教师账户">
        <span class="teacher-top-header__avatar" aria-hidden="true">师</span>
        <strong>教师账户</strong>
        <ArrowDown aria-hidden="true" />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown, Bell, Search } from '@element-plus/icons-vue'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? '教师工作台'))
</script>

<style scoped>
.teacher-top-header {
  display: flex;
  min-width: 0;
  min-height: 56px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 0 24px 0 28px;
  border-bottom: 1px solid #e7edf5;
  background: var(--color-card);
}

.teacher-top-header__title {
  overflow: hidden;
  color: var(--color-text);
  font-size: 16px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.teacher-top-header__actions {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.teacher-top-header__search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 30px;
  gap: 4px;
  align-items: center;
  width: 265px;
  min-height: 36px;
  padding: 0 8px 0 12px;
  border: 1px solid var(--color-border-strong);
  border-radius: 8px;
  background: var(--color-card);
}

.teacher-top-header__search input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: var(--color-text);
  background: transparent;
  font: inherit;
  font-size: 13px;
}

.teacher-top-header__search input::placeholder {
  color: var(--color-secondary);
}

.teacher-top-header__search-icon,
.teacher-top-header__icon-button,
.teacher-top-header__profile {
  border: 0;
  background: transparent;
  cursor: default;
}

.teacher-top-header__search-icon {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: #40516d;
}

.teacher-top-header__icon-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  color: #40516d;
  font-size: 20px;
}

.teacher-top-header__profile {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 0;
  color: var(--color-text);
}

.teacher-top-header__profile strong {
  font-size: 14px;
  white-space: nowrap;
}

.teacher-top-header__profile > svg {
  width: 14px;
  color: #60708b;
}

.teacher-top-header__avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 2px solid #dbeafe;
  border-radius: 50%;
  color: #214c9a;
  background: #eaf2ff;
  font-size: 14px;
  font-weight: 800;
}

@media (max-width: 1080px) {
  .teacher-top-header__search {
    width: 200px;
  }

  .teacher-top-header__profile strong {
    display: none;
  }
}

@media (max-width: 760px) {
  .teacher-top-header {
    gap: 8px;
    padding-inline: 10px;
  }

  .teacher-top-header__actions {
    gap: 8px;
  }

  .teacher-top-header__search {
    width: min(180px, 45vw);
  }
}
</style>
