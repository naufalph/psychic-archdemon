<template>
  <router-link
    to="/"
    class="text-2xl font-black tracking-tighter logo"
    :class="{ 'logo-default': !isHovered, 'logo-hover': isHovered }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    Rumantra.
  </router-link>
</template>

<script setup>
import { ref } from 'vue'

const isHovered = ref(false)
</script>

<style scoped>
.logo-default {
  background: linear-gradient(
    to right,
    #6b7786,
    theme('colors.black'),
    #6b7786,
    #185c93,
    theme('colors.black'),
    #185c93
  );
  background-size: 200% 100%;
  background-position: 0% 50%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: background-position 0.8s ease-in-out;
}

.logo-hover {
  background: linear-gradient(
    to right,
    #6b7786,
    theme('colors.black'),
    #6b7786,
    #185c93,
    theme('colors.black'),
    #185c93
  );
  background-size: 200% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
