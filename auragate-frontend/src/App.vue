<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
onMounted(() => {
  if (import.meta.env.VITE_APP_TITLE) {
    document.title = import.meta.env.VITE_APP_TITLE
  }
})
</script>
