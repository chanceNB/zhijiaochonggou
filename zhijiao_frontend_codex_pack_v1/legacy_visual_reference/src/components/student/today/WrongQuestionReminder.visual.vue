<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/WrongQuestionReminder.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="wrong-card" data-testid="wrong-question-reminder">
    <h3>⚠ 错题提醒</h3>
    <p>
      你有 <strong>{{ items.reduce((sum, item) => sum + item.count, 0) }}</strong>
      道错题未巩固，建议及时复习哦！
    </p>
    <ul>
      <li v-for="item in items" :key="item.id">
        <span>{{ item.title }}</span>
        <b>错 {{ item.count }} 次</b>
      </li>
    </ul>
    <button @click="$emit('review')">去错题本巩固 ›</button>
  </section>
</template>

<style scoped lang="scss">
.wrong-card {
  min-width: 0;
  padding: 16px;
  border: 1px solid #f2d6d6;
  border-radius: 14px;
  background: #fffafa;
}
h3 {
  margin: 0 0 9px;
  color: #dc3d3d;
}
p {
  margin: 0 0 9px;
  color: #50627e;
  line-height: 1.5;
}
p strong {
  color: #e03131;
}
ul {
  display: grid;
  gap: 8px;
  padding: 0;
  margin: 0;
  list-style: none;
}
li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
  color: #263b5f;
}
li span {
  min-width: 0;
  overflow-wrap: anywhere;
}
b {
  flex: 0 0 auto;
  padding: 3px 8px;
  border-radius: 999px;
  background: #ffe8e8;
  color: #df3d3d;
  font-size: 12px;
}
button {
  margin-top: 10px;
  min-height: 36px;
  border: 0;
  background: transparent;
  color: #2563eb;
  font-weight: 800;
  cursor: pointer;
}
</style>
