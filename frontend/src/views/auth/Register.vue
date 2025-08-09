<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <!-- Logo and Title -->
      <div class="flex justify-center">
        <router-link to="/" class="flex items-center">
          <svg class="w-8 h-8 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2L2 7L12 12L22 7L12 2Z"/>
            <path d="M2 17L12 22L22 17"/>
            <path d="M2 12L12 17L22 12"/>
          </svg>
          <span class="ml-2 text-2xl font-bold text-gray-900">ArchMatch</span>
        </router-link>
      </div>
      <h2 class="mt-6 text-center text-3xl font-bold text-gray-900">
        Create your account
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Or
        <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-500">
          sign in to your existing account
        </router-link>
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <!-- Registration Form -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- User Type -->
          <div>
            <label class="form-label">I am a:</label>
            <div class="mt-2 space-y-2">
              <label class="flex items-center">
                <input
                  v-model="form.userType"
                  type="radio"
                  value="CLIENT"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                />
                <span class="ml-2 text-sm text-gray-900">Client looking for an architect</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="form.userType"
                  type="radio"
                  value="ARCHITECT"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                />
                <span class="ml-2 text-sm text-gray-900">Architect offering services</span>
              </label>
            </div>
          </div>

          <!-- Name Fields -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label for="firstName" class="form-label">First name</label>
              <input
                id="firstName"
                v-model="form.firstName"
                type="text"
                required
                class="form-input"
              />
            </div>
            <div>
              <label for="lastName" class="form-label">Last name</label>
              <input
                id="lastName"
                v-model="form.lastName"
                type="text"
                required
                class="form-input"
              />
            </div>
          </div>

          <!-- Email Field -->
          <div>
            <label for="email" class="form-label">Email address</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              autocomplete="email"
              required
              class="form-input"
            />
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="form-label">Password</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              autocomplete="new-password"
              required
              class="form-input"
            />
          </div>

          <!-- Confirm Password -->
          <div>
            <label for="confirmPassword" class="form-label">Confirm password</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              required
              class="form-input"
            />
          </div>

          <!-- Terms and Conditions -->
          <div class="flex items-center">
            <input
              id="acceptTerms"
              v-model="form.acceptTerms"
              type="checkbox"
              required
              class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label for="acceptTerms" class="ml-2 block text-sm text-gray-900">
              I agree to the
              <a href="#" class="text-primary-600 hover:text-primary-500">Terms and Conditions</a>
              and
              <a href="#" class="text-primary-600 hover:text-primary-500">Privacy Policy</a>
            </label>
          </div>

          <!-- Submit Button -->
          <div>
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full btn btn-primary py-3 text-lg"
              :class="{ 'opacity-50 cursor-not-allowed': isLoading }"
            >
              <span v-if="isLoading" class="inline-flex items-center">
                <div class="spinner mr-2"></div>
                Creating account...
              </span>
              <span v-else>Create account</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      form: {
        userType: '',
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        acceptTerms: false
      },
      isLoading: false
    }
  },
  methods: {
    handleSubmit() {
      // TODO: Implement registration logic
      console.log('Register form submitted:', this.form)
      alert('Registration form submitted! (This is a placeholder)')
    }
  }
}
</script>
