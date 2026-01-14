<template>
  <footer class="bg-white border-t border-gray-100 pt-16 pb-8">
    <div class="max-w-7xl mx-auto px-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-12 mb-16">
        <div class="col-span-1 md:col-span-1">
          <div class="mb-4">
            <Logo />
          </div>
          <p class="text-gray-500 text-sm leading-relaxed">{{ t.footer.description }}</p>
        </div>

        <div>
          <h4 class="font-semibold mb-4">{{ t.footer.platform }}</h4>
          <ul class="space-y-3 text-sm text-gray-500">
            <li><router-link to="/how-it-works" class="hover:text-black transition-colors">{{ t.footer.howItWorks }}</router-link></li>
            <li><router-link to="/signup?role=ARCHITECT" class="hover:text-black transition-colors">{{ t.footer.forArchitects }}</router-link></li>
            <li><router-link to="/signup" class="hover:text-black transition-colors">{{ t.footer.signUpIn }}</router-link></li>
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.pricing }}</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold mb-4">{{ t.footer.company }}</h4>
          <ul class="space-y-3 text-sm text-gray-500">
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.about }}</a></li>
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.careers }}</a></li>
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.blog }}</a></li>
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.contact }}</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold mb-4">{{ t.footer.legal }}</h4>
          <ul class="space-y-3 text-sm text-gray-500">
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.terms }}</a></li>
            <li><a href="#" class="hover:text-black transition-colors">{{ t.footer.privacy }}</a></li>
          </ul>
        </div>
      </div>

      <div class="flex flex-col md:flex-row justify-between items-center pt-8 border-t border-gray-100 text-sm text-gray-400">
        <p>&copy; {{ currentYear }} Rumantra Indonesia. {{ t.footer.rights }}</p>
        <div class="flex gap-6 mt-4 md:mt-0">
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
import Logo from '@/components/ui/Logo.vue'

const { t } = useI18n()
const currentYear = computed(() => new Date().getFullYear())
</script>
