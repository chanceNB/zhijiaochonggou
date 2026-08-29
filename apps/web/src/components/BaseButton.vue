<template>
  <button class="base-button" :type="type" :disabled="disabled || loading">
    <span v-if="loading" aria-hidden="true">...</span>
    <slot />
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset'
    disabled?: boolean
    loading?: boolean
  }>(),
  {
    type: 'button',
    disabled: false,
    loading: false,
  },
)
</script>
