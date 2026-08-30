<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/TeacherTodoPanel.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="surface-card todo-card">
    <div class="card-header"><h2 class="card-title">今日待办</h2></div>
    <div class="todos">
      <label v-for="todo in todos" :key="todo.id"
        ><el-checkbox :model-value="todo.completed" /><span>{{ todo.label }}</span
        ><b>{{ todo.count }}</b></label
      ><button data-testid="go-review" type="button" @click="$emit('review')">
        查看全部待办 ›
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.todos {
  padding: 6px 16px 10px;
}
.todos label {
  display: grid;
  grid-template-columns: 22px 1fr auto;
  align-items: center;
  gap: 6px;
  min-height: 27px;
  font-size: 13px;
}
.todos b {
  min-width: 24px;
  padding: 2px 7px;
  border-radius: 7px;
  background: #dbeafe;
  color: #2563eb;
  text-align: center;
}
.todos button {
  width: 100%;
  margin-top: 6px;
  border: 0;
  background: transparent;
  color: #2563eb;
  font-weight: 700;
  cursor: pointer;
  text-align: right;
}
</style>
