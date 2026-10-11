<template>
  <div class="bg-white border border-hairline rounded-[20px] p-6 md:p-8 animate-brief-rise">
    <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-4">
      <div
        v-for="service in SURVEY_SERVICES"
        :key="service.id"
        class="relative min-w-0 min-h-[260px] box-border flex flex-col gap-3 p-6 rounded-2xl border transition-all duration-300"
        :class="cardClass(service)"
        :role="service.soon ? undefined : 'button'"
        :tabindex="service.soon ? undefined : 0"
        :aria-pressed="service.soon ? undefined : isOn(service)"
        @click="toggle(service)"
        @keydown.enter.prevent="toggle(service)"
        @keydown.space.prevent="toggle(service)"
      >
        <div class="flex items-center flex-wrap gap-2 min-h-[22px]">
          <span
            v-if="service.soon"
            class="inline-flex items-center h-[22px] px-2.5 rounded-full border border-hairline text-ink-400 text-micro-sm uppercase tracking-[.05em] whitespace-nowrap"
          >
            Segera tersedia
          </span>
        </div>
        <span
          class="mt-2 text-[22px] font-bold tracking-[-.025em] leading-[1.2]"
          :class="isOn(service) ? 'text-white' : 'text-ink-900'"
        >
          {{ service.label }}
        </span>
        <span class="text-caption font-normal leading-normal" :class="isOn(service) ? 'text-white/80' : 'text-ink-400'">
          {{ service.desc }}
        </span>

        <template v-if="service.soon">
          <span
            v-if="survey.state.notified[service.id]"
            class="mt-auto h-11 inline-flex items-center gap-2 text-caption font-semibold text-ink-900"
          >
            <Check :size="16" :stroke-width="2.6" /> Kami akan kabari Anda
          </span>
          <button
            v-else
            type="button"
            class="self-start mt-auto inline-flex items-center gap-2 h-11 px-4 rounded-full border border-ink-900 bg-white text-ink-900 text-caption font-bold hover:bg-surface-alt transition-colors"
            @click.stop="$emit('notify', service)"
          >
            <Bell :size="16" :stroke-width="2" /> Kabari saya
          </button>
        </template>

        <span
          v-if="isOn(service)"
          class="absolute bottom-4 right-4 w-5 h-5 rounded-full bg-white text-ink-900 flex items-center justify-center"
        >
          <Check :size="13" :stroke-width="3.4" />
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Bell, Check } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { SURVEY_SERVICES } from '@/constants/projectSurvey'

defineEmits(['notify'])

const survey = useProjectSurvey()

const isOn = service => !service.soon && !!survey.state.services[service.id]

const cardClass = service => {
  if (service.soon) return 'bg-surface-alt border-hairline cursor-default'
  return isOn(service)
    ? 'bg-ink-900 border-ink-900 cursor-pointer hover:-translate-y-1 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.4)]'
    : 'bg-white border-hairline cursor-pointer hover:-translate-y-1 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.4)]'
}

const toggle = service => {
  if (service.soon) return
  survey.state.services[service.id] = !survey.state.services[service.id]
}
</script>
