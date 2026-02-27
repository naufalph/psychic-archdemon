<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-4xl mx-auto px-6">
      <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
        <div class="bg-[#7C4728] p-8 text-white">
          <h1 class="text-3xl font-bold flex items-center gap-3">
            <Home :size="32" />
            Post New Project
          </h1>
          <p class="text-white/80 mt-2">Define your requirements to find the perfect architect</p>
        </div>

        <form @submit.prevent="handleSubmit" class="p-8 space-y-10">
          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-[#F5E6D3] text-[#7C4728] font-bold px-3 py-1 rounded-full text-sm">Part 0</span>
              <h2 class="text-xl font-bold text-black">Project Images</h2>
            </div>
            <p class="text-xs text-gray-500">Upload photos, mood boards, or inspiration images for your project (max 10 images)</p>
            <MultiImageUploader v-model="coverImages" label="" :max-files="10" />
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-[#F5E6D3] text-[#7C4728] font-bold px-3 py-1 rounded-full text-sm">Part 1</span>
              <h2 class="text-xl font-bold text-black">General Information</h2>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Project Title<span class="text-red-500">*</span></label
              >
              <input
                v-model="formData.title"
                required
                type="text"
                placeholder="e.g., Modern Student Housing in Depok"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none transition"
              />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Location<span class="text-red-500">*</span></label
                >
                <input
                  v-model="formData.location"
                  required
                  type="text"
                  placeholder="City, Area"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Lot Size (m²)<span class="text-red-500">*</span></label
                >
                <input
                  v-model.number="formData.lotSize"
                  required
                  type="number"
                  placeholder="e.g., 200"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Building Type<span class="text-red-500">*</span></label
              >
              <select
                v-model="formData.buildingType"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
              >
                <option value="RESIDENTIAL">Residential Home</option>
                <option value="STUDENT_HOUSING">Student Housing (Kost)</option>
                <option value="VILLA">Villa / Resort</option>
                <option value="COMMERCIAL">Commercial / Office</option>
                <option value="RENOVATION">Renovation</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Detailed Requirements<span class="text-red-500">*</span></label
              >
              <textarea
                v-model="formData.description"
                required
                rows="4"
                placeholder="Describe number of rooms, style preference (e.g., Industrial, Tropical), timeline constraints..."
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-[#F5E6D3] text-[#7C4728] font-bold px-3 py-1 rounded-full text-sm">Part 2</span>
              <h2 class="text-xl font-bold text-black">Required Deliverables</h2>
            </div>

            <DeliverablesSelector v-model="formData.deliverables" />
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-[#F5E6D3] text-[#7C4728] font-bold px-3 py-1 rounded-full text-sm">Part 3</span>
              <h2 class="text-xl font-bold text-black">Budgeting</h2>
            </div>

            <div class="space-y-8">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Total Construction Budget</label>
                <p class="text-xs text-gray-500 mb-2">
                  Estimate for construction, material, and labor (Fisik Bangunan)
                </p>
                <div class="relative">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 font-medium">IDR</span>
                  <input
                    v-model="formData.constructionBudget"
                    type="text"
                    placeholder="e.g., 2.000.000.000 (optional)"
                    @input="formatConstructionBudget"
                    class="w-full pl-16 pr-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none text-right font-medium"
                  />
                </div>
              </div>

              <BudgetRangeSlider
                v-model="formData.designBudget"
                label="Design Budget (Architect Fee)"
                description="This is the main reference for architects to bid"
                :step="1000000"
                :required="true"
                :range-percent="25"
                hint="<strong>IAI Guideline:</strong> According to the Indonesian Institute of Architects (IAI), the design fee is typically around <strong>5% - 7%</strong> of the total construction budget."
              />
            </div>
          </section>

          <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
            {{ error }}
          </div>

          <div class="flex gap-4 pt-6 border-t border-gray-100">
            <button
              type="button"
              @click="handleCancel"
              class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 px-6 py-3 text-white bg-[#7C4728] rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader v-if="loading" :size="20" class="animate-spin" />
              <CheckSquare v-else :size="20" />
              {{ loading ? 'Creating...' : 'Post Project' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Home, CheckSquare, Loader } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import DeliverablesSelector from '@/components/project/DeliverablesSelector.vue'
import BudgetRangeSlider from '@/components/project/BudgetRangeSlider.vue'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'

const router = useRouter()
const projectsStore = useProjectsStore()

const formData = ref({
  title: '',
  location: '',
  lotSize: null,
  buildingType: 'RESIDENTIAL',
  description: '',
  constructionBudget: '',
  designBudget: {
    total: 0,
    min: 0,
    max: 0
  },
  deliverables: []
})

const coverImages = ref([])
const loading = ref(false)
const error = ref(null)

const formatConstructionBudget = event => {
  const value = event.target.value.replace(/[^0-9]/g, '')
  if (value) {
    formData.value.constructionBudget = parseInt(value, 10).toLocaleString('id-ID')
  } else {
    formData.value.constructionBudget = ''
  }
}

const parseConstructionBudget = () => {
  if (!formData.value.constructionBudget) return null
  return parseInt(formData.value.constructionBudget.replace(/[^0-9]/g, ''), 10) || null
}

const handleSubmit = async () => {
  loading.value = true
  error.value = null

  try {
    const projectData = {
      title: formData.value.title,
      location: formData.value.location,
      budgetTotal: parseConstructionBudget(),
      designBudgetMin: formData.value.designBudget.min,
      designBudgetMax: formData.value.designBudget.max,
      buildingFunction: formData.value.buildingType,
      estimatedBuildArea: formData.value.lotSize,
      scopeOfWork: formData.value.description,
      deliverables: formData.value.deliverables
    }

    await projectsStore.createProject(projectData, coverImages.value)
    router.push({ name: 'ClientDashboard' })
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to create project. Please try again.'
    console.error('Failed to create project:', err)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push({ name: 'ClientDashboard' })
}
</script>
