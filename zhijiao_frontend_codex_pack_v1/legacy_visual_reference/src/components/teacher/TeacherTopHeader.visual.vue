<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/TeacherTopHeader.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <header data-testid="teacher-top-header" class="top-header">
    <span class="route-title sr-only">{{ String($route.meta.title ?? '教师工作台') }}</span>
    <div class="header-actions">
      <form
        class="header-search"
        data-testid="teacher-global-search"
        role="search"
        @submit.prevent="submitSearch"
      >
        <el-input v-model="searchTerm" class="search" placeholder="搜索学生、课程、内容...">
          <template #suffix
            ><el-icon><Search /></el-icon
          ></template>

<style scoped lang="scss">
.top-header {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: flex-end;
  min-height: 56px;
  min-width: 360px;
  padding: 0 24px 0 12px;
  border-bottom: 1px solid #e7edf5;
  background: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 19px;
}

.header-search {
  display: flex;
  align-items: center;
  gap: 5px;
}

.search {
  width: 265px;
}

.header-search > button {
  display: none;
}

.icon-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 0;
  border-radius: 50%;
  color: #40516d;
  background: transparent;
  cursor: pointer;
  font-size: 21px;
}

.icon-button:hover {
  background: #f1f5ff;
  color: #175ff1;
}

.teacher-profile {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0;
  border: 0;
  color: #17233c;
  background: transparent;
  cursor: pointer;
}

.teacher-profile strong {
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.teacher-profile > .el-icon {
  color: #60708b;
  font-size: 14px;
}

.avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 2px solid #fff;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(145deg, #e9b28c, #6e3f35);
  box-shadow: 0 2px 8px rgb(25 59 120 / 16%);
  font-size: 14px;
  font-weight: 800;
}

.header-popover-wrap,
.profile-wrap {
  position: relative;
}

.header-popover {
  position: absolute;
  z-index: 30;
  top: calc(100% + 10px);
  right: 0;
  display: grid;
  min-width: 190px;
  gap: 8px;
  padding: 14px;
  border: 1px solid #dfe7f3;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 30px rgb(35 69 126 / 15%);
}

.header-popover p {
  margin: 0;
  color: #6e7d96;
  font-size: 12px;
}

.header-popover a,
.header-popover button {
  padding: 0;
  border: 0;
  color: #1b62ef;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.profile-popover {
  right: 0;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 1280px) {
  .top-header {
    padding-right: 16px;
  }

  .search {
    width: 220px;
  }
}

@media (max-width: 1080px) {
  .header-actions {
    gap: 12px;
  }

  .search {
    width: 180px;
  }

  .teacher-profile strong {
    display: none;
  }
}

@media (max-width: 760px) {
  .top-header {
    min-width: 0;
    padding-inline: 10px;
  }
  .header-actions {
    min-width: 0;
    width: 100%;
    justify-content: flex-end;
    gap: 8px;
  }
  .header-search,
  .search {
    min-width: 0;
    width: min(180px, 45vw);
  }
  .teacher-profile strong {
    display: none;
  }
}
</style>
