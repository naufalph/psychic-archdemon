<template>
  <div>
    <OnbHeading
      title="Anda bekerja sebagai apa?"
      lead="Pilihan ini menentukan data yang kami minta di langkah berikutnya. Anda bisa mengubahnya nanti di pengaturan profil."
    />

    <div class="bg-white border border-hairline rounded-[20px] p-6 md:p-8 animate-brief-rise">
      <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-4" role="radiogroup" aria-label="Jenis usaha">
        <OnbOptionCard
          v-for="o in JENIS_USAHA_OPTIONS"
          :key="o.id"
          role="radio"
          :title="o.title"
          :desc="o.desc"
          :icon="ICONS[o.icon]"
          :on="state.jenis === o.id"
          @pick="state.jenis = o.id"
        >
          <template #default="{ on }">
            <span
              class="mt-auto flex flex-col gap-2 pt-4 pb-6 border-t"
              :class="on ? 'border-white/20' : 'border-hairline'"
            >
              <span class="text-micro-sm uppercase tracking-[.05em]" :class="on ? 'text-white/70' : 'text-ink-400'">
                Data yang diminta
              </span>
              <span class="flex flex-wrap gap-1.5">
                <span
                  v-for="n in o.needs"
                  :key="n"
                  class="inline-flex items-center h-[26px] px-2.5 border rounded-full text-xs font-semibold"
                  :class="on ? 'border-white/30 text-white' : 'border-hairline text-ink-500'"
                >
                  {{ n }}
                </span>
              </span>
            </span>
          </template>
        </OnbOptionCard>
      </div>
    </div>

    <OnbFooter :disabled="!state.jenis" hint="Pilih salah satu" @back="$emit('back')" @next="$emit('next')" />
  </div>
</template>

<script setup>
import { Building2, User, Users } from 'lucide-vue-next'
import OnbHeading from './OnbHeading.vue'
import OnbFooter from './OnbFooter.vue'
import OnbOptionCard from './OnbOptionCard.vue'
import { JENIS_USAHA_OPTIONS } from '@/constants/architectOnboarding'
import { useArchitectOnboarding } from '@/composables/useArchitectOnboarding'

defineEmits(['back', 'next'])

const { state } = useArchitectOnboarding()

const ICONS = { User, Users, Building2 }
</script>
