<template>
  <aside
    class="student-sidebar"
    :class="{ 'is-collapsed': collapsed }"
    data-testid="student-sidebar"
    aria-label="学生端导航"
  >
    <div class="student-sidebar__brand">
      <RouterLink class="student-sidebar__brand-mark" to="/student/today" aria-label="学生学习空间">
        <span class="student-sidebar__brand-icon" aria-hidden="true">智</span>
        <span class="student-sidebar__brand-name">学习空间</span>
      </RouterLink>
      <button
        class="student-sidebar__toggle"
        data-testid="student-sidebar-toggle"
        type="button"
        :aria-controls="navId"
        :aria-expanded="!collapsed"
        :aria-label="collapsed ? '展开学生端导航' : '收起学生端导航'"
        @click="emit('toggle')"
      >
        <Expand v-if="collapsed" aria-hidden="true" />
        <Fold v-else aria-hidden="true" />
      </button>
    </div>

    <nav :id="navId" class="student-sidebar__nav" data-testid="student-sidebar-nav">
      <RouterLink
        v-for="item in items"
        :key="item.to"
        class="student-sidebar__link"
        :class="{ 'is-active': isActive(item.match) }"
        :to="item.to"
        data-testid="student-nav-link"
        :aria-current="isActive(item.match) ? 'page' : undefined"
        :title="collapsed ? item.label : undefined"
      >
        <component :is="item.icon" class="student-sidebar__icon" aria-hidden="true" />
        <span class="student-sidebar__label">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <div class="student-sidebar__note" aria-hidden="true">
      <span>学习空间</span>
      <strong>准备就绪</strong>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { toRef, type Component } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  ChatDotRound,
  Collection,
  EditPen,
  Expand,
  Fold,
  HomeFilled,
  Reading,
  TrendCharts,
} from '@element-plus/icons-vue'

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const route = useRoute()
const navId = 'student-sidebar-nav'
const collapsed = toRef(props, 'collapsed')

const items = [
  { label: '今日学习', to: '/student/today', match: '/student/today', icon: HomeFilled },
  { label: 'AI学习教练', to: '/student/ai-coach', match: '/student/ai-coach', icon: ChatDotRound },
  { label: '定向练习', to: '/student/practice', match: '/student/practice', icon: EditPen },
  { label: '错题本', to: '/student/wrong-book', match: '/student/wrong-book', icon: Collection },
  { label: '我的成长', to: '/student/growth', match: '/student/growth', icon: TrendCharts },
  { label: '学习资料', to: '/student/resources', match: '/student/resources', icon: Reading },
] satisfies Array<{ label: string; to: string; match: string; icon: Component }>

const isActive = (match: string) => route.path === match || route.path.startsWith(`${match}/`)
</script>

<style scoped>
.student-sidebar {
  position: sticky;
  top: 0;
  z-index: 2;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 24px;
  width: var(--student-sidebar-width);
  height: 100vh;
  padding: 18px 16px 20px;
  border-right: 1px solid var(--color-border);
  background: var(--color-card);
  transition: width 180ms ease, padding 180ms ease;
}

.student-sidebar__brand {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 30px;
  gap: 6px;
  align-items: center;
  padding: 0 6px 12px;
  border-bottom: 1px solid #eef1f5;
}

.student-sidebar__brand-mark {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  color: #111b3e;
  text-decoration: none;
  white-space: nowrap;
}

.student-sidebar__brand-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 2px solid #9db6ff;
  border-radius: 8px;
  color: #fff;
  background: #2f67ed;
  font-size: 17px;
  font-weight: 800;
}

.student-sidebar__brand-name {
  overflow: hidden;
  font-size: 17px;
  font-weight: 800;
  text-overflow: ellipsis;
}

.student-sidebar__toggle {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 1px solid var(--color-border-strong);
  border-radius: 6px;
  color: #405372;
  background: #f8faff;
  cursor: pointer;
}

.student-sidebar__toggle:hover,
.student-sidebar__toggle:focus-visible {
  border-color: #a9c1f8;
  color: var(--color-primary);
  outline: 2px solid #d9e5ff;
  outline-offset: 1px;
}

.student-sidebar__nav {
  display: grid;
  align-content: start;
  gap: 7px;
}

.student-sidebar__link {
  display: grid;
  grid-template-columns: 25px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-height: 48px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  color: #253250;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  transition: color 160ms ease, background 160ms ease;
}

.student-sidebar__link:hover,
.student-sidebar__link:focus-visible,
.student-sidebar__link.is-active {
  color: #2f63dc;
  background: var(--color-active);
  outline: none;
}

.student-sidebar__icon {
  width: 21px;
  height: 21px;
}

.student-sidebar__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-sidebar__note {
  display: grid;
  gap: 4px;
  padding: 10px 9px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  color: var(--color-secondary);
  background: #fafbfe;
  font-size: 12px;
}

.student-sidebar__note strong {
  color: #3550a3;
  font-size: 13px;
}

.student-sidebar.is-collapsed {
  width: var(--student-sidebar-collapsed-width);
  padding-inline: 10px;
}

.student-sidebar.is-collapsed .student-sidebar__brand {
  grid-template-columns: 1fr;
  justify-items: center;
}

.student-sidebar.is-collapsed .student-sidebar__brand-name,
.student-sidebar.is-collapsed .student-sidebar__label,
.student-sidebar.is-collapsed .student-sidebar__note {
  display: none;
}

.student-sidebar.is-collapsed .student-sidebar__link {
  grid-template-columns: 1fr;
  justify-items: center;
  padding: 0;
}

@media (max-width: 1024px) {
  .student-sidebar {
    width: var(--student-sidebar-collapsed-width);
    padding-inline: 10px;
  }

  .student-sidebar__brand {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .student-sidebar__brand-name,
  .student-sidebar__label,
  .student-sidebar__note {
    display: none;
  }

  .student-sidebar__link {
    grid-template-columns: 1fr;
    justify-items: center;
    padding: 0;
  }
}

@media (max-width: 760px) {
  .student-sidebar {
    width: 64px;
    padding-inline: 8px;
  }
}
</style>
