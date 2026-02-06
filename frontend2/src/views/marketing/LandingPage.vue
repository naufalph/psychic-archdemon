<template>
  <div class="min-h-screen bg-white overflow-x-hidden">
    <Navbar />

    <div class="pt-16">
      <section class="relative bg-[#F4F5F7] min-h-[90vh] flex items-center overflow-hidden">
        <div
          class="absolute inset-0 opacity-[0.03] pointer-events-none"
          style="
            background-image:
              linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
            background-size: 50px 50px;
          "
        ></div>

        <div class="max-w-7xl mx-auto px-6 relative z-10 w-full py-20">
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div
              v-motion
              :initial="{ opacity: 0, x: -30 }"
              :visible="{ opacity: 1, x: 0 }"
              :duration="800"
              class="lg:col-span-7"
            >
              <h1 class="text-4xl md:text-5xl lg:text-[4.5rem] font-bold tracking-tight leading-[1.1] mb-8">
                <span class="text-gray-400 block">{{ t.hero.title1 }}</span>
                <span class="text-gray-400 block">{{ t.hero.title2 }}</span>
                <span class="text-black block">{{ t.hero.title3 }}</span>
              </h1>
              <p class="text-lg text-gray-500 max-w-lg mb-12 font-medium leading-relaxed">{{ t.hero.subtitle }}</p>
              <router-link to="/signup">
                <BaseButton
                  class="bg-[#C5A17A] hover:bg-[#B39069] text-white text-lg px-12 py-5 rounded-full shadow-lg border-none flex gap-3"
                >
                  {{ t.hero.cta }} <ArrowRight class="w-5 h-5" />
                </BaseButton>
              </router-link>
            </div>

            <div
              v-motion
              :initial="{ opacity: 0, scale: 0.95 }"
              :visible="{ opacity: 1, scale: 1 }"
              :duration="1000"
              :delay="200"
              class="lg:col-span-5 relative"
            >
              <div class="relative">
                <div class="bg-white p-4 rounded-3xl shadow-2xl border border-gray-100">
                  <img
                    src="https://images.unsplash.com/photo-1503387762-592dea58ef21?auto=format&fit=crop&q=80&w=1200"
                    alt="Modern Architecture"
                    class="rounded-2xl w-full h-[500px] object-cover grayscale brightness-110"
                  />
                  <div
                    class="absolute -bottom-4 -right-4 bg-brand-yellow px-6 py-3 rounded-lg shadow-xl border-2 border-white"
                  >
                    <span class="text-[10px] font-mono font-bold tracking-[0.2em] text-black">SCALE 1:50</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="py-24 bg-white border-b border-gray-50">
        <div class="max-w-7xl mx-auto px-6">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6 lg:gap-10">
            <div
              v-for="(item, idx) in SOLUTIONS"
              :key="idx"
              v-motion
              :initial="{ opacity: 0, y: 30 }"
              :visible="{ opacity: 1, y: 0 }"
              :delay="idx * 50"
              class="flex flex-col items-center p-8 border border-gray-100 rounded-3xl hover:bg-gray-50 hover:border-gray-200 transition-all group"
            >
              <div
                class="w-14 h-14 bg-gray-50 rounded-2xl flex items-center justify-center mb-6 border border-gray-100 group-hover:bg-white group-hover:shadow-sm transition-all"
              >
                <component :is="item.icon" class="w-7 h-7 text-gray-800" />
              </div>
              <h3
                class="text-[11px] font-bold text-center text-gray-500 uppercase tracking-[0.15em] leading-tight px-4 group-hover:text-black transition-colors"
              >
                {{ t.solutions[item.key] }}
              </h3>
            </div>
          </div>
        </div>
      </section>

      <section class="py-24 bg-white overflow-hidden">
        <div class="max-w-7xl mx-auto px-6 text-center">
          <div v-motion :initial="{ opacity: 0, y: 30 }" :visible="{ opacity: 1, y: 0 }" class="mb-16">
            <h2 class="text-4xl md:text-5xl font-bold mb-6 tracking-tight">{{ t.visualization.title }}</h2>
            <div class="w-24 h-1 bg-[#C5A17A] mx-auto rounded-full"></div>
          </div>

          <div
            v-motion
            :initial="{ opacity: 0, y: 50 }"
            :visible="{ opacity: 1, y: 0 }"
            :duration="1000"
            class="relative inline-block max-w-5xl w-full"
          >
            <img
              src="https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=1400"
              alt="Architecture Process Visualization"
              class="w-full rounded-[3rem] shadow-2xl grayscale border border-gray-100"
            />
          </div>

          <div class="mt-24 pt-10 border-t border-gray-100">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-10">
              {{ t.visualization.trustedBy }}
            </p>
            <div class="flex flex-wrap justify-center gap-12 lg:gap-20 items-center opacity-30 grayscale contrast-125">
              <span class="font-black text-2xl tracking-tighter uppercase italic">Mastercard</span>
              <span class="font-black text-2xl tracking-tighter uppercase">VISA</span>
              <span class="font-black text-2xl tracking-tighter uppercase">ShopeePay</span>
              <span class="font-black text-2xl tracking-tighter uppercase italic">GoPay</span>
              <span class="font-black text-2xl tracking-tighter uppercase">OVO</span>
              <span class="font-black text-2xl tracking-tighter uppercase">LinkAja</span>
            </div>
          </div>
        </div>
      </section>

      <section class="py-24 bg-[#F5E6D3]/40">
        <div class="max-w-7xl mx-auto px-6">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-24 items-center">
            <div>
              <p class="text-[#7C4728] font-bold mb-4 uppercase tracking-[0.4em] text-[10px]">{{ t.premium.label }}</p>
              <h2 class="text-4xl lg:text-5xl font-bold mb-14 text-black leading-tight">{{ t.premium.title }}</h2>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-12">
                <div
                  v-for="(feature, key) in t.premium.features"
                  :key="key"
                  v-motion
                  :initial="{ opacity: 0, y: 30 }"
                  :visible="{ opacity: 1, y: 0 }"
                >
                  <div class="flex items-start gap-4 mb-4">
                    <div class="w-6 h-6 rounded-full bg-white flex items-center justify-center flex-shrink-0 shadow-sm">
                      <CheckCircle2 class="w-4 h-4 text-[#7C4728]" />
                    </div>
                    <h4 class="font-bold text-lg text-black leading-tight">{{ feature.title }}</h4>
                  </div>
                  <p class="text-gray-600 text-sm leading-relaxed pl-10">{{ feature.desc }}</p>
                </div>
              </div>
              <BaseButton
                class="mt-16 bg-[#7C4728] hover:bg-black text-white rounded-full px-12 py-5 font-bold shadow-xl border-none"
              >
                {{ t.premium.cta }}
              </BaseButton>
            </div>

            <div v-motion :initial="{ opacity: 0, x: 50 }" :visible="{ opacity: 1, x: 0 }" class="relative">
              <div class="relative rounded-[2.5rem] overflow-hidden shadow-2xl border-8 border-white bg-white">
                <img
                  src="https://images.unsplash.com/photo-1541888946425-d81bb19240f5?auto=format&fit=crop&q=80&w=1200"
                  alt="Architecture Planning"
                  class="w-full h-[600px] object-cover grayscale contrast-125 opacity-90"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="py-24 bg-white">
        <div class="max-w-7xl mx-auto px-6 text-center mb-20">
          <h2 class="text-5xl font-bold mb-6 tracking-tight">{{ t.gallery.title }}</h2>
          <p class="text-gray-500 text-lg max-w-2xl mx-auto">{{ t.gallery.subtitle }}</p>
        </div>

        <div class="max-w-[1500px] mx-auto px-6">
          <MasonryGallery :images="MASONRY_IMAGES" />
        </div>
      </section>

      <section class="py-24 bg-[#1A1A1A] relative overflow-hidden">
        <div class="max-w-7xl mx-auto px-6">
          <div
            class="bg-[#7C4728] rounded-[4rem] p-12 lg:p-24 relative overflow-hidden flex flex-col lg:flex-row items-center justify-between gap-16 shadow-2xl border border-white/10"
          >
            <div class="max-w-xl relative z-10 text-center lg:text-left">
              <h2 class="text-4xl lg:text-6xl font-bold text-white mb-8 leading-[1.1] tracking-tighter">
                {{ t.cta.title }}
              </h2>
              <p class="text-white/70 text-lg mb-12">{{ t.cta.subtitle }}</p>
              <div class="flex flex-wrap gap-6 justify-center lg:justify-start">
                <BaseButton
                  class="bg-white text-black hover:bg-gray-100 px-12 py-5 rounded-full font-bold text-lg shadow-xl border-none"
                >
                  {{ t.cta.startNow }}
                </BaseButton>
                <BaseButton
                  variant="ghost"
                  class="text-white border-white/30 border-2 hover:bg-white/10 px-12 py-5 rounded-full font-bold text-lg"
                >
                  {{ t.cta.learnMore }}
                </BaseButton>
              </div>
            </div>

            <div
              v-motion
              :initial="{ rotate: 5, scale: 0.9, opacity: 0 }"
              :visible="{ rotate: 0, scale: 1, opacity: 1 }"
              class="bg-white p-10 rounded-[3rem] shadow-3xl max-w-sm w-full relative z-10"
            >
              <div class="flex justify-between items-center mb-8">
                <h4 class="font-black text-black text-xl uppercase tracking-tight">{{ t.cta.hirePro }}</h4>
                <div class="p-3 bg-gray-50 rounded-2xl">
                  <FileText class="text-[#7C4728] w-6 h-6" />
                </div>
              </div>
              <div class="space-y-6">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center">
                    <CheckCircle2 class="w-5 h-5 text-green-600" />
                  </div>
                  <span class="text-base font-bold text-gray-800">{{ t.cta.verified }}</span>
                </div>
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center">
                    <ShieldCheck class="w-5 h-5 text-blue-600" />
                  </div>
                  <span class="text-base font-bold text-gray-800">{{ t.cta.secure }}</span>
                </div>
                <div class="h-px bg-gray-100 my-4"></div>
                <BaseButton :fullWidth="true" class="bg-black text-white rounded-full py-4 text-lg font-bold">
                  {{ t.cta.searchNow }}
                </BaseButton>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import {
  ShieldCheck,
  PenTool,
  Layout,
  Calculator,
  ClipboardList,
  Building2,
  Sparkles,
  BarChart3,
  Search,
  CheckCircle2,
  FileText,
  ArrowRight
} from 'lucide-vue-next'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import MasonryGallery from '@/components/marketing/MasonryGallery.vue'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const SOLUTIONS = [
  { icon: Building2, key: 'personalArch' },
  { icon: ClipboardList, key: 'projectBrief' },
  { icon: Search, key: 'findArchitect' },
  { icon: BarChart3, key: 'budgetTool' },
  { icon: Calculator, key: 'priceAnalysis' },
  { icon: PenTool, key: 'digitalBlueprint' },
  { icon: Layout, key: 'trackMilestones' },
  { icon: Sparkles, key: 'aiInspiration' }
]

const MASONRY_IMAGES = [
  {
    url: 'https://images.unsplash.com/photo-1600585154340-be6199f7a096?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 0'
  },
  {
    url: 'https://images.unsplash.com/photo-1600607687940-4e524cb3513a?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 1'
  },
  {
    url: 'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 2'
  },
  {
    url: 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 3'
  },
  {
    url: 'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 4'
  },
  {
    url: 'https://images.unsplash.com/photo-1600573472591-ee6b68d14c68?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 5'
  },
  {
    url: 'https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 6'
  },
  {
    url: 'https://images.unsplash.com/photo-1600585154526-990dced4db0d?auto=format&fit=crop&q=80&w=800',
    alt: 'Inspiration 7'
  }
]
</script>
