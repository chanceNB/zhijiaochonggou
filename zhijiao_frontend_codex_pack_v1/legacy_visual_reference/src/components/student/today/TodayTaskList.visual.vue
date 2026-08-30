<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/TodayTaskList.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="task-card" data-testid="today-task-list">
    <header>
      <h3>今天的任务</h3>
    </header>
    <ul>
      <li
        v-for="task in tasks"
        :key="task.id"
        :class="{ active: highlightedTaskId === task.id, completed: task.status === 'completed' }"
      >
        <span class="check">{{
          task.status === 'completed' ? '已完成' : highlightedTaskId === task.id ? '进行中' : ''
        }}</span>
        <div>
          <strong>{{ task.title }}</strong>
          <small>预计 {{ task.estimateMinutes }} 分钟</small>
        </div>
        <button @click="$emit('navigate', task.route)">{{ task.actionLabel }}</button>
      </li>
    </ul>
    <a href="#" @click.prevent="$emit('navigate', '/student/practice')"
      >查看全部任务（共 {{ tasks.length + 2 }} 项） ›</a
    >
  </section>
</template>

<style scoped lang="scss">
.task-card {
  min-width: 0;
  padding: 16px;
  border: 1px solid #d2dff0;
  border-radius: 14px;
  background: rgb(255 255 255 / 92%);
}
h3 {
  margin: 0 0 10px;
  color: #135fd0;
}
ul {
  display: grid;
  gap: 9px;
  padding: 0;
  margin: 0;
  list-style: none;
}
li {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr) 68px;
  gap: 8px;
  align-items: center;
}
li.active li > div {
  min-width: 0;
}
strong {
  color: #174fba;
}
.check {
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  border: 1px solid #9ab5dd;
  border-radius: 5px;
  color: #2f74ee;
  font-size: 12px;
}
strong,
small {
  display: block;
}
li > div {
  min-width: 0;
}
strong {
  color: #243b63;
  overflow-wrap: anywhere;
}
small {
  margin-top: 3px;
  color: #8190a7;
}
button {
  min-height: 32px;
  border: 1px solid #a9c5ec;
  border-radius: 8px;
  background: #f8fbff;
  color: #2563eb;
  font-weight: 700;
  cursor: pointer;
}
a {
  display: inline-block;
  margin-top: 10px;
  color: #2563eb;
  font-weight: 800;
  text-decoration: none;
}
</style>
