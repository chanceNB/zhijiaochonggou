<!-- VISUAL REFERENCE ONLY.
Source: src/layouts/StudentLayout.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div
    data-testid="student-shell"
    data-breakpoints="1440,1280,1024"
    class="student-layout student-theme v3-shell"
    :class="{
      'is-today-route': isTodayRoute,
      'is-coach-route': isCoachRoute,
      'is-practice-route': isPracticeRoute,
      'is-wrong-book-route': isWrongBookRoute,
      'is-sidebar-collapsed': sidebarCollapsed,
    }"
  >
    <StudentSidebar :collapsed="sidebarCollapsed" :nav-id="sidebarNavId" @toggle="toggleSidebar" />

    <section class="student-layout__workspace" aria-label="学生端主区域">
      <header class="student-layout__topbar">
        <div class="student-layout__status">
          <span v-if="isCoachRoute" class="student-layout__coach-mark" aria-hidden="true">
            <el-icon><Cpu /></el-icon>
          </span>
          <span>{{ topbarTitle }}</span>
        </div>
        <form
          class="student-layout__search"
          :class="{ 'student-layout__compat-search': !isCoachRoute }"
          @submit.prevent="submitSearch"
        >
          <input
            id="student-global-search"
            v-model="searchTerm"
            type="search"
            placeholder="搜索知识点、题目、资料..."
          />
          <button type="submit" aria-label="提交学生搜索" @click="submitSearch">
            <el-icon><Search /></el-icon>
          </button>
        </form>
        <div class="student-layout__actions">
          <button
            class="student-layout__notification"
            type="button"
            aria-label="查看学生通知"
            data-testid="student-notifications"
            @click="notificationsOpen = !notificationsOpen"
          >
            <el-icon><Bell /></el-icon>
            <span class="notification-dot" aria-label="2 条未读通知">2</span>
          </button>
          <div
            v-if="notificationsOpen"
            class="student-layout__popover"
            data-testid="student-notification-panel"
          >
            <strong>学习提醒</strong>
            <p>教师已回复你的复习问题。</p>
            <RouterLink to="/student/messages?status=unread" @click="notificationsOpen = false"
              >查看消息</RouterLink
            >
          </div>
        </div>
        <div class="student-layout__profile-wrap">
          <button
            class="student-layout__profile"
            type="button"
            aria-label="打开学生个人菜单"
            data-testid="student-profile"
            @click="profileOpen = !profileOpen"
          >
            <span class="student-layout__avatar" aria-hidden="true">李</span>
            <strong>李同学</strong>
          </button>
          <div v-if="profileOpen" class="student-layout__popover profile-popover">
            <strong>李同学</strong>
            <button type="button" data-testid="student-logout" @click="logout">退出登录</button>
          </div>
        </div>
      </header>

      <main data-testid="student-shell-main" class="student-layout__main v3-responsive-frame">
        <router-view />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.student-layout {
  --student-sidebar-width: 186px;
  display: grid;
  grid-template-columns: var(--student-sidebar-width) minmax(0, 1fr);
  min-height: 100vh;
  color: #18233f;
  background: #f6f8fc;
}

.student-layout.is-coach-route {
  background: #f6f8fc;
}

.student-layout.is-today-route {
  grid-template-columns: var(--student-sidebar-width) minmax(0, 1fr);
}

/* Keep directed practice inside the shared student shell so navigation remains available. */
.student-layout.is-practice-route {
  grid-template-columns: var(--student-sidebar-width) minmax(0, 1fr);
  background: #f5f8fd;
}

.student-layout__workspace {
  display: grid;
  grid-template-rows: 72px minmax(0, 1fr);
  min-width: 0;
  min-height: 100vh;
}

.student-layout.is-practice-route .student-layout__workspace {
  grid-template-rows: 58px minmax(0, 1fr);
}

.student-layout.is-practice-route .student-layout__topbar {
  min-height: 58px;
  padding: 0 36px;
}

.student-layout.is-practice-route .student-layout__status span {
  font-size: 18px;
  font-weight: 700;
}

.student-layout.is-practice-route .student-layout__main {
  padding: 0;
  overflow: auto;
  background: #f5f8fd;
}

.student-layout.is-today-route .student-layout__topbar {
  display: none;
}

.student-layout.is-today-route .student-layout__workspace {
  grid-template-rows: minmax(0, 1fr);
}

