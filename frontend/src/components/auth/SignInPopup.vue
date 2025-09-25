<template>
  <!-- Modal Overlay -->
  <div
    v-if="isVisible"
    class="fixed inset-0 z-50 overflow-y-auto"
    @click.self="closeModal"
  >
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div
        class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
        @click="closeModal"
      ></div>

      <!-- Modal positioning -->
      <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>

      <!-- Modal content -->
      <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
        <!-- Sign Up Pop-Up (Social Login) -->
        <div v-if="currentView === 'signup'" class="w-full max-w-md mx-auto">
          <div class="text-left mb-8">
            <h2 class="title-medium mb-2 hover:text-black">{{ $t('auth.signup.title') }}</h2>
            <p class="body-small text-gray-600">
              {{ $t('auth.signup.alreadyHaveAccount') }}
              <button 
                @click="switchToSignIn" 
                class="text-primary-600 hover:text-primary-700 font-medium"
              >
                {{ $t('auth.signIn') }}
              </button>
            </p>
          </div>

          <div>
            <!-- LinkedIn -->
              <button
                @click="handleSocialLogin('linkedin')"
                class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5"
              >
                <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                </svg>
                {{ $t('auth.signup.socialLogin.linkedin') }}
              </button>

            <!-- Email -->
            <button
              @click="switchToEmailSignup"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5"
            >
              <svg class="w-5 h-5 mr-3 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.email') }}
            </button>

            <!-- Divider -->
            <div class="flex items-center my-5 ">
              <div class="flex-grow border-t border-gray-300"></div>
              <div class="mx-4 text-sm text-gray-500">{{ $t('auth.or') }}</div>
              <div class="flex-grow border-t border-gray-300"></div>
            </div>

            <!-- Google -->
            <button
              @click="handleSocialLogin('google')"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5" 
            >
              <svg class="w-5 h-5 mr-3" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.google') }}
            </button>

            <!-- Facebook -->
            <button
              @click="handleSocialLogin('facebook')"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black "
            >
              <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.facebook') }}
            </button>
          </div>

          <div class="mt-6 text-left">
            <p class="text-xxs text-gray-500">
              {{ $t('auth.signup.termsAndPrivacy.prefix') }}
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }} </a> 
              {{ $t('auth.signup.termsAndPrivacy.and') }} 
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
            </p>
          </div>
        </div>

        <!-- Email Sign Up Form -->
        <div v-else-if="currentView === 'emailSignup'" class="w-full max-w-md mx-auto">
          <div class="mb-8">
            <div class="flex items-center mb-2">
              <button
                @click="switchToSignup"
                class="mr-2 p-1 rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
              </button>
              <span class="text-xxs text-gray-600">{{ $t('auth.emailSignup.back') }}</span>
            </div>
            <h2 class="title-medium text-gray-900">{{ $t('auth.emailSignup.title') }}</h2>
          </div>

          <form @submit.prevent="handleEmailSignup" class="space-y-4 pt-6">
            <div>
              <label for="email" class="block text-xxs text-gray-700 mb-2">Email</label>
              <input
                id="email"
                v-model="emailForm.email"
                type="email"
                required
                class="w-full h-10 px-4 py-3 border border-gray-300 rounded-full body-medium placeholder-gray-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                :placeholder="$t('auth.emailSignup.emailPlaceholder')"
              >
              <p v-if="emailForm.errors.email" class="form-error mt-1 text-red-500 body-small">{{ emailForm.errors.email }}</p>
            </div>

            <div class="relative">
              <label for="password" class="block text-xxs text-gray-700 mb-2">Password</label>
              <input
                id="password"
                v-model="emailForm.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="w-full h-10 px-4 py-3 pr-12 border border-gray-300 rounded-full body-medium placeholder-gray-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                :placeholder="$t('auth.emailSignup.passwordPlaceholder')"
              >
              <button
                type="button"
                @click="togglePasswordVisibility"
                class="absolute right-3 top-8 p-1 text-gray-400 hover:text-gray-600 focus:outline-none"
              >
                <svg v-if="showPassword" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <p v-if="emailForm.errors.password" class="form-error mt-1 text-red-500 body-small">{{ emailForm.errors.password }}</p>
            </div>

            <!-- General error message -->
            <div v-if="emailForm.errors.general" class="p-3 bg-red-50 border border-red-200 rounded-lg">
              <p class="text-red-700 body-small">{{ emailForm.errors.general }}</p>
            </div>

            <Button
              :text="$t('auth.emailSignup.createAccount')"
              variant="filled"
              size="medium"
              :disabled="isLoading"
              @click="handleEmailSignup"
              class="w-full"
            >
              <span v-if="isLoading" class="spinner mr-2"></span>
            </Button>
          </form>

          <div class="mt-1 mb-8 text-left">
            <p class="text-xxs text-gray-500">
              {{ $t('auth.signup.termsAndPrivacy.prefix') }}
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }} </a> 
              {{ $t('auth.signup.termsAndPrivacy.and') }} 
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
            </p>
          </div>
        </div>

        <!-- Sign In Form -->
        <div v-else-if="currentView === 'signin'" class="w-full max-w-md mx-auto">
          <div class="text-left mb-8">
            <h2 class="title-medium mb-2 hover:text-black">{{ $t('auth.signin.title') }}</h2>
            <p class="body-small text-gray-600">
              {{ $t('auth.signin.noAccount') }}
              <button 
                @click="switchToSignup" 
                class="text-primary-600 hover:text-primary-700 font-medium"
              >
                {{ $t('auth.signin.signUp') }}
              </button>
            </p>
          </div>

          <div>
            <!-- LinkedIn -->
              <button
                @click="handleSocialLogin('linkedin')"
                class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5"
              >
                <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                </svg>
                {{ $t('auth.signin.socialLogin.linkedin') }}
              </button>

            <!-- Email -->
            <button
              @click="switchToEmailSignup"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5"
            >
              <svg class="w-5 h-5 mr-3 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              {{ $t('auth.signin.socialLogin.email') }}
            </button>

            <!-- Divider -->
            <div class="flex items-center my-5 ">
              <div class="flex-grow border-t border-gray-300"></div>
              <div class="mx-4 text-sm text-gray-500">{{ $t('auth.or') }}</div>
              <div class="flex-grow border-t border-gray-300"></div>
            </div>

            <!-- Google -->
            <button
              @click="handleSocialLogin('google')"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black mb-5" 
            >
              <svg class="w-5 h-5 mr-3" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              {{ $t('auth.signin.socialLogin.google') }}
            </button>

            <!-- Facebook -->
            <button
              @click="handleSocialLogin('facebook')"
              class="w-full h-[40px] flex items-center justify-center px-4 py-3 rounded-[100px] border border-black shadow-sm bg-white body-medium hover:bg-[#E6E6E6] hover:text-black "
            >
              <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
              {{ $t('auth.signin.socialLogin.facebook') }}
            </button>
          </div>

          <div class="mt-6 text-left">
            <p class="text-xxs text-gray-500">
              {{ $t('auth.signup.termsAndPrivacy.prefix') }}
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }} </a> 
              {{ $t('auth.signup.termsAndPrivacy.and') }} 
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
            </p>
          </div>

        </div>

        <!-- Email Sign Up Form -->
        <div v-else-if="currentView === 'emailSignIn'" class="w-full max-w-md mx-auto">
          <div class="mb-8">
            <div class="flex items-center mb-2">
              <button
                @click="switchToSignIn"
                class="mr-2 p-1 rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
              </button>
              <span class="text-xxs text-gray-600">{{ $t('auth.emailSignin.back') }}</span>
            </div>
            <h2 class="title-medium text-gray-900">{{ $t('auth.emailSignin.title') }}</h2>
          </div>

          <form @submit.prevent="handleEmailSignup" class="space-y-4 pt-6">
            <div>
              <label for="email" class="block text-xxs text-gray-700 mb-2">Email</label>
              <input
                id="email"
                v-model="emailForm.email"
                type="email"
                required
                class="w-full h-10 px-4 py-3 border border-gray-300 rounded-full body-medium placeholder-gray-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                :placeholder="$t('auth.emailSignup.emailPlaceholder')"
              >
              <p v-if="emailForm.errors.email" class="form-error mt-1 text-red-500 body-small">{{ emailForm.errors.email }}</p>
            </div>

            <div class="relative">
              <label for="password" class="block text-xxs text-gray-700 mb-2">Password</label>
              <input
                id="password"
                v-model="emailForm.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="w-full h-10 px-4 py-3 pr-12 border border-gray-300 rounded-full body-medium placeholder-gray-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                :placeholder="$t('auth.emailSignup.passwordPlaceholder')"
              >
              <button
                type="button"
                @click="togglePasswordVisibility"
                class="absolute right-3 top-8 p-1 text-gray-400 hover:text-gray-600 focus:outline-none"
              >
                <svg v-if="showPassword" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <p v-if="emailForm.errors.password" class="form-error mt-1 text-red-500 body-small">{{ emailForm.errors.password }}</p>
            </div>
            
            <Button
              :text="$t('auth.emailSignup.createAccount')" 
              variant="filled"
              size="medium"
              :disabled="isLoading"
              @click="handleEmailSignup"
              class="w-full"
            >
              <span v-if="isLoading" class="spinner mr-2"></span>
            </Button>
          </form>

          <div class="mt-1 mb-8 text-left">
            <p class="text-xxs text-gray-500">
              {{ $t('auth.signup.termsAndPrivacy.prefix') }}
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }} </a> 
              {{ $t('auth.signup.termsAndPrivacy.and') }} 
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
            </p>
          </div>
        </div>

        <!-- Close button -->
        <div class="absolute top-0 right-0 pt-4 pr-4">
          <button
            @click="closeModal"
            class="bg-white rounded-md text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <span class="sr-only">Close</span>
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/Button.vue'

