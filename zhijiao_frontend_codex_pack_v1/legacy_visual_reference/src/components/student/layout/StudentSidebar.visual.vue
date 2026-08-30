<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/layout/StudentSidebar.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <aside class="student-sidebar" aria-label="学生端导航">
    <div class="student-sidebar__brand">
      <div class="student-sidebar__brand-mark" aria-label="AG 学习助手">
        <span class="student-sidebar__brand-icon" aria-hidden="true">AG</span>
        <span class="student-sidebar__brand-name">学习助手</span>
      </div>
      <button
        data-testid="student-sidebar-toggle"
        class="student-sidebar__toggle"
        type="button"
        :aria-controls="navId"
        :aria-expanded="!collapsed"
        :aria-label="collapsed ? '展开学生端导航' : '收起学生端导航'"
        @click="emit('toggle')"
      >
        <el-icon>
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
      </button>
    </div>

    <nav :id="navId" data-testid="student-sidebar-nav" class="student-sidebar__nav">
      <router-link
        v-for="item in visibleNavItems"
        :key="item.to"
        data-testid="student-nav-link"
        class="student-sidebar__link"
        :to="item.to"
      >
        <el-icon class="student-sidebar__icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="student-sidebar__label">{{ item.label }}</span>
        <i v-if="item.badge" class="student-sidebar__badge" aria-label="有新消息" />
      </router-link>
    </nav>

    <div class="student-sidebar__note">
      <span>学习空间</span>
      <strong>切换视图</strong>
      <el-icon aria-hidden="true"><Expand /></el-icon>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.student-sidebar {
  position: sticky;
  top: 0;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 24px;
  width: 186px;
  height: 100vh;
  padding: 18px 16px 20px;
  border-right: 1px solid #e5e9f1;
  background: #fff;
}

.student-sidebar__brand {
  display: grid;
  grid-template-columns: 1fr 30px;
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
  white-space: nowrap;
}

.student-sidebar__brand-icon {
  display: grid;
  width: 42px;
  height: 32px;
  place-items: center;
  color: #2f63dd;
  font-family: Arial, sans-serif;
  font-size: 21px;
  font-style: italic;
  font-weight: 900;
  letter-spacing: -3px;
}

.student-sidebar__brand-name {
  font-size: 17px;
  font-weight: 800;
}

.student-sidebar__toggle {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 1px solid #d8dfeb;
  border-radius: 6px;
  color: #405372;
  background: #f8faff;
  cursor: pointer;
}

.student-sidebar__nav {
  display: grid;
  align-content: start;
  gap: 7px;
}

.student-sidebar__link {
  position: relative;
  display: grid;
  grid-template-columns: 25px 1fr auto;
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
}

.student-sidebar__link:hover,
.student-sidebar__link.router-link-active {
  color: #2f63dc;
  background: #eef2ff;
}

.student-sidebar__icon {
  color: currentcolor;
  font-size: 21px;
}

.student-sidebar__badge {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #ff5a4f;
}

.student-sidebar__note {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px 6px;
  align-items: center;
  padding: 10px 9px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  color: #6c7890;
  background: #fafbfe;
  font-size: 12px;
}

.student-sidebar__note strong {
  color: #3550a3;
  font-size: 13px;
}

.student-sidebar__note .el-icon {
  grid-row: 1 / span 2;
  grid-column: 2;
  color: #54698f;
}

:global(.student-layout.is-sidebar-collapsed) .student-sidebar {
  width: 76px;
  padding-inline: 10px;
}

:global(.student-layout.is-sidebar-collapsed) .student-sidebar__brand {
  grid-template-columns: 1fr;
  justify-items: center;
}

:global(.student-layout.is-sidebar-collapsed) .student-sidebar__brand-name,
:global(.student-layout.is-sidebar-collapsed) .student-sidebar__label,
:global(.student-layout.is-sidebar-collapsed) .student-sidebar__note {
  display: none;
}

:global(.student-layout.is-sidebar-collapsed) .student-sidebar__link {
  grid-template-columns: 1fr;
  justify-items: center;
  padding: 0;
}

@media (max-width: 1024px) {
  .student-sidebar {
    width: 76px;
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
