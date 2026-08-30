<template>
  <div
    class="teacher-shell teacher-theme"
    :class="{ 'is-sidebar-collapsed': sidebarCollapsed }"
    data-testid="teacher-shell"
  >
    <TeacherSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />
    <section class="teacher-workspace" aria-label="教师端主区域">
      <TeacherTopHeader />
      <main class="teacher-content" data-testid="teacher-shell-main">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import TeacherSidebar from '@/components/TeacherSidebar.vue'
import TeacherTopHeader from '@/components/TeacherTopHeader.vue'

const sidebarCollapsed = ref(false)
</script>

<style scoped>
.teacher-shell {
  display: grid;
  grid-template-columns: var(--teacher-sidebar-width) minmax(0, 1fr);
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
  background: #f7faff;
  transition: grid-template-columns 180ms ease;
}

.teacher-shell.is-sidebar-collapsed {
  grid-template-columns: var(--teacher-sidebar-collapsed-width) minmax(0, 1fr);
}

.teacher-workspace {
  width: 100%;
  min-width: 0;
  overflow-x: hidden;
}

.teacher-content {
  width: 100%;
  max-width: 1440px;
  min-width: 0;
  margin-inline: auto;
  padding: 22px 24px 34px;
}

.teacher-content :deep(> *) {
  min-width: 0;
}

@media (max-width: 1180px) {
  .teacher-content {
    padding: 18px;
  }
}

@media (max-width: 1024px) {
  .teacher-shell {
    grid-template-columns: var(--teacher-sidebar-collapsed-width) minmax(0, 1fr);
  }

  .teacher-content {
    padding: 16px;
  }
}

@media (max-width: 760px) {
  .teacher-shell {
    grid-template-columns: 64px minmax(0, 1fr);
  }

  .teacher-content {
    padding: 12px;
  }
}
</style>
