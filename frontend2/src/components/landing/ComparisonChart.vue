<template>
  <div class="bg-white rounded-2xl p-6">
    <Radar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Radar } from 'vue-chartjs'
import { Chart as ChartJS, RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend } from 'chart.js'

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

const props = defineProps({
  architects: {
    type: Array,
    required: true
  }
})

const chartData = computed(() => ({
  labels: ['Harga', 'Pengalaman', 'Rating', 'Kesesuaian Gaya', 'Kecepatan', 'Komunikasi'],
  datasets: props.architects.map((architect, index) => ({
    label: architect.name,
    data: [
      architect.price,
      architect.experience,
      architect.rating,
      architect.styleMatch,
      architect.speed,
      architect.communication
    ],
    backgroundColor: index === 0 ? 'rgba(16, 185, 129, 0.1)' : 'rgba(59, 130, 246, 0.1)',
    borderColor: index === 0 ? '#10B981' : '#3B82F6',
    borderWidth: 2,
    pointBackgroundColor: index === 0 ? '#10B981' : '#3B82F6',
    pointBorderColor: '#fff',
    pointHoverBackgroundColor: '#fff',
    pointHoverBorderColor: index === 0 ? '#10B981' : '#3B82F6'
  }))
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  scales: {
    r: {
      beginAtZero: true,
      max: 100,
      ticks: {
        stepSize: 20,
        backdropColor: 'transparent'
      },
      grid: {
        color: '#e5e7eb'
      },
      pointLabels: {
        font: {
          size: 12,
          family: 'Inter'
        },
        color: '#374151'
      }
    }
  },
  plugins: {
    legend: {
      position: 'top',
      labels: {
        font: {
          family: 'Inter',
          size: 13
        },
        usePointStyle: true,
        padding: 15
      }
    }
  }
}
</script>
