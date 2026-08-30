<!-- VISUAL REFERENCE ONLY.
Source: src/layouts/TeacherLayout.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div
    data-testid="teacher-shell"
    data-breakpoints="1440,1280,1024"
    class="teacher-shell teacher-theme v3-shell"
    :class="{ collapsed, 'profile-mode': profileMode }"
  >
    <TeacherSidebar v-if="!profileMode" :collapsed="collapsed" @toggle="collapsed = !collapsed" />
    <section class="teacher-workspace">
      <TeacherTopHeader />
      <main data-testid="teacher-shell-main" class="teacher-content v3-responsive-frame">
        <router-view />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.teacher-shell {
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  width: 100%;
  max-width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
  background: #f7faff;
  transition: grid-template-columns 0.2s ease;
}

.teacher-shell.collapsed {
  grid-template-columns: 78px minmax(0, 1fr);
}

.teacher-shell.profile-mode {
  grid-template-columns: minmax(0, 1fr);
  background: #f6f8fc;
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
  box-sizing: border-box;
}

.teacher-shell.profile-mode .teacher-content {
  max-width: none;
  padding: 0 24px 24px;
}

@media (max-width: 1180px) {
  .teacher-content {
    padding: 18px;
  }

  .teacher-shell.profile-mode .teacher-content {
    padding: 0 18px 20px;
  }
}

@media (max-width: 1024px) {
  .teacher-content {
    padding: 16px;
  }

  .teacher-shell.profile-mode .teacher-content {
    padding: 0 16px 18px;
  }
}

@media (max-width: 760px) {
  .teacher-shell {
    grid-template-columns: 64px minmax(0, 1fr);
  }
  .teacher-content {
    padding: 12px;
  }

  .teacher-shell.profile-mode .teacher-content {
    padding: 0 12px 16px;
  }
  .top-header {
    min-width: 0;
    padding-inline: 10px;
  }
}
</style>
