<template>
  <nav class="flex place-content-around p-4 bg-white shadow-md">
    <div class="flex items-center space-x-6">
      <!-- Logo -->
      <div class="flex items-center space-x-2">
        <div class="w-7 h-7 bg-black"></div>
        <router-link to="/" class="cursor-pointer group" @mouseover="isHovered = true" @mouseleave="isHovered = false">
          <h1
            class="logo"
            :class="{
              'logo-default': !isHovered,
              'logo-hover': isHovered
            }"
          >
            rumantra
          </h1>
        </router-link>
      </div>

      <!-- Navigation Links -->
      <div class="flex items-center space-x-6 ml-6">
        <router-link to="/cara-kerja" class="title-small">{{ $t('nav.links.howItWorks') }}</router-link>
        <router-link to="/pilihan-designer" class="title-small">{{ $t('nav.links.designerSelection') }}</router-link>
        <router-link to="/ide" class="title-small">{{ $t('nav.links.ideas') }}</router-link>
        <router-link to="/artikel" class="title-small">{{ $t('nav.links.articles') }}</router-link>
      </div>
    </div>

    <div class="flex items-center space-x-4">
      <!-- Language Selection -->
      <div class="flex items-center space-x-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5 text-gray-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"
          />
        </svg>
        <select
          class="text-sm text-gray-700 border-none focus:outline-none"
          @change="changeLanguage($event)"
          :value="$i18n.locale.toUpperCase()"
        >
          <option value="ID">ID</option>
          <option value="EN">EN</option>
        </select>
      </div>

      <!-- Auth Buttons / User Menu -->
      <div class="flex space-x-2">
        <template v-if="isAuthenticated">
          <!-- Admin Link (Superuser Only) -->
          <router-link
            v-if="isSuperuser"
            to="/admin/projects"
            class="flex items-center px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition"
          >
            Admin Dashboard
          </router-link>

          <!-- User Profile/Logout -->
          <Button :text="userName" variant="filled" @click="$router.push('/profile')" />
          <Button text="Logout" variant="outlined" @click="handleLogout" />
        </template>

        <template v-else>
          <Button :text="$t('auth.signIn')" variant="filled" @click="openSignIn" />
          <Button :text="$t('auth.signUp')" variant="outlined" @click="openSignUp" />
        </template>
      </div>
    </div>
  </nav>
</template>

<script>
import Button from './ui/Button.vue'
import { useAuthStore } from '@/stores/auth'
import { mapState } from 'pinia'

export default {
  name: 'NavLink',
  components: {
    Button
  },
  data() {
    return {
      isHovered: false
    }
  },
  computed: {
    ...mapState(useAuthStore, ['isAuthenticated', 'isSuperuser', 'userName'])
  },
  methods: {
    openSignIn() {
      this.$emit('open-signin')
    },
    openSignUp() {
      this.$emit('open-signup')
    },
    handleLogout() {
      const authStore = useAuthStore()
      authStore.logout()
      this.$router.push('/')
    },
    changeLanguage(event) {
      const selectedLanguage = event.target.value.toLowerCase()
      this.$i18n.locale = selectedLanguage

      // Optional: Persist language preference in localStorage
      localStorage.setItem('language', selectedLanguage)
    }
  },
  mounted() {
    // Check for saved language preference
    const savedLanguage = localStorage.getItem('language')
    if (savedLanguage) {
      this.$i18n.locale = savedLanguage
    }
  }
}
</script>

<style scoped>
.logo-default {
  background: linear-gradient(to right, #6b7786, #000000, #6b7786, #185c93, #000000, #185c93);
  background-size: 200% 100%;
  background-position: 0% 50%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: background-position 0.8s ease-in-out;
}

.logo-hover {
  background: linear-gradient(to right, #6b7786, #000000, #6b7786, #185c93, #000000, #185c93);
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
