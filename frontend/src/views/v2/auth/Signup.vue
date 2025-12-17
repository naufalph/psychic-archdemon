<template>
  <div class="flex h-screen w-screen overflow-hidden relative">
    <!-- Background Gradient for entire page -->
    <div class="absolute inset-0" style="background: linear-gradient(180deg, #9e9d99 0%, #6c6762 100%)"></div>

    <!-- Angled Gradient Overlay Vector -->
    <div
      class="absolute opacity-30"
      style="
        left: -207px;
        top: 111px;
        width: 2000px;
        height: 750px;
        background: linear-gradient(0deg, rgba(158, 157, 153, 1) 14%, rgba(98, 94, 89, 1) 64%, rgba(19, 17, 15, 1) 86%);
        transform: rotate(10deg);
        transform-origin: top left;
      "
    ></div>

    <!-- Left Side: Hero Section -->
    <div class="hidden md:block md:w-1/2 lg:w-[60%] relative z-10">
      <SignupHero />
    </div>

    <!-- Right Side: Signup Form -->
    <div class="w-full md:w-1/2 lg:w-[40%] flex items-center justify-center p-8 relative z-10 overflow-y-auto">
      <SignupForm />
    </div>
  </div>
</template>

<script setup>
import SignupHero from './components/SignupHero.vue'
import SignupForm from './components/SignupForm.vue'
</script>