.student-layout__topbar {
  display: grid;
  grid-template-columns: minmax(300px, 1fr) auto auto;
  gap: 22px;
  align-items: center;
  padding: 0 24px 0 30px;
  border-bottom: 1px solid #e4e8f0;
  background: #fff;
}

.student-layout.is-coach-route .student-layout__topbar {
  position: relative;
  grid-template-columns: minmax(0, 1fr) auto auto;
  border-color: #dce4ef;
  background: #fff;
}

.student-layout:not(.is-coach-route) .student-layout__topbar {
  padding-left: 68px;
}

.student-layout__status {
  min-width: 0;
}

.student-layout__status span {
  color: #172347;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.student-layout.is-coach-route .student-layout__status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-layout.is-coach-route .student-layout__status::before {
  content: '';
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  border: 2px solid #254fd2;
  border-radius: 8px;
  background: #2563eb;
  box-shadow: inset 0 0 0 5px #9db6ff;
}

.student-layout.is-coach-route .student-layout__status span {
  color: #151820;
  font-family: inherit;
  font-size: 20px;
  font-weight: 700;
}

.student-layout__search {
  display: grid;
  grid-template-columns: 1fr 30px;
  gap: 10px;
  align-items: center;
  min-height: 42px;
  padding: 0 12px 0 16px;
  border: 1px solid #bec5d3;
  border-radius: 8px;
  background: #fff;
}

.student-layout.is-coach-route .student-layout__search {
  position: absolute;
  left: 50%;
  width: min(620px, 48vw);
  transform: translateX(-50%);
}

.student-layout__compat-search {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
}

.student-layout__search input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: #263248;
  background: transparent;
  font: inherit;
  font-family: inherit;
  font-size: 17px;
}

.student-layout__search input::placeholder {
  color: #747b88;
}

.student-layout__search .el-icon {
  color: #273348;
  font-size: 21px;
}

.student-layout__search button {
  display: grid;
  width: 30px;
  height: 34px;
  place-items: center;
  border: 0;
  color: #273348;
  background: transparent;
  cursor: pointer;
}

.student-layout__actions {
  position: relative;
  display: flex;
  align-items: center;
}

.student-layout__notification {
  position: relative;
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 0;
  color: #172347;
  background: transparent;
  cursor: pointer;
}

.student-layout__notification .el-icon {
  font-size: 24px;
}

.notification-dot {
  position: absolute;
  top: 7px;
  right: 6px;
  display: grid;
  width: 16px;
  height: 16px;
  place-items: center;
  border: 2px solid #fff;
  border-radius: 999px;
  color: #fff;
  background: #ed4a4a;
  font-size: 10px;
  font-weight: 800;
}

.student-layout__profile-wrap {
  position: relative;
}

.student-layout__profile {
  display: grid;
  grid-template-columns: 42px auto 16px;
  gap: 9px;
  align-items: center;
  min-height: 48px;
  padding: 0 0 0 4px;
  border: 0;
  color: #172347;
  background: transparent;
  cursor: pointer;
}

.student-layout__profile::after {
  content: '⌄';
  color: #35435d;
  font-size: 18px;
}

.student-layout__profile strong {
  font-size: 16px;
  font-weight: 800;
}

.student-layout__avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border: 2px solid #c9d8f2;
  border-radius: 50%;
  color: #233d74;
  background: #edf3ff;
  font-weight: 900;
}

.student-layout__popover {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 20;
  display: grid;
  gap: 8px;
  min-width: 180px;
  padding: 12px;
  border: 1px solid #dbe1ec;
  border-radius: 8px;
  color: #23304d;
  background: #fff;
  box-shadow: 0 10px 30px rgb(40 58 95 / 12%);
}

.student-layout__popover p {
  margin: 0;
  color: #66738a;
  font-size: 13px;
  line-height: 1.5;
}

.student-layout__popover a,
.student-layout__popover button {
  color: #315ed5;
  background: transparent;
  border: 0;
  text-align: left;
  cursor: pointer;
}

.profile-popover {
  right: 0;
}

.student-layout__main {
  min-width: 0;
  min-height: 0;
  padding: 16px 6px 24px 22px;
  overflow: auto;
}

.student-layout.is-today-route .student-layout__main {
  width: 100%;
  max-width: none;
  padding: 20px clamp(24px, 3.25vw, 50px) 20px 24px;
  overflow: auto;
}

.student-layout.is-coach-route .student-layout__main {
  padding: 14px 24px 18px;
  background: #f8fafc;
}

.student-layout__main > :deep(*) {
  min-width: 0;
}

