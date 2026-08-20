<template>
  <div v-if="visible" :class="containerClasses">
    <div class="flex items-start gap-3">
      <component :is="iconComponent" :size="18" :class="iconClasses" class="shrink-0 mt-0.5" />
      <div class="flex-1 min-w-0">
        <p v-if="title" :class="titleClasses">{{ title }}</p>
        <div :class="messageClasses">
          <slot />
        </div>
      </div>
      <button v-if="dismissible" :class="dismissClasses" :aria-label="t.common.dismiss" @click="visible = false">
        <X :size="16" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { AlertCircle, CheckCircle2, AlertTriangle, Info, X } from 'lucide-vue-next'

const props = defineProps({
  variant: {
    type: String,
    default: 'info',
    validator: v => ['error', 'success', 'warning', 'info', 'neutral'].includes(v)
  },
  title: {
    type: String,
    default: ''
  },
  dismissible: {
    type: Boolean,
    default: false
  }
})

const { t } = useI18n()

const visible = ref(true)

const config = {
  error: {
    container: 'bg-red-50 border border-red-200 border-l-4 border-l-red-500',
    icon: AlertCircle,
    iconColor: 'text-red-500',
    title: 'text-red-800 font-semibold text-sm',
    message: 'text-red-700 text-sm',
    dismiss: 'text-red-400 hover:text-red-600'
  },
  success: {
    container: 'bg-green-50 border border-green-200 border-l-4 border-l-green-500',
    icon: CheckCircle2,
    iconColor: 'text-green-500',
    title: 'text-green-800 font-semibold text-sm',
    message: 'text-green-700 text-sm',
    dismiss: 'text-green-400 hover:text-green-600'
  },
  warning: {
    container: 'bg-amber-50 border border-amber-200 border-l-4 border-l-amber-500',
    icon: AlertTriangle,
    iconColor: 'text-amber-500',
    title: 'text-amber-800 font-semibold text-sm',
    message: 'text-amber-700 text-sm',
    dismiss: 'text-amber-400 hover:text-amber-600'
  },
  info: {
    container: 'bg-blue-50 border border-blue-200 border-l-4 border-l-blue-500',
    icon: Info,
    iconColor: 'text-blue-500',
    title: 'text-blue-800 font-semibold text-sm',
    message: 'text-blue-700 text-sm',
    dismiss: 'text-blue-400 hover:text-blue-600'
  },
  neutral: {
    container: 'bg-gray-50 border border-gray-200 border-l-4 border-l-gray-400',
    icon: Info,
    iconColor: 'text-gray-400',
    title: 'text-gray-700 font-semibold text-sm',
    message: 'text-gray-600 text-sm',
    dismiss: 'text-gray-400 hover:text-gray-600'
  }
}

const containerClasses = computed(() => `rounded-xl p-4 ${config[props.variant].container}`)
const iconComponent = computed(() => config[props.variant].icon)
const iconClasses = computed(() => config[props.variant].iconColor)
const titleClasses = computed(() => `${config[props.variant].title} mb-0.5`)
const messageClasses = computed(() => config[props.variant].message)
const dismissClasses = computed(() => `${config[props.variant].dismiss} transition-colors shrink-0 -mt-0.5`)
</script>
