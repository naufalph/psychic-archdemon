<template>
  <BriefModal :open="!!service" panel-class="max-w-[480px] p-8" @close="$emit('close')">
    <button
      type="button"
      aria-label="Tutup"
      class="absolute top-5 right-5 w-11 h-11 rounded-full border border-hairline bg-white text-ink-900 flex items-center justify-center hover:border-ink-900"
      @click="$emit('close')"
    >
      <X :size="18" :stroke-width="2.2" />
    </button>

    <template v-if="!sent">
      <span
        class="inline-flex items-center h-[22px] px-2.5 rounded-full border border-hairline text-ink-400 text-micro-sm uppercase tracking-[.05em]"
      >
        Segera tersedia
      </span>
      <h2 class="text-[24px] font-bold tracking-[-.025em] leading-[1.2] mt-4 mb-2 mr-14">
        Kabari saya saat {{ service?.label }} tersedia
      </h2>
      <p class="text-[15px] leading-relaxed text-ink-400 mt-0 mb-6">
        Kami kirim email saat layanan ini dibuka, beserta kabar terbaru dari rumantra.
      </p>
      <form class="flex flex-col gap-4" novalidate @submit.prevent="submit">
        <div>
          <label :for="emailId" class="block text-body font-medium text-ink-700 mb-2.5">Email</label>
          <input
            :id="emailId"
            v-model="email"
            type="email"
            autocomplete="email"
            placeholder="cth. budi@email.com"
            class="w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors"
          />
        </div>
        <p v-if="error" class="text-caption text-[#B42318] m-0">Masukkan email yang valid.</p>
        <button
          type="submit"
          class="w-full h-[52px] rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity"
        >
          Kabari saya
        </button>
      </form>
    </template>

    <div v-else class="flex flex-col items-start gap-4 pt-2">
      <span class="w-14 h-14 rounded-full bg-ink-900 text-white flex items-center justify-center">
        <Check :size="26" :stroke-width="3" />
      </span>
      <h2 class="text-[24px] font-bold tracking-[-.025em] m-0">Anda sudah terdaftar</h2>
      <p class="text-[15px] leading-relaxed text-ink-400 m-0">
        Kami akan mengirim kabar ke <strong class="text-ink-900">{{ email }}</strong> saat {{ service?.label }}
        tersedia.
      </p>
      <button
        type="button"
        class="w-full h-[52px] mt-2 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity"
        @click="$emit('close')"
      >
        Kembali ke brief
      </button>
    </div>
  </BriefModal>
</template>

<script setup>
import { ref, watch, useId } from 'vue'
import { X, Check } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import BriefModal from '@/components/brief/BriefModal.vue'

const props = defineProps({ service: { type: Object, default: null } })
defineEmits(['close'])

const authStore = useAuthStore()
const { state } = useProjectSurvey()
const emailId = useId()

const email = ref('')
const error = ref(false)
const sent = ref(false)

watch(
  () => props.service,
  service => {
    if (!service) return
    sent.value = false
    error.value = false
    email.value = email.value || authStore.user?.email || ''
  }
)

watch(email, () => {
  error.value = false
})

// No waitlist endpoint exists yet, so the interest is only remembered in this browser
const submit = () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) {
    error.value = true
    return
  }
  state.notified = { ...state.notified, [props.service.id]: true }
  sent.value = true
}
</script>
