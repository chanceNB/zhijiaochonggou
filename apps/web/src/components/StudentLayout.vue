<template>
  <div
    class="student-layout student-theme"
    :class="{
      'is-sidebar-collapsed': sidebarCollapsed,
      'is-today-route': isTodayRoute,
      'is-practice-route': isPracticeRoute,
    }"
    data-testid="student-shell"
  >
    <StudentSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />

    <section class="student-layout__workspace" aria-label="学生端主区域">
      <header v-if="!isTodayRoute" class="student-layout__topbar" data-testid="student-top-header">
        <div class="student-layout__status">
          <span v-if="isCoachRoute" class="student-layout__coach-mark" aria-hidden="true">
            <Cpu />
          </span>
          <span>{{ topbarTitle }}</span>
        </div>

        <form class="student-layout__search" role="search" @submit.prevent>
          <input type="search" aria-label="搜索学生学习内容" placeholder="搜索知识点、题目、资料..." />
          <button type="submit" aria-label="提交搜索">
            <Search aria-hidden="true" />
          </button>
        </form>

        <div class="student-layout__actions">
          <button class="student-layout__notification" type="button" aria-label="查看学生通知">
            <Bell aria-hidden="true" />
          </button>
          <button class="student-layout__profile" type="button" aria-label="打开学生个人菜单">
            <span class="student-layout__avatar" aria-hidden="true">学</span>
            <strong>学生账户</strong>
            <span class="student-layout__chevron" aria-hidden="true">⌄</span>
          </button>
        </div>
      </header>

      <main
        class="student-layout__main"
        data-testid="student-shell-main"
        :aria-label="`${topbarTitle}内容`"
      >
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { Bell, Cpu, Search } from '@element-plus/icons-vue'
import StudentSidebar from '@/components/StudentSidebar.vue'

const route = useRoute()
const sidebarCollapsed = ref(false)

const isTodayRoute = computed(() => route.path === '/student/today')
const isCoachRoute = computed(() => route.path.startsWith('/student/ai-coach'))
const isPracticeRoute = computed(() => route.path.startsWith('/student/practice'))
const topbarTitle = computed(() => String(route.meta.title ?? '学生学习空间'))
</script>

<style scoped>
.student-layout {
  --student-layout-sidebar-width: var(--student-sidebar-width, 186px);
  display: grid;
  grid-template-columns: var(--student-layout-sidebar-width) minmax(0, 1fr);
  min-height: 100vh;
  color: var(--color-text);
  background: var(--color-student-bg);
  transition: grid-template-columns 180ms ease;
}

.student-layout.is-sidebar-collapsed {
  --student-layout-sidebar-width: var(--student-sidebar-collapsed-width, 76px);
}

.student-layout__workspace {
  display: grid;
  grid-template-rows: 72px minmax(0, 1fr);
  min-width: 0;
  min-height: 100vh;
}

.student-layout.is-today-route .student-layout__workspace {
  grid-template-rows: minmax(0, 1fr);
}

.student-layout.is-practice-route .student-layout__workspace {
  grid-template-rows: 58px minmax(0, 1fr);
}

.student-layout__topbar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(300px, 620px) auto;
  gap: 22px;
  align-items: center;
  min-width: 0;
  padding: 0 24px 0 30px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-card);
}

.student-layout.is-practice-route .student-layout__topbar {
  min-height: 58px;
  padding-inline: 30px;
}

.student-layout__status {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.student-layout__status > span:last-child {
  overflow: hidden;
  color: var(--color-text);
  font-size: 20px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-layout__coach-mark {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 9px;
  color: #fff;
  background: var(--color-primary);
  box-shadow: inset 0 0 0 4px rgb(255 255 255 / 20%);
}

.student-layout__search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 30px;
  gap: 8px;
  align-items: center;
  min-height: 42px;
  padding: 0 10px 0 16px;
  border: 1px solid #bec5d3;
  border-radius: 8px;
  background: var(--color-card);
}

.student-layout__search input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: #263248;
  background: transparent;
  font: inherit;
  font-size: 14px;
}

.student-layout__search input::placeholder {
  color: #747b88;
}

.student-layout__search button,
.student-layout__notification,
.student-layout__profile {
  border: 0;
  background: transparent;
  cursor: pointer;
}

.student-layout__search button {
  display: grid;
  width: 30px;
  height: 34px;
  place-items: center;
  color: #273348;
}

.student-layout__actions {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.student-layout__notification {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 50%;
  color: #40516d;
  font-size: 21px;
}

.student-layout__notification:hover,
.student-layout__notification:focus-visible {
  color: var(--color-primary);
  background: var(--color-primary-soft);
  outline: none;
}

.student-layout__profile {
  display: grid;
  grid-template-columns: 38px auto 14px;
  gap: 8px;
  align-items: center;
  min-height: 44px;
  padding: 0;
  color: var(--color-text);
}

.student-layout__profile strong {
  font-size: 14px;
  white-space: nowrap;
}

.student-layout__avatar {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 2px solid #c9d8f2;
  border-radius: 50%;
  color: #233d74;
  background: #edf3ff;
  font-weight: 900;
}

.student-layout__chevron {
  color: #60708b;
  font-size: 16px;
}

.student-layout__main {
  min-width: 0;
  min-height: 0;
  padding: 22px 24px 34px;
  overflow: auto;
}

.student-layout.is-today-route .student-layout__main {
  padding: 24px clamp(24px, 3.25vw, 50px);
}

.student-layout.is-practice-route .student-layout__main {
  padding: 0;
  background: #f5f8fd;
}

.student-layout__main :deep(> *) {
  min-width: 0;
}

@media (max-width: 1180px) {
  .student-layout__topbar {
    grid-template-columns: minmax(180px, 1fr) minmax(220px, 44vw) auto;
  }
}

@media (max-width: 1024px) {
  .student-layout {
    --student-layout-sidebar-width: var(--student-sidebar-collapsed-width, 76px);
  }

  .student-layout__topbar {
    padding-inline: 18px;
  }

  .student-layout__profile strong {
    display: none;
  }

  .student-layout__profile {
    grid-template-columns: 38px 14px;
  }
}

@media (max-width: 760px) {
  .student-layout {
    --student-layout-sidebar-width: 64px;
  }

  .student-layout__workspace {
    min-width: 0;
  }

  .student-layout__topbar {
    grid-template-columns: minmax(0, 1fr) auto;
    grid-template-rows: auto auto;
    gap: 8px 10px;
    min-height: 64px;
    padding: 10px 14px;
  }

  .student-layout__search {
    grid-column: 1 / -1;
    grid-row: 2;
    width: 100%;
  }

  .student-layout__actions {
    grid-column: 2;
    grid-row: 1;
  }

  .student-layout__status > span:last-child {
    font-size: 17px;
  }

  .student-layout__main,
  .student-layout.is-today-route .student-layout__main {
    padding: 12px;
  }

  .student-layout.is-practice-route .student-layout__main {
    padding: 0;
  }
}
</style>
