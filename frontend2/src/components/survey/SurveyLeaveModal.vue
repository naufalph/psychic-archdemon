<template>
  <BriefModal :open="open" panel-class="max-w-[480px] p-8" @close="$emit('stay')">
    <button
      type="button"
      aria-label="Tutup"
      class="absolute top-5 right-5 w-11 h-11 rounded-full border border-hairline bg-white text-ink-900 flex items-center justify-center hover:border-ink-900"
      @click="$emit('stay')"
    >
      <X :size="18" :stroke-width="2.2" />
    </button>

    <span class="w-14 h-14 rounded-full bg-surface-alt text-ink-900 flex items-center justify-center">
      <Bookmark :size="24" :stroke-width="2" />
    </span>
    <h2 class="text-[24px] font-bold tracking-[-.025em] leading-[1.2] mt-5 mb-2 mr-14">Tinggalkan brief proyek?</h2>
    <p v-if="saveFailed" class="text-[15px] leading-relaxed text-ink-400 mt-0 mb-7">
      Perubahan terakhir belum berhasil tersimpan ke akun Anda. Kami akan mencoba menyimpannya lagi saat Anda keluar,
      dan salinannya tetap ada di browser ini.
    </p>
    <p v-else class="text-[15px] leading-relaxed text-ink-400 mt-0 mb-7">
      Brief Anda belum dikirim, tapi progresnya sudah tersimpan di akun Anda. Lanjutkan kapan saja dari
      <strong class="text-ink-900">dashboard</strong>, di bagian paling atas.
    </p>
    <div class="flex flex-col gap-3">
      <button
        type="button"
        class="w-full h-[52px] rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity"
        @click="$emit('stay')"
      >
        Lanjutkan brief proyek
      </button>
      <button
        type="button"
        class="w-full h-[52px] rounded-full border border-hairline bg-white text-ink-900 text-[15px] font-bold hover:border-ink-900 transition-colors"
        @click="$emit('leave')"
      >
        Ke dashboard
      </button>
    </div>
  </BriefModal>
</template>

<script setup>
import { X, Bookmark } from 'lucide-vue-next'
import BriefModal from '@/components/brief/BriefModal.vue'

defineProps({
  open: { type: Boolean, default: false },
  saveFailed: { type: Boolean, default: false }
})
defineEmits(['stay', 'leave'])
</script>
