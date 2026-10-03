<template>
  <footer class="bg-ink-900 text-white box-border px-6 pt-16 pb-8">
    <div class="max-w-[1328px] mx-auto">
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-12 mb-14">
        <div>
          <div class="text-2xl font-bold tracking-[-.03em] mb-4 text-white">rumantra</div>
          <p class="text-body-sm text-white/70 leading-relaxed max-w-[300px]">{{ t.footer.description }}</p>
        </div>

        <div>
          <h4 class="text-body-sm font-semibold mb-4 text-white">{{ t.footer.platform }}</h4>
          <div class="flex flex-col gap-3 text-body-sm text-white/70">
            <router-link to="/how-it-works" class="hover:text-white transition-colors">{{
              t.footer.howItWorks
            }}</router-link>
            <router-link to="/signup?role=ARCHITECT" class="hover:text-white transition-colors">{{
              t.footer.forArchitects
            }}</router-link>
            <router-link to="/signup" class="hover:text-white transition-colors">{{ t.footer.signUpIn }}</router-link>
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.pricing }}</a>
          </div>
        </div>

        <div>
          <h4 class="text-body-sm font-semibold mb-4 text-white">{{ t.footer.company }}</h4>
          <div class="flex flex-col gap-3 text-body-sm text-white/70">
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.about }}</a>
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.careers }}</a>
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.blog }}</a>
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.contact }}</a>
          </div>
        </div>

        <div>
          <h4 class="text-body-sm font-semibold mb-4 text-white">{{ t.footer.legal }}</h4>
          <div class="flex flex-col gap-3 text-body-sm text-white/70">
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.terms }}</a>
            <a href="#" class="hover:text-white transition-colors">{{ t.footer.privacy }}</a>
          </div>
        </div>
      </div>

      <div
        class="flex flex-col md:flex-row justify-between items-center gap-4 pt-8 border-t border-white/[.16] text-body-sm text-white/70"
      >
        <p class="m-0">&copy; {{ currentYear }} Rumantra Indonesia. {{ t.footer.rights }}</p>
        <div class="flex gap-6">
          <span>Instagram</span>
          <span>LinkedIn</span>
          <span>Twitter</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const currentYear = computed(() => new Date().getFullYear())
</script>
