<template>
  <div>
    <OnbHeading
      title="Anda bergabung sebagai apa?"
      lead="Silakan pilih semua bidang yang sesuai dengan usaha Anda. Anda dapat memilih lebih dari satu."
    />

    <div class="bg-white border border-hairline rounded-[20px] p-6 md:p-8 animate-brief-rise">
      <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-4" role="group" aria-label="Profesi">
        <OnbOptionCard
          v-for="o in PROFESI_OPTIONS"
          :key="o.id"
          class="min-h-[240px]"
          :title="o.title"
          :desc="o.desc"
          :icon="ICONS[o.icon]"
          :on="state.profesi.includes(o.id)"
          @pick="toggle('profesi', o.id)"
        />
        <OnbOptionCard
          class="min-h-[240px]"
          :title="PROFESI_SOON.title"
          :desc="PROFESI_SOON.desc"
          :icon="HardHat"
          soon
        />
      </div>
    </div>

    <OnbFooter
      :disabled="!state.profesi.length"
      hint="Pilih minimal satu profesi"
      @back="$emit('back')"
      @next="$emit('next')"
    />
  </div>
</template>

<script setup>
import { HardHat, Home, Sofa } from 'lucide-vue-next'
import OnbHeading from './OnbHeading.vue'
import OnbFooter from './OnbFooter.vue'
import OnbOptionCard from './OnbOptionCard.vue'
import { PROFESI_OPTIONS, PROFESI_SOON } from '@/constants/architectOnboarding'
import { useArchitectOnboarding } from '@/composables/useArchitectOnboarding'

defineEmits(['back', 'next'])

const { state, toggle } = useArchitectOnboarding()

const ICONS = { House: Home, Sofa }
</script>
