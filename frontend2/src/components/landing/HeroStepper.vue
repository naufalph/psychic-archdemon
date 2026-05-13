<template>
  <div class="relative w-full max-w-[680px] mx-auto" @mouseenter="pauseTimer" @mouseleave="resumeTimer">
    <div class="laptop-mockup hover:-translate-y-1 transition-transform duration-500">
      <!-- Screen bezel -->
      <div
        class="relative"
        style="
          background: #000;
          border-radius: 24px;
          padding: 8px;
          height: 440px;
          box-shadow: 0 50px 100px -20px rgba(0, 0, 0, 0.3);
          display: flex;
          flex-direction: column;
        "
      >
        <!-- Notch -->
        <div
          style="
            position: absolute;
            top: 8px;
            left: 50%;
            transform: translateX(-50%);
            width: 140px;
            height: 24px;
            background: #000;
            border-radius: 0 0 12px 12px;
            z-index: 10;
          "
        ></div>

        <!-- Viewport -->
        <div
          style="
            background: #ffffff;
            flex: 1;
            border-radius: 12px;
            padding: 20px 24px;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            position: relative;
          "
        >
          <!-- macOS traffic lights -->
          <div class="flex gap-1.5 mb-5">
            <div style="width: 10px; height: 10px; border-radius: 50%; background: #ff5f57; opacity: 0.8"></div>
            <div style="width: 10px; height: 10px; border-radius: 50%; background: #ffbd2e; opacity: 0.8"></div>
            <div style="width: 10px; height: 10px; border-radius: 50%; background: #28c840; opacity: 0.8"></div>
          </div>

          <!-- Stepper dots -->
          <div class="flex justify-center gap-2 mb-4">
            <button
              v-for="i in 4"
              :key="i"
              class="rounded-full transition-all duration-300 cursor-pointer border-none p-0"
              :style="
                currentStep === i - 1
                  ? 'width:6px;height:6px;background:#0A0A0A;transform:scale(1.4);box-shadow:0 0 0 4px rgba(0,0,0,0.1);'
                  : 'width:6px;height:6px;background:#CCCCCC;'
              "
              @click="goToStep(i - 1)"
            />
          </div>

          <!-- Step panels container -->
          <div class="flex-1 relative overflow-hidden">
            <!-- Step 0: Project brief form -->
            <div class="step-panel" :class="{ active: currentStep === 0 }">
              <p class="step-label">Jenis Bangunan</p>
              <div class="flex flex-wrap gap-1.5 mb-3">
                <span
                  v-for="type in ['Hunian', 'Komersial', 'Industrial']"
                  :key="type"
                  class="px-2 py-0.5 rounded-full text-[9px] font-semibold"
                  :style="
                    type === 'Hunian'
                      ? 'background:#0A0A0A;color:#fff;'
                      : 'background:#F5F5F5;color:#666;border:1px solid #E8E8E8;'
                  "
                  >{{ type }}</span
                >
              </div>
              <div class="mb-2">
                <p class="step-field-label">Luas Bangunan</p>
                <div class="step-input">120 m²</div>
              </div>
              <div class="mb-2">
                <p class="step-field-label">Anggaran Desain</p>
                <div class="step-input">Rp 25.000.000</div>
              </div>
              <div class="mt-3 rounded-lg p-2 text-[9px]" style="background: #f5f5f5; color: #666">
                ✨ AI menyarankan 3 arsitek berlisensi IAI untuk proyek Anda
              </div>
            </div>

            <!-- Step 1: Bid cards -->
            <div class="step-panel" :class="{ active: currentStep === 1 }">
              <p class="step-label">3 Penawaran Diterima</p>
              <div
                v-for="(arch, i) in architects"
                :key="arch.name"
                class="flex items-center justify-between rounded-lg p-2 mb-2"
                style="background: #fff; border: 1px solid #e8e8e8"
                :style="`animation: cardIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) ${i * 100}ms both`"
              >
                <div class="flex items-center gap-2">
                  <div
                    class="w-6 h-6 rounded-full flex items-center justify-center text-[9px] font-bold text-white flex-shrink-0"
                    :style="`background:${arch.color}`"
                  >
                    {{ arch.initial }}
                  </div>
                  <div>
                    <p style="font-size: 10px; font-weight: 600; color: #0a0a0a">{{ arch.name }}</p>
                    <p style="font-size: 8px; color: #888">{{ arch.exp }} • ⭐ {{ arch.rating }}</p>
                  </div>
                </div>
                <span style="font-size: 9px; font-weight: 700; color: #0a0a0a">{{ arch.price }}</span>
              </div>
            </div>

            <!-- Step 2: Comparison table -->
            <div class="step-panel" :class="{ active: currentStep === 2 }">
              <p class="step-label">Perbandingan</p>
              <table class="w-full" style="font-size: 9px; border-collapse: collapse">
                <thead>
                  <tr style="border-bottom: 1px solid #e8e8e8">
                    <th class="text-left py-1" style="color: #aaaaaa; font-weight: 500">Metrik</th>
                    <th class="text-center py-1" style="color: #aaaaaa; font-weight: 500">Budi S.</th>
                    <th class="text-center py-1" style="color: #aaaaaa; font-weight: 500">Ani R.</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in compareRows"
                    :key="row.label"
                    :style="row.highlight ? 'background:#ECFCE5;' : 'border-bottom:1px solid #F5F5F5;'"
                  >
                    <td class="py-1" style="color: #666">{{ row.label }}</td>
                    <td
                      class="py-1 text-center font-semibold"
                      :style="row.highlight ? 'color:#1A7A2E;' : 'color:#0A0A0A;'"
                    >
                      {{ row.a }}
                    </td>
                    <td class="py-1 text-center" style="color: #0a0a0a">{{ row.b }}</td>
                  </tr>
                </tbody>
              </table>
              <div
                class="mt-2 rounded p-1.5"
                style="background: #ecfce5; font-size: 8px; color: #1a7a2e; font-weight: 600"
              >
                ✓ AI merekomendasikan Budi Santoso — nilai terbaik
              </div>
            </div>

            <!-- Step 3: Success -->
            <div class="step-panel" :class="{ active: currentStep === 3 }">
              <div class="text-center pt-2 mb-3">
                <div style="font-size: 32px; margin-bottom: 8px">🎉</div>
                <p style="font-size: 12px; font-weight: 700; color: #0a0a0a">Proyek Dimulai!</p>
                <p style="font-size: 9px; color: #888">Budi Santoso dikonfirmasi</p>
              </div>
              <div class="rounded-lg p-2" style="background: #fff; border: 1px solid #e8e8e8">
                <div
                  v-for="(item, i) in timeline"
                  :key="item.label"
                  class="flex items-start gap-2 pb-2"
                  :style="i < timeline.length - 1 ? 'border-bottom:1px solid #F5F5F5;' : ''"
                >
                  <div
                    class="rounded-full mt-1 flex-shrink-0"
                    style="width: 6px; height: 6px"
                    :style="i === 0 ? 'background:#1A7A2E;' : 'background:#CCCCCC;'"
                  ></div>
                  <p style="font-size: 9px" :style="i === 0 ? 'color:#1A7A2E;font-weight:600;' : 'color:#AAAAAA;'">
                    {{ item.label }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Laptop base -->
      <div
        style="
          height: 12px;
          background: linear-gradient(to bottom, #d1d5db, #9ca3af);
          border-radius: 0 0 8px 8px;
          margin: 0 20px;
        "
      ></div>
      <div style="height: 4px; background: #9ca3af; border-radius: 0 0 12px 12px; margin: 0 40px"></div>
    </div>

    <!-- Live notification badge -->
    <div
      class="absolute flex items-center gap-2"
      style="
        bottom: 60px;
        right: -16px;
        background: #fff;
        padding: 8px 12px;
        border-radius: 12px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        z-index: 20;
      "
    >
      <div class="live-dot"></div>
      <span style="font-size: 10px; font-weight: 600; color: #333">2 penawaran baru masuk</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const currentStep = ref(0)
let timer = null
let isPaused = false

const architects = [
  { name: 'Budi Santoso', initial: 'B', color: '#0A0A0A', exp: '8 tahun', rating: '4.9', price: 'Rp 22jt' },
  { name: 'Ani Rahayu', initial: 'A', color: '#3B82F6', exp: '6 tahun', rating: '4.7', price: 'Rp 25jt' },
  { name: 'Dimas P.', initial: 'D', color: '#8B5CF6', exp: '5 tahun', rating: '4.6', price: 'Rp 20jt' }
]

const compareRows = [
  { label: 'Harga', a: 'Rp 22jt', b: 'Rp 25jt', highlight: true },
  { label: 'Timeline', a: '45 hari', b: '60 hari', highlight: false },
  { label: 'Rating', a: '⭐ 4.9', b: '⭐ 4.7', highlight: false },
  { label: 'Portofolio', a: '12 proyek', b: '8 proyek', highlight: false }
]

const timeline = [
  { label: 'Pembayaran fase 1 diterima' },
  { label: 'Desain konsep — minggu 1–2' },
  { label: 'Desain skematik — minggu 3–4' },
  { label: 'Finalisasi — minggu 6–7' }
]

function startTimer() {
  timer = setInterval(() => {
    if (!isPaused) {
      currentStep.value = (currentStep.value + 1) % 4
    }
  }, 5000)
}

function goToStep(n) {
  currentStep.value = n
  clearInterval(timer)
  startTimer()
}

function pauseTimer() {
  isPaused = true
}

function resumeTimer() {
  isPaused = false
}

onMounted(() => startTimer())
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.step-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  pointer-events: none;
}

.step-panel.active {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  pointer-events: all;
}

.step-label {
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #888888;
  margin-bottom: 8px;
}

.step-field-label {
  font-size: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #aaaaaa;
  margin-bottom: 4px;
}

.step-input {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 5px 8px;
  font-size: 11px;
  color: #0a0a0a;
  font-weight: 500;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
}

.live-dot::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: #10b981;
  border-radius: 50%;
  animation: pulseLive 2s infinite;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulseLive {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  100% {
    transform: scale(2.5);
    opacity: 0;
  }
}
</style>
