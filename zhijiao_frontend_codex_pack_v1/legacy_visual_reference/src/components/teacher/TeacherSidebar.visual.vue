<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/TeacherSidebar.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <aside class="teacher-nav" aria-label="教师端导航">
    <RouterLink to="/teacher/dashboard" class="teacher-brand" aria-label="教师工作台">
      <span class="brand-mark" aria-hidden="true"><i></i><i></i></span>
      <strong>教师工作台</strong>
    </RouterLink>
    <nav class="teacher-nav-list">
      <RouterLink
        v-for="item in visibleItems"
        :key="item.path"
        :to="item.path"
        data-testid="teacher-nav-link"
        class="teacher-nav-link"
        :class="{ active: activePath === item.path }"
        :aria-current="activePath === item.path ? 'page' : undefined"
        :title="item.label"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped lang="scss">
.teacher-nav {
  display: flex;
  flex: 1 1 auto;
  align-items: stretch;
  min-width: 0;
}

.teacher-brand {
  display: inline-flex;
  flex: 0 0 205px;
  align-items: center;
  gap: 11px;
  padding: 0 24px;
  color: #17233c;
  white-space: nowrap;
}

.teacher-brand strong {
  font-size: 21px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.brand-mark {
  position: relative;
  display: inline-block;
  width: 29px;
  height: 29px;
  transform: none;
}

.brand-mark::after {
  position: absolute;
  inset: 3px 4px 3px 5px;
  border: 2px solid #1f78ee;
  border-radius: 6px;
  content: '';
}

.brand-mark i {
  position: absolute;
  z-index: 1;
  width: 15px;
  height: 8px;
  border-radius: 3px;
  background: #1e7cf2;
}

.brand-mark i:first-child {
  top: 3px;
  left: 7px;
}

.brand-mark i:last-child {
  right: 1px;
  bottom: 3px;
}

.teacher-nav-list {
  display: flex;
  align-items: stretch;
  gap: 3px;
  min-width: 0;
}

.teacher-nav-link {
  position: relative;
  display: inline-flex;
  min-width: 73px;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 13px;
  color: #263653;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  transition:
    color 160ms ease,
    background 160ms ease;
}

.teacher-nav-link .el-icon {
  display: none;
}

.teacher-nav-link::after {
  position: absolute;
  right: 13px;
  bottom: 0;
  left: 13px;
  height: 3px;
  border-radius: 4px 4px 0 0;
  background: transparent;
  content: '';
}

.teacher-nav-link:hover {
  color: #145cf5;
  background: #f6f9ff;
}

.teacher-nav-link.active {
  color: #145cf5;
}

.teacher-nav-link.active::after {
  background: #2168f4;
}

.teacher-nav-link--secondary {
  display: flex;
}

@media (max-width: 1280px) {
  .teacher-brand {
    flex-basis: 180px;
    padding-inline: 18px;
  }

  .teacher-nav-link {
    min-width: 65px;
    padding-inline: 8px;
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  .teacher-brand {
    flex-basis: 160px;
    padding-inline: 14px;
  }

  .teacher-brand strong {
    font-size: 18px;
  }

  .teacher-nav-link {
    min-width: 56px;
    padding-inline: 5px;
    font-size: 13px;
  }
}

/* The teacher prototype uses a persistent vertical navigation rail. */
.teacher-nav {
  flex-direction: column;
  min-height: 100vh;
  padding: 0 14px 18px;
  border-right: 1px solid #e5eaf2;
  background: #fff;
}
.teacher-brand {
  flex: 0 0 auto;
  min-height: 74px;
  padding: 0 10px;
  border-bottom: 1px solid #edf1f7;
}
.teacher-nav-list {
  display: grid;
  gap: 6px;
  padding-top: 18px;
}
.teacher-nav-link {
  display: flex;
  min-height: 46px;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 12px;
  border-radius: 8px;
}
.teacher-nav-link .el-icon {
  display: inline-flex;
  flex: 0 0 22px;
  justify-content: center;
  font-size: 19px;
}
.teacher-nav-link::after {
  inset: 0;
  z-index: -1;
  right: auto;
  bottom: auto;
  left: auto;
  height: auto;
  border-radius: 8px;
}
.teacher-nav-link.active {
  background: #eaf2ff;
}
.teacher-nav-link.active::after {
  background: #eaf2ff;
}
:global(.teacher-shell.collapsed) .teacher-nav .teacher-brand {
  justify-content: center;
  padding-inline: 0;
}
:global(.teacher-shell.collapsed) .teacher-nav .teacher-brand strong,
:global(.teacher-shell.collapsed) .teacher-nav .teacher-nav-link span {
  display: none;
}
:global(.teacher-shell.collapsed) .teacher-nav .teacher-nav-link {
  justify-content: center;
  padding-inline: 0;
}
</style>