:global(.student-layout.is-sidebar-collapsed) {
  --student-sidebar-width: 76px;
}

@media (max-width: 1180px) {
  .student-layout__topbar {
    grid-template-columns: minmax(220px, 1fr) auto auto;
  }

  .student-layout.is-coach-route .student-layout__topbar {
    grid-template-columns: minmax(0, 1fr) auto auto;
  }
}

@media (max-width: 760px) {
  .student-layout {
    --student-sidebar-width: 64px;
  }
  .student-layout__topbar,
  .student-layout.is-coach-route .student-layout__topbar {
    grid-template-columns: 1fr auto auto;
    gap: 10px;
    padding: 10px 14px;
  }

  .student-layout__search {
    grid-column: 1 / -1;
    grid-row: 2;
  }

  .student-layout.is-coach-route .student-layout__search {
    position: static;
    width: auto;
    transform: none;
  }

  .student-layout__status span {
    font-size: 17px;
  }

  .student-layout__main {
    padding: 12px;
  }

  .student-layout.is-practice-route .student-layout__workspace {
    grid-template-rows: 58px minmax(0, 1fr);
  }

  .student-layout.is-practice-route .student-layout__topbar {
    padding: 8px 14px;
  }

  .student-layout.is-practice-route .student-layout__main {
    padding: 0;
  }

  .student-layout.is-today-route .student-layout__main {
    padding: 12px;
  }
}

@media (max-width: 1024px) {
  .student-layout {
    --student-sidebar-width: 76px;
  }
}

@media (max-width: 760px) {
  .student-layout {
    --student-sidebar-width: 64px;
  }
}

/* The coach prototype uses a shared full-width header with navigation starting below it. */
.student-layout.is-coach-route {
  grid-template-columns: 186px minmax(0, 1fr);
  min-height: 100vh;
}

.student-layout.is-coach-route .student-layout__workspace {
  display: block;
  grid-column: 2;
  grid-row: 1;
  min-height: 100vh;
}

.student-layout.is-coach-route .student-layout__topbar {
  position: fixed;
  inset: 0 0 auto;
  z-index: 40;
  width: 100%;
  height: 72px;
  min-height: 72px;
  box-sizing: border-box;
  padding: 0 24px;
  border-bottom-color: #e3e9f3;
  box-shadow: 0 1px 0 rgb(30 64 122 / 3%);
}

.student-layout.is-coach-route .student-layout__status {
  gap: 14px;
}

.student-layout.is-coach-route .student-layout__status::before {
  display: none;
}

.student-layout__coach-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 9px;
  color: #fff;
  background: #2f67ed;
  box-shadow: inset 0 0 0 4px rgb(255 255 255 / 20%);
}

.student-layout__coach-mark .el-icon {
  font-size: 24px;
}

.student-layout.is-coach-route .student-layout__status span:last-child {
  font-size: 22px;
  font-weight: 800;
}

.student-layout.is-coach-route .student-layout__main {
  height: 100vh;
  min-height: 0;
  box-sizing: border-box;
  padding: 80px 38px 18px 13px;
  scrollbar-width: none;
  background: #f7f9fc;
}

.student-layout.is-coach-route .student-layout__main::-webkit-scrollbar {
  display: none;
}

:global(.student-layout.is-coach-route .student-sidebar) {
  position: fixed;
  inset: 72px auto 0 0;
  z-index: 30;
  width: 186px;
  height: auto;
  padding: 18px 16px 20px;
  grid-template-rows: 1fr auto;
  gap: 0;
}

:global(.student-layout.is-coach-route .student-sidebar__brand) {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  padding: 0;
  border: 0;
  clip: rect(0 0 0 0);
  clip-path: inset(50%);
  white-space: nowrap;
}

:global(.student-layout.is-coach-route .student-sidebar__nav) {
  gap: 6px;
}

:global(.student-layout.is-coach-route .student-sidebar__note) {
  display: none;
}

@media (max-width: 760px) {
  .student-layout.is-coach-route .student-layout__topbar {
    position: sticky;
    padding: 10px 14px;
  }

  .student-layout.is-coach-route .student-layout__main {
    padding: 12px;
  }

  :global(.student-layout.is-coach-route .student-sidebar) {
    position: sticky;
    inset: 0 auto auto 0;
    width: 64px;
    height: 100vh;
  }
}

