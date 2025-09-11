<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <NavLink 
      @open-signin="showSignInPopup = true" 
      @open-signup="showSignUpPopup = true"
    />

    <router-view v-slot="{ Component }">
      <transition
        name="fade"
        mode="out-in"
        enter-active-class="animate-fade-in"
        leave-active-class="animate-fade-in"
      >
        <component :is="Component" />
      </transition>
    </router-view>
    
    <SignInPopup 
      :visible="showSignInPopup" 
      :initial-view="initialPopupView"
      @close="closePopup"
      @success="handleAuthSuccess"
    />
  </div>
</template>

<script>
import NavLink from '@/components/NavLink.vue'
import SignInPopup from '@/components/auth/SignInPopup.vue'
import { useAuthStore } from './stores/auth'

export default {
  name: 'App',
  components: {
    NavLink,
    SignInPopup
  },
  data() {
    return {
      showSignInPopup: false,
      showSignUpPopup: false,
      initialPopupView: 'signup'
    }
  },
  methods: {
    closePopup() {
      this.showSignInPopup = false
      this.showSignUpPopup = false
    },
    handleAuthSuccess(authData) {
      console.log('Authentication successful:', authData)
      const authStore = useAuthStore()
      
      if (authData.type === 'login') {
        this.$router.push('/dashboard')
      } else if (authData.type === 'register') {
        this.$router.push('/profile')
      }
    }
  }
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Barlow:wght@400;600;700&display=swap');

/* Transition styles */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Custom animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
</style>
