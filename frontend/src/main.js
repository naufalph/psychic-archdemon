import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// Import global styles
import './assets/main.css'

// Create Vue app instance
const app = createApp(App)

// Install plugins
app.use(createPinia())
app.use(router)

// Global properties (if needed)
app.config.globalProperties.$API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// Global error handler
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err)
  console.error('Component:', vm)
  console.error('Info:', info)

  // In production, you might want to send this to a logging service
  if (import.meta.env.PROD) {
    // Send to logging service
    console.log('Would send to logging service in production')
  }
}

// Mount the app
app.mount('#app')
