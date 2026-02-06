<template>
  <nav :class="navClasses">
    <div class="max-w-7xl mx-auto px-6 flex justify-between items-center">
      <div class="flex items-center gap-12">
        <Logo />

        <div class="hidden md:flex items-center gap-10">
          <router-link to="/" class="text-sm font-bold text-gray-900 hover:text-brand-brown transition-colors">{{
            t.nav.home
          }}</router-link>
          <router-link
            to="/how-it-works"
            class="text-sm font-bold text-gray-900 hover:text-brand-brown transition-colors"
            >{{ t.nav.howItWorks }}</router-link
          >
          <router-link
            to="/signup?role=ARCHITECT"
            class="text-sm font-bold text-gray-900 hover:text-brand-brown transition-colors"
            >{{ t.nav.forArchitects }}</router-link
          >
        </div>
      </div>

      <div class="hidden md:flex items-center gap-4">
        <LanguageSwitcher />
        <router-link to="/login">
          <BaseButton variant="ghost" class="px-6 text-sm font-bold text-gray-900">{{ t.nav.signIn }}</BaseButton>
        </router-link>
        <router-link to="/signup">
          <BaseButton variant="primary" class="px-8 py-3 text-sm font-bold rounded-full">{{ t.nav.signUp }}</BaseButton>
        </router-link>
      </div>

      <button class="md:hidden text-black" @click="isMobileMenuOpen = !isMobileMenuOpen">
        <component :is="isMobileMenuOpen ? X : Menu" :size="24" />
      </button>
    </div>

    <div
      v-if="isMobileMenuOpen"
      class="absolute top-full left-0 w-full bg-white shadow-xl p-8 flex flex-col gap-6 md:hidden border-b border-gray-100"
    >
      <router-link to="/" class="text-lg font-bold">{{ t.nav.home }}</router-link>
      <router-link to="/how-it-works" class="text-lg font-bold">{{ t.nav.howItWorks }}</router-link>
      <router-link to="/signup?role=ARCHITECT" class="text-lg font-bold">{{ t.nav.forArchitects }}</router-link>
      <hr class="border-gray-100" />
      <div class="flex flex-col gap-4">
        <LanguageSwitcher />
        <router-link to="/login">
          <BaseButton variant="ghost" :fullWidth="true" class="font-bold">{{ t.nav.signIn }}</BaseButton>
        </router-link>
        <router-link to="/signup">
          <BaseButton variant="primary" :fullWidth="true" class="font-bold">{{ t.nav.signUp }}</BaseButton>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import BaseButton from '@/components/ui/BaseButton.vue'
import LanguageSwitcher from './LanguageSwitcher.vue'
import Logo from '@/components/ui/Logo.vue'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const route = useRoute()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)

const navClasses = computed(() => {
  const base = 'fixed w-full z-50 transition-all duration-300 py-4'
  return isScrolled.value ? `${base} bg-white/95 backdrop-blur-md shadow-sm` : `${base} bg-transparent`
})

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

watch(
  () => route.path,
  () => {
    isMobileMenuOpen.value = false
  }
)

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>
