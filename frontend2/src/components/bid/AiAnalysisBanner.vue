<template>
  <div class="bg-gradient-to-r from-[#2D1B12] to-[#4A2F1F] rounded-3xl p-8 shadow-2xl border border-amber-900/20">
    <div class="flex items-start gap-6">
      <div class="bg-amber-500/20 p-4 rounded-2xl">
        <Sparkles :size="32" class="text-amber-300" />
      </div>

      <div class="flex-1">
        <h3 class="text-2xl font-bold text-white mb-2 flex items-center gap-2">
          Analyze with Rumantra AI
          <span class="px-3 py-1 bg-amber-500/20 text-amber-300 text-xs font-bold rounded-full uppercase">
            Coming Soon
          </span>
        </h3>
        <p class="text-white/80 mb-4">
          Comparing {{ proposalCount }} proposals? Let AI help you identify the best value based on cost, timeline,
          experience, and concept quality.
        </p>
        <button
          disabled
          class="px-6 py-3 bg-amber-500/30 text-white/50 rounded-full font-bold cursor-not-allowed flex items-center gap-2"
        >
          <Zap :size="18" />
          Start AI Analysis
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Sparkles, Zap } from 'lucide-vue-next'

defineProps({
  proposalCount: {
    type: Number,
    default: 0
  }
})
</script>
