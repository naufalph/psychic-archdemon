<template>
  <div
    class="relative overflow-hidden bg-white border border-hairline rounded-[20px] px-8 py-[clamp(48px,7vw,96px)] animate-brief-rise flex flex-col items-center text-center gap-4"
  >
    <div class="absolute left-1/2 top-0" aria-hidden="true">
      <ConfettiExplosion
        :particle-count="80"
        :force="0.4"
        :duration="3200"
        :stage-height="520"
        :colors="['#0A0A0A', '#B08B63', '#2F5566', '#D9CBB8', '#7A8F6B']"
      />
    </div>
    <span class="relative w-14 h-14 rounded-full bg-ink-900 text-white flex items-center justify-center">
      <Check :size="26" :stroke-width="3" />
    </span>
    <h1 class="relative text-[clamp(28px,3.5vw,40px)] font-bold tracking-[-.03em] leading-[1.15] m-0">
      Pendaftaran Anda selesai
    </h1>
    <p class="relative text-body leading-relaxed text-ink-400 m-0 max-w-[460px]">
      Profil Anda sedang kami verifikasi. Kami akan memberi tahu Anda setelah selesai. Sambil menunggu, lengkapi dokumen
      dan portofolio Anda di dashboard.
    </p>
    <span
      class="relative inline-flex items-center gap-2 h-9 px-4 rounded-full bg-surface-alt border border-hairline text-[15px] font-bold text-ink-900 animate-pts-pop"
    >
      <Star :size="16" :stroke-width="2" /> +10 Pts
    </span>
    <router-link
      to="/architect/dashboard"
      class="relative h-[52px] px-8 mt-2 rounded-full bg-ink-900 text-white text-[17px] font-bold inline-flex items-center hover:opacity-85 transition-opacity"
    >
      Masuk dashboard
    </router-link>
  </div>
</template>

<script setup>
import ConfettiExplosion from 'vue-confetti-explosion'
import { Check, Star } from 'lucide-vue-next'
</script>

<style scoped>
.animate-pts-pop {
  animation: ptsPop 0.6s 0.5s cubic-bezier(0.2, 0.7, 0.2, 1) both;
}
@keyframes ptsPop {
  0% {
    opacity: 0;
    transform: scale(0.6);
  }
  60% {
    opacity: 1;
    transform: scale(1.12);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
