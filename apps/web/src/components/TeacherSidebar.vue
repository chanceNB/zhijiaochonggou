<template>
  <aside
    class="teacher-sidebar"
    :class="{ 'is-collapsed': collapsed }"
    data-testid="teacher-sidebar"
    aria-label="教师端导航"
  >
    <RouterLink class="teacher-sidebar__brand" to="/teacher/workbench" aria-label="教师工作台">
      <span class="teacher-sidebar__brand-mark" aria-hidden="true"><i></i><i></i></span>
      <strong>教师工作台</strong>
    </RouterLink>

    <nav class="teacher-sidebar__nav" data-testid="teacher-sidebar-nav">
      <RouterLink
        v-for="item in items"
        :key="item.key"
        class="teacher-sidebar__link"
        :class="{ 'is-active': isActive(item.key) }"
        :to="item.to"
        data-testid="teacher-nav-link"
        :aria-current="isActive(item.key) ? 'page' : undefined"
        :title="collapsed ? item.label : undefined"
      >
        <component :is="item.icon" class="teacher-sidebar__icon" aria-hidden="true" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>

    <button
      class="teacher-sidebar__toggle"
      data-testid="teacher-sidebar-toggle"
      type="button"
      :aria-expanded="!collapsed"
      :aria-label="collapsed ? '展开教师端导航' : '收起教师端导航'"
      @click="emit('toggle')"
    >
      <Expand v-if="collapsed" aria-hidden="true" />
      <Fold v-else aria-hidden="true" />
      <span>收起导航</span>
    </button>
  </aside>
</template>

<script setup lang="ts">
import { toRef, type Component } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  DataAnalysis,
  Expand,
  Fold,
  FolderOpened,
  List,
  Odometer,
} from '@element-plus/icons-vue'

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const route = useRoute()
const items = [
  { key: 'workbench', label: '工作台', to: '/teacher/workbench', icon: Odometer },
  { key: 'analytics', label: '数据洞察', to: '/teacher/analytics', icon: DataAnalysis },
  { key: 'decision', label: '干预决策', to: '/teacher/interventions', icon: List },
  { key: 'outcome', label: '干预结果', to: { path: '/teacher/interventions', query: { section: 'outcome' } }, icon: DataAnalysis },
  { key: 'resources', label: '课程资源', to: '/teacher/resources', icon: FolderOpened },
] satisfies Array<{ key: string; label: string; to: string | { path: string; query: Record<string, string> }; icon: Component }>

const isActive = (key: string) => {
  if (key === 'workbench') return route.path === '/teacher/workbench'
  if (key === 'analytics') return route.path.startsWith('/teacher/analytics')
  if (key === 'resources') return route.path.startsWith('/teacher/resources')
  if (key === 'outcome') return route.path === '/teacher/interventions' && route.query.section === 'outcome'
  return route.path.startsWith('/teacher/interventions') && route.query.section !== 'outcome'
}

const collapsed = toRef(props, 'collapsed')
</script>

<style scoped>
.teacher-sidebar {
  position: sticky;
  top: 0;
  z-index: 2;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 18px;
  width: var(--teacher-sidebar-width);
  height: 100vh;
  padding: 0 14px 18px;
  border-right: 1px solid var(--color-border);
  background: var(--color-card);
  transition: width 180ms ease, padding 180ms ease;
}

.teacher-sidebar__brand {
  display: inline-flex;
  min-height: 74px;
  align-items: center;
  gap: 11px;
  padding: 0 10px;
  border-bottom: 1px solid #edf1f7;
  color: #17233c;
  text-decoration: none;
  white-space: nowrap;
}

.teacher-sidebar__brand strong {
  overflow: hidden;
  font-size: 20px;
  font-weight: 800;
  text-overflow: ellipsis;
}

.teacher-sidebar__brand-mark {
  position: relative;
  display: inline-block;
  width: 29px;
  height: 29px;
  flex: 0 0 auto;
}

.teacher-sidebar__brand-mark::after {
  position: absolute;
  inset: 3px 4px 3px 5px;
  border: 2px solid #1f78ee;
  border-radius: 6px;
  content: '';
}

.teacher-sidebar__brand-mark i {
  position: absolute;
  z-index: 1;
  width: 15px;
  height: 8px;
  border-radius: 3px;
  background: #1e7cf2;
}

.teacher-sidebar__brand-mark i:first-child {
  top: 3px;
  left: 7px;
}

.teacher-sidebar__brand-mark i:last-child {
  right: 1px;
  bottom: 3px;
}

.teacher-sidebar__nav {
  display: grid;
  align-content: start;
  gap: 6px;
  padding-top: 2px;
}

.teacher-sidebar__link {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  min-height: 46px;
  padding: 0 12px;
  border-radius: 8px;
  color: #263653;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: color 160ms ease, background 160ms ease;
}

.teacher-sidebar__link:hover,
.teacher-sidebar__link:focus-visible,
.teacher-sidebar__link.is-active {
  color: #145cf5;
  background: #eaf2ff;
  outline: none;
}

.teacher-sidebar__icon {
  width: 19px;
  height: 19px;
}

.teacher-sidebar__link span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.teacher-sidebar__toggle {
  display: grid;
  grid-template-columns: 20px 1fr;
  gap: 10px;
  align-items: center;
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid var(--color-border-strong);
  border-radius: 8px;
  color: #405372;
  background: #f8faff;
  cursor: pointer;
  text-align: left;
}

.teacher-sidebar__toggle:hover,
.teacher-sidebar__toggle:focus-visible {
  border-color: #a9c1f8;
  color: var(--color-primary);
  outline: 2px solid #d9e5ff;
  outline-offset: 1px;
}

.teacher-sidebar.is-collapsed {
  width: var(--teacher-sidebar-collapsed-width);
  padding-inline: 10px;
}

.teacher-sidebar.is-collapsed .teacher-sidebar__brand {
  justify-content: center;
  padding-inline: 0;
}

.teacher-sidebar.is-collapsed .teacher-sidebar__brand strong,
.teacher-sidebar.is-collapsed .teacher-sidebar__link span,
.teacher-sidebar.is-collapsed .teacher-sidebar__toggle span {
  display: none;
}

.teacher-sidebar.is-collapsed .teacher-sidebar__link,
.teacher-sidebar.is-collapsed .teacher-sidebar__toggle {
  grid-template-columns: 1fr;
  justify-items: center;
  padding-inline: 0;
}

@media (max-width: 1024px) {
  .teacher-sidebar {
    width: var(--teacher-sidebar-collapsed-width);
    padding-inline: 10px;
  }

  .teacher-sidebar__brand {
    justify-content: center;
    padding-inline: 0;
  }

  .teacher-sidebar__brand strong,
  .teacher-sidebar__link span,
  .teacher-sidebar__toggle span {
    display: none;
  }

  .teacher-sidebar__link,
  .teacher-sidebar__toggle {
    grid-template-columns: 1fr;
    justify-items: center;
    padding-inline: 0;
  }
}

@media (max-width: 760px) {
  .teacher-sidebar {
    width: 64px;
    padding-inline: 8px;
  }
}
</style>
