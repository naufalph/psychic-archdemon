import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useCountdown(targetDate) {
  const now = ref(Date.now())
  let interval = null

  const remaining = computed(() => {
    const target = new Date(targetDate).getTime()
    const diff = target - now.value

    if (diff <= 0) {
      return {
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0,
        expired: true,
        total: 0
      }
    }

    return {
      days: Math.floor(diff / (1000 * 60 * 60 * 24)),
      hours: Math.floor((diff / (1000 * 60 * 60)) % 24),
      minutes: Math.floor((diff / (1000 * 60)) % 60),
      seconds: Math.floor((diff / 1000) % 60),
      expired: false,
      total: diff
    }
  })

  const start = () => {
    if (interval) return
    interval = setInterval(() => {
      now.value = Date.now()
    }, 1000)
  }

  const stop = () => {
    if (interval) {
      clearInterval(interval)
      interval = null
    }
  }

  onMounted(() => {
    start()
  })

  onUnmounted(() => {
    stop()
  })

  return {
    remaining,
    start,
    stop
  }
}