@media (max-width: 760px) {
  .student-layout.is-coach-route {
    display: block;
    width: 100%;
    min-height: 100dvh;
    overflow-x: hidden;
  }

  .student-layout.is-coach-route .student-layout__workspace {
    display: block;
    width: 100%;
    min-height: 100dvh;
  }

  .student-layout.is-coach-route .student-layout__topbar {
    position: sticky;
    inset: 0 0 auto;
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto auto;
    grid-template-rows: auto auto;
    gap: 8px 10px;
    width: 100%;
    height: auto;
    min-height: 64px;
    padding: 10px 14px;
  }

  .student-layout.is-coach-route .student-layout__status {
    min-width: 0;
    gap: 8px;
  }

  .student-layout.is-coach-route .student-layout__coach-mark {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    box-shadow: inset 0 0 0 3px rgb(255 255 255 / 20%);
  }

  .student-layout.is-coach-route .student-layout__coach-mark .el-icon {
    font-size: 19px;
  }

  .student-layout.is-coach-route .student-layout__status span:last-child {
    overflow: hidden;
    font-size: 18px !important;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .student-layout.is-coach-route .student-layout__search {
    position: static;
    grid-column: 1 / -1;
    grid-row: 2;
    width: auto;
    min-height: 40px;
    transform: none;
  }

  .student-layout.is-coach-route .student-layout__actions {
    grid-column: 2;
    grid-row: 1;
  }

  .student-layout.is-coach-route .student-layout__profile-wrap {
    grid-column: 3;
    grid-row: 1;
    width: auto;
  }

  .student-layout.is-coach-route .student-layout__search input {
    font-size: 15px;
  }

  .student-layout.is-coach-route .student-layout__actions {
    min-width: 0;
  }

  .student-layout.is-coach-route .student-layout__profile {
    grid-template-columns: 34px 0 14px;
    gap: 4px;
    min-width: 52px;
    min-height: 40px;
    padding: 0;
  }

  .student-layout.is-coach-route .student-layout__profile strong {
    display: none;
  }

  .student-layout.is-coach-route .student-layout__avatar {
    width: 34px;
    height: 34px;
  }

  .student-layout.is-coach-route .student-layout__profile::after {
    font-size: 14px;
  }

  .student-layout.is-coach-route .student-layout__main {
    width: 100%;
    height: auto;
    min-height: calc(100dvh - 64px);
    padding: 12px 12px 84px;
    overflow: visible;
  }

  :global(.student-layout.is-coach-route .student-sidebar) {
    position: fixed;
    inset: auto 0 0;
    z-index: 50;
    display: block;
    width: 100%;
    height: 68px;
    padding: 6px 10px;
    border: 0;
    border-top: 1px solid #e0e7f1;
    overflow-x: auto;
    overflow-y: hidden;
    background: #fff;
  }

  :global(.student-layout.is-coach-route .student-sidebar__brand),
  :global(.student-layout.is-coach-route .student-sidebar__toggle),
  :global(.student-layout.is-coach-route .student-sidebar__note) {
    display: none;
  }

  :global(.student-layout.is-coach-route .student-sidebar__nav) {
    display: flex;
    align-items: stretch;
    width: max-content;
    min-width: 100%;
    height: 56px;
    gap: 4px;
  }

  :global(.student-layout.is-coach-route .student-sidebar__link) {
    flex: 0 0 64px;
    display: grid;
    grid-template-columns: 1fr;
    gap: 2px;
    align-content: center;
    justify-items: center;
    width: 64px;
    min-height: 54px;
    padding: 5px 4px;
    border-radius: 8px;
    text-align: center;
  }

  :global(.student-layout.is-coach-route .student-sidebar__icon) {
    font-size: 18px;
  }

  :global(.student-layout.is-coach-route .student-sidebar__label) {
    display: block !important;
    overflow: hidden;
    max-width: 100%;
    color: inherit;
    font-size: 11px !important;
    line-height: 1.2;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :global(.student-layout.is-coach-route .student-sidebar__label::after) {
    display: none;
  }
}

/* Keep the wrong-book workspace inside the shared student shell. */
.student-layout.is-wrong-book-route {
  grid-template-columns: var(--student-sidebar-width) minmax(0, 1fr);
  background: #f5f8fd;
}

.student-layout.is-wrong-book-route .student-layout__workspace {
  grid-template-rows: minmax(0, 1fr);
}

.student-layout.is-wrong-book-route .student-layout__topbar {
  display: none;
}

.student-layout.is-wrong-book-route .student-layout__main {
  width: 100%;
  max-width: none;
  padding: 0;
  overflow: auto;
}
</style>
