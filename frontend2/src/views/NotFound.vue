<template>
  <div class="min-h-screen bg-[#F4F5F7] flex items-center justify-center p-6">
    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div
      v-motion
      :initial="{ opacity: 0, y: 20 }"
      :enter="{ opacity: 1, y: 0 }"
      class="text-center relative z-10 max-w-2xl"
    >
      <div class="mb-8">
        <router-link to="/" class="inline-block">
          <Logo class="h-10" />
        </router-link>
      </div>

      <div
        v-motion
        :initial="{ opacity: 0, scale: 0.9 }"
        :enter="{ opacity: 1, scale: 1 }"
        :delay="200"
        class="bg-white rounded-3xl shadow-2xl p-16 border border-gray-100"
      >
        <div class="w-32 h-32 bg-[#F5E6D3] rounded-full flex items-center justify-center mx-auto mb-8">
          <span class="text-6xl font-black text-[#7C4728]">404</span>
        </div>

        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-black">
          {{ t.notFound?.title || 'Page Not Found' }}
        </h1>
        <p class="text-lg text-gray-500 mb-12 leading-relaxed max-w-md mx-auto">
          {{
            t.notFound?.message ||
            'The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.'
          }}
        </p>

        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link to="/">
            <BaseButton class="bg-[#C5A17A] hover:bg-[#B39069] text-white border-none px-12">
              {{ t.notFound?.backHome || 'Back to Home' }}
            </BaseButton>
          </router-link>
          <button
            @click="$router.go(-1)"
            class="px-10 py-3.5 text-base font-bold tracking-tight rounded-full border-2 border-gray-200 hover:bg-gray-50 hover:border-gray-300 transition-all"
          >
            {{ t.notFound?.goBack || 'Go Back' }}
          </button>
        </div>
      </div>

      <div
        v-motion
        :initial="{ opacity: 0 }"
        :enter="{ opacity: 1 }"
        :delay="400"
        class="mt-12 grid grid-cols-1 sm:grid-cols-3 gap-6"
      >
        <router-link
          to="/"
          class="bg-white/60 backdrop-blur-sm rounded-2xl p-6 border border-gray-100 hover:shadow-lg hover:bg-white transition-all group"
        >
          <Home class="w-8 h-8 text-[#C5A17A] mb-3 mx-auto group-hover:scale-110 transition-transform" />
          <p class="font-semibold text-gray-800">{{ t.nav?.home || 'Home' }}</p>
        </router-link>

        <router-link
          to="/login"
          class="bg-white/60 backdrop-blur-sm rounded-2xl p-6 border border-gray-100 hover:shadow-lg hover:bg-white transition-all group"
        >
          <LogIn class="w-8 h-8 text-[#7C4728] mb-3 mx-auto group-hover:scale-110 transition-transform" />
          <p class="font-semibold text-gray-800">{{ t.nav?.signIn || 'Sign In' }}</p>
        </router-link>

        <router-link
          to="/signup"
          class="bg-white/60 backdrop-blur-sm rounded-2xl p-6 border border-gray-100 hover:shadow-lg hover:bg-white transition-all group"
        >
          <UserPlus class="w-8 h-8 text-brand-green mb-3 mx-auto group-hover:scale-110 transition-transform" />
          <p class="font-semibold text-gray-800">{{ t.nav?.signUp || 'Sign Up' }}</p>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import Logo from '@/components/ui/Logo.vue'
import { Home, LogIn, UserPlus } from 'lucide-vue-next'

const { t } = useI18n()
</script>
