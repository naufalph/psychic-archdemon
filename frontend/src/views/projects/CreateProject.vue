<template>
  <div class="create-project">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Post New Project</h1>
        <p class="mt-2 text-gray-600">Tell us about your project and connect with talented architects</p>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Project Title -->
          <div>
            <label for="title" class="form-label">Project Title *</label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              required
              class="form-input"
              placeholder="e.g., Modern Family Home Design"
            />
          </div>

          <!-- Project Category -->
          <div>
            <label for="category" class="form-label">Category *</label>
            <select id="category" v-model="form.category" required class="form-input">
              <option value="">Select a category</option>
              <option value="residential">Residential</option>
              <option value="commercial">Commercial</option>
              <option value="industrial">Industrial</option>
              <option value="public">Public</option>
            </select>
          </div>

          <!-- Project Description -->
          <div>
            <label for="description" class="form-label">Project Description *</label>
            <textarea
              id="description"
              v-model="form.description"
              rows="5"
              required
              class="form-input"
              placeholder="Describe your project in detail..."
            ></textarea>
          </div>

          <!-- Budget -->
          <div>
            <label for="budget" class="form-label">Budget *</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span class="text-gray-500 sm:text-sm">$</span>
              </div>
              <input
                id="budget"
                v-model="form.budget"
                type="number"
                required
                class="form-input pl-7"
                placeholder="250000"
              />
            </div>
          </div>

          <!-- Location -->
          <div>
            <label for="location" class="form-label">Location *</label>
            <input
              id="location"
              v-model="form.location"
              type="text"
              required
              class="form-input"
              placeholder="City, State"
            />
          </div>

          <!-- Deadline -->
          <div>
            <label for="deadline" class="form-label">Project Deadline</label>
            <input id="deadline" v-model="form.deadline" type="date" class="form-input" />
          </div>

          <!-- Form Actions -->
          <div class="flex justify-end space-x-4 pt-6">
            <router-link to="/projects" class="btn btn-outline"> Cancel </router-link>
            <button type="submit" :disabled="isLoading" class="btn btn-primary">
              <span v-if="isLoading">Posting...</span>
              <span v-else>Post Project</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CreateProject',
  data() {
    return {
      isLoading: false,
      form: {
        title: '',
        category: '',
        description: '',
        budget: '',
        location: '',
        deadline: ''
      }
    }
  },
  methods: {
    handleSubmit() {
      this.isLoading = true
      // TODO: Implement project creation
      setTimeout(() => {
        console.log('Project form submitted:', this.form)
        alert('Project posted successfully! (This is a placeholder)')
        this.isLoading = false
        this.$router.push('/projects')
      }, 2000)
    }
  }
}
</script>
