<template>
  <div
    class="relative min-w-0 box-border flex flex-col gap-3 p-6 rounded-2xl border transition-all duration-300"
    :class="cardClass"
    :role="soon ? undefined : role"
    :tabindex="soon ? undefined : 0"
    :aria-checked="soon ? undefined : on"
    :aria-disabled="soon ? 'true' : undefined"
    @click="pick"
    @keydown.enter.prevent="pick"
    @keydown.space.prevent="pick"
  >
    <div class="flex items-center flex-wrap gap-2 min-h-[22px]">
      <component
        :is="icon"
        v-if="icon"
        :size="24"
        :stroke-width="2"
        :class="soon ? 'text-ink-300' : on ? 'text-white' : 'text-ink-900'"
      />
      <span
        v-if="soon"
        class="ml-auto inline-flex items-center h-[22px] px-2.5 rounded-full border border-hairline text-ink-400 text-micro-sm uppercase tracking-[.05em] whitespace-nowrap"
      >
        Segera tersedia
      </span>
    </div>
    <span
      class="mt-2 text-[22px] font-bold tracking-[-.025em] leading-[1.2]"
      :class="soon ? 'text-ink-400' : on ? 'text-white' : 'text-ink-900'"
    >
      {{ title }}
    </span>
    <span class="text-caption font-normal leading-normal" :class="on ? 'text-white/80' : 'text-ink-400'">
      {{ desc }}
    </span>
    <slot :on="on" />
    <span
      v-if="on"
      class="absolute bottom-4 right-4 w-5 h-5 rounded-full bg-white text-ink-900 flex items-center justify-center"
    >
      <Check :size="13" :stroke-width="3.4" />
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check } from 'lucide-vue-next'

const props = defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  on: { type: Boolean, default: false },
  soon: { type: Boolean, default: false },
  role: { type: String, default: 'checkbox' }
})
const emit = defineEmits(['pick'])

const pick = () => {
  if (!props.soon) emit('pick')
}

const lift = 'cursor-pointer hover:-translate-y-1 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.4)]'
const cardClass = computed(() => {
  if (props.soon) return 'bg-surface-alt border-hairline cursor-default'
  return props.on ? `bg-ink-900 border-ink-900 ${lift}` : `bg-white border-hairline ${lift}`
})
</script>