export default {
  name: 'SignInPopup',
  components: {
    Button
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    initialView: {
      type: String,
      default: 'signup', // 'signup', 'signin', 'emailSignup'
      validator: (value) => ['signup', 'signin', 'emailSignup'].includes(value)
    }
  },
  emits: ['close', 'success'],
  data() {
    return {
      currentView: this.initialView,
      isLoading: false,
      showPassword: false,
      showSigninPassword: false,
      emailForm: {
        email: '',
        password: '',
        errors: {}
      },
      signinForm: {
        email: '',
        password: '',
        rememberMe: false,
        errors: {}
      }
    }
  },
  computed: {
    isVisible() {
      return this.visible
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.currentView = this.initialView
        this.resetForms()
      }
    },
    initialView(newVal) {
      this.currentView = newVal
    }
  },
  methods: {
    ...mapActions(useAuthStore, ['login', 'register', 'loginWithGoogle', 'loginWithLinkedIn']),

    closeModal() {
      this.$emit('close')
    },

    switchToSignup() {
      this.currentView = 'signup'
      this.resetForms()
    },

    switchToSignIn() {
      this.currentView = 'signin'
      this.resetForms()
    },

    switchToEmailSignup() {
      this.currentView = 'emailSignup'
      this.resetForms()
    },

    switchToEmailSignIn() {
      this.currentView = 'emailSignIn'
      this.resetForms()
    },

    togglePasswordVisibility() {
      this.showPassword = !this.showPassword
    },

    toggleSigninPasswordVisibility() {
      this.showSigninPassword = !this.showSigninPassword
    },

    resetForms() {
      this.emailForm = {
        email: '',
        password: '',
        errors: {}
      }
      this.signinForm = {
        email: '',
        password: '',
        rememberMe: false,
        errors: {}
      }
      this.isLoading = false
    },

    validateEmailForm() {
      const errors = {}
      
      if (!this.emailForm.email) {
        errors.email = 'Email harus diisi'
      } else if (!/\S+@\S+\.\S+/.test(this.emailForm.email)) {
        errors.email = 'Format email tidak valid'
      }

      if (!this.emailForm.password) {
        errors.password = 'Password harus diisi'
      } else if (this.emailForm.password.length < 6) {
        errors.password = 'Password minimal 6 karakter'
      }

      this.emailForm.errors = errors
      return Object.keys(errors).length === 0
    },

    validateSigninForm() {
      const errors = {}
      
      if (!this.signinForm.email) {
        errors.email = 'Email harus diisi'
      } else if (!/\S+@\S+\.\S+/.test(this.signinForm.email)) {
        errors.email = 'Format email tidak valid'
      }

      if (!this.signinForm.password) {
        errors.password = 'Password harus diisi'
      }

      this.signinForm.errors = errors
      return Object.keys(errors).length === 0
    },

    async handleEmailSignup() {
      if (!this.validateEmailForm()) {
        return
      }

      this.isLoading = true

      try {
        const userData = {
          email: this.emailForm.email,
          password: this.emailForm.password,
          role:'CLIENT'
        }

        const result = await this.register(userData)

        this.$emit('success', { type: 'register', message: result.message })
        this.closeModal()

        // Show success message about email verification
        this.$toast?.success('Registrasi berhasil! Silakan cek email Anda untuk verifikasi akun.')

      } catch (error) {
        console.error('Registration failed:', error)

        if (error.response?.status === 400) {
          // Handle validation errors from backend ApiResponse
          const errorMessage = error.response.data?.message || 'Terjadi kesalahan validasi'
          this.emailForm.errors.general = errorMessage
        } else if (error.response?.data?.message) {
          // Handle other backend errors with message
          this.emailForm.errors.general = error.response.data.message
        } else {
          this.emailForm.errors.general = 'Terjadi kesalahan. Silakan coba lagi.'
        }
      } finally {
        this.isLoading = false
      }
    },

    async handleSignIn() {
      if (!this.validateSigninForm()) {
        return
      }

      this.isLoading = true

      try {
        const credentials = {
          email: this.signinForm.email,
          password: this.signinForm.password
        }

        const result = await this.login(credentials)
        
        this.$emit('success', { type: 'login', user: result.user })
        this.closeModal()
        
        // Show success message
        this.$toast?.success(`Selamat datang kembali, ${result.user.firstName || result.user.email}!`)
        
      } catch (error) {
        console.error('Login failed:', error)
        
        if (error.response?.status === 401) {
          this.signinForm.errors.general = 'Email atau password salah'
        } else if (error.response?.status === 423) {
          this.signinForm.errors.general = 'Akun terkunci. Silakan coba lagi nanti.'
        } else {
          this.signinForm.errors.general = 'Terjadi kesalahan. Silakan coba lagi.'
        }
      } finally {
        this.isLoading = false
      }
    },

    async handleSocialLogin(provider) {
      this.isLoading = true

      try {
        console.log(`Initiating ${provider} login...`)

        if (provider === 'google') {
          await this.loginWithGoogle()
        } else if (provider === 'linkedin') {
          await this.loginWithLinkedIn()
        } else {
          // Facebook not implemented yet
          this.$toast?.error(`${provider} login belum tersedia.`)
        }

      } catch (error) {
        console.error(`${provider} login failed:`, error)
        this.$toast?.error(`Gagal masuk dengan ${provider}. Silakan coba lagi.`)
      } finally {
        this.isLoading = false
      }
    }
  },

  mounted() {
    // Handle escape key
    const handleEscape = (e) => {
      if (e.key === 'Escape' && this.isVisible) {
        this.closeModal()
      }
    }

    document.addEventListener('keydown', handleEscape)
    
  },

  beforeUnmount() {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && this.isVisible) {
        this.closeModal()
      }
    }
    document.removeEventListener('keydown', handleEscape)
  }
}
</script>

<style scoped>
/* Additional custom styles if needed */
.spinner {
  border-top-color: transparent;
}

/* Smooth transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Focus styles for better accessibility */
button:focus,
input:focus {
  outline: none;
}

/* Custom scrollbar for modal content */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
