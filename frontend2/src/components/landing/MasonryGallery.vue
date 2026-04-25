<template>
  <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
    <div
      v-for="(image, index) in images"
      :key="index"
      v-motion
      :initial="{ opacity: 0, scale: 0.9 }"
      :visible="{ opacity: 1, scale: 1 }"
      :delay="index * 50"
      :class="getImageClasses(index)"
      class="overflow-hidden rounded-2xl"
    >
      <img
        :src="image.url"
        :alt="image.alt"
        class="w-full h-full object-cover hover:scale-110 transition-transform duration-500"
      />
    </div>
  </div>
</template>

<script setup>
defineProps({
  images: {
    type: Array,
    required: true
  }
})

const getImageClasses = index => {
  const heights = ['h-64', 'h-80', 'h-72', 'h-64', 'h-72', 'h-80', 'h-64', 'h-72']
  return heights[index % heights.length]
}
</script>
