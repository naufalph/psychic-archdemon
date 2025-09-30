<template>
  <div class="architect-landing">
    <!-- Hero Section -->
    <section class="hero-section bg-gradient-to-br from-black to-blue-800 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h1 class="text-display-large font-display  text-white mb-6 text-balance">
              {{ $t('architectLanding.hero.title') }}
            </h1>
            <p class="text-body-large font-body-regular  text-blue-100 mb-8 leading-relaxed">
              {{ $t('architectLanding.hero.subtitle') }}
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <button
                @click="showSignInPopup = true"
                class="btn btn-primary bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8 py-3 text-lg"
              >
                {{ $t('architectLanding.hero.signUpNow') }}
              </button>
              <router-link
                to="/projects"
                class="btn btn-outline border-white text-white hover:bg-white hover:text-blue-600 font-semibold px-8 py-3 text-lg"
              >
                {{ $t('architectLanding.hero.viewProjects') }}
              </router-link>
            </div>
          </div>
          <div class="hidden lg:block">
            <div class="hero-image-container">
              <img
                src="/src/assets/images/landing/hero-architecture.jpg"
                alt="Architect at Work"
                class="w-full h-96 object-cover rounded-lg shadow-2xl"
                @error="handleImageError"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="how-it-works-section py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-display-small font-display  text-gray-900 mb-4">
            {{ $t('architectLanding.howItWorks.title') }}
          </h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          <div class="step-card bg-white rounded-xl p-6 shadow-lg hover:shadow-lg transition-shadow">
            <div class="step-number-container mb-6">
              <div class="step-number">1</div>
            </div>
            <h3 class="text-title-large font-title  text-gray-900 mb-3">
              {{ $t('architectLanding.howItWorks.steps.createProfile.title') }}
            </h3>
            <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
              {{ $t('architectLanding.howItWorks.steps.createProfile.description') }}
            </p>
          </div>
          <div class="step-card bg-white rounded-xl p-6 shadow-lg hover:shadow-lg transition-shadow">
            <div class="step-number-container mb-6">
              <div class="step-number">2</div>
            </div>
            <h3 class="text-title-large font-title  text-gray-900 mb-3">
              {{ $t('architectLanding.howItWorks.steps.sendOffers.title') }}
            </h3>
            <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
              {{ $t('architectLanding.howItWorks.steps.sendOffers.description') }}
            </p>
          </div>
          <div class="step-card bg-white rounded-xl p-6 shadow-lg hover:shadow-lg transition-shadow">
            <div class="step-number-container mb-6">
              <div class="step-number">3</div>
            </div>
            <h3 class="text-title-large font-title  text-gray-900 mb-3">
              {{ $t('architectLanding.howItWorks.steps.workAndGetPaid.title') }}
            </h3>
            <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
              {{ $t('architectLanding.howItWorks.steps.workAndGetPaid.description') }}
            </p>
          </div>
        </div>

        <div class="text-center">
          <button class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button  hover:bg-gray-800 transition-colors">
            {{ $t('architectLanding.howItWorks.signUpNow') }}
          </button>
        </div>
      </div>
    </section>

    <!-- Everything in One Platform Section -->
    <section class="platform-section py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <h2 class="text-display-small font-display  text-gray-900 mb-4">
            {{ $t('architectLanding.platform.title') }}
          </h2>
        </div>

        <!-- Horizontal Scrolling Cards -->
        <div class="relative">
          <!-- Navigation Arrows -->
          <button
            v-show="scrollPosition > 0"
            @click="scrollLeft"
            class="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-white rounded-full p-3 shadow-lg hover:shadow-xl transition-all duration-300"
          >
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
          </button>

          <button
            v-show="scrollPosition < maxScrollPosition"
            @click="scrollRight"
            class="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-white rounded-full p-3 shadow-lg hover:shadow-xl transition-all duration-300"
          >
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </button>

          <!-- Scrollable Container -->
          <div
            ref="scrollContainer"
            class="flex gap-6 overflow-x-hidden scroll-smooth px-12"
            @scroll="updateScrollPosition"
          >
            <div class="flex-shrink-0 w-72 bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow">
              <div class="text-headline-small font-headline  text-gray-600 mb-4">
                {{ $t('architectLanding.platform.features.smartPortfolio.title') }}
              </div>
              <h3 class="text-title-large font-title  text-gray-900 mb-2">
                {{ $t('architectLanding.platform.features.smartPortfolio.description') }}
              </h3>
              <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
                {{ $t('architectLanding.platform.features.smartPortfolio.subtitle') }}
              </p>
            </div>

            <div class="flex-shrink-0 w-72 bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow">
              <div class="text-headline-small font-headline  text-gray-600 mb-4">
                {{ $t('architectLanding.platform.features.curatedProjects.title') }}
              </div>
              <h3 class="text-title-large font-title  text-gray-900 mb-2">
                {{ $t('architectLanding.platform.features.curatedProjects.description') }}
              </h3>
              <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
                {{ $t('architectLanding.platform.features.curatedProjects.subtitle') }}
              </p>
            </div>

            <div class="flex-shrink-0 w-72 bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow">
              <div class="text-headline-small font-headline  text-gray-600 mb-4">
                {{ $t('architectLanding.platform.features.estimateAssistance.title') }}
              </div>
              <h3 class="text-title-large font-title  text-gray-900 mb-2">
                {{ $t('architectLanding.platform.features.estimateAssistance.description') }}
              </h3>
              <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
                {{ $t('architectLanding.platform.features.estimateAssistance.subtitle') }}
              </p>
            </div>

            <div class="flex-shrink-0 w-72 bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow">
              <div class="text-headline-small font-headline  text-gray-600 mb-4">
                {{ $t('architectLanding.platform.features.onDemandSurveys.title') }}
              </div>
              <h3 class="text-title-large font-title  text-gray-900 mb-2">
                {{ $t('architectLanding.platform.features.onDemandSurveys.description') }}
              </h3>
              <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
                {{ $t('architectLanding.platform.features.onDemandSurveys.subtitle') }}
              </p>
            </div>

            <div class="flex-shrink-0 w-72 bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow">
              <div class="text-headline-small font-headline  text-gray-600 mb-4">
                {{ $t('architectLanding.platform.features.contractDocs.title') }}
              </div>
              <h3 class="text-title-large font-title  text-gray-900 mb-2">
                {{ $t('architectLanding.platform.features.contractDocs.description') }}
              </h3>
              <p class="text-body-small font-body-regular  text-gray-600 leading-relaxed">
                {{ $t('architectLanding.platform.features.contractDocs.subtitle') }}
              </p>
            </div>
          </div>
        </div>

        <div class="text-center mt-12">
          <button class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button  hover:bg-gray-800 transition-colors">
            {{ $t('architectLanding.platform.tryNow') }}
          </button>
        </div>
      </div>
    </section>

    <!-- Portfolio Showcase Section -->
    <section class="portfolio-section py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <!-- Left Content Card -->
          <div class="lg:col-span-5">
            <div class="bg-gray-50 rounded-xl p-8">
              <h2 class="text-display-medium font-display  text-gray-900 mb-4">
                {{ $t('architectLanding.portfolio.title') }}
              </h2>
              <p class="text-body-large font-body-regular  text-gray-600 mb-8">
                {{ $t('architectLanding.portfolio.subtitle') }}
              </p>
              <button class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button  hover:bg-gray-800 transition-colors">
                {{ $t('architectLanding.portfolio.createPortfolio') }}
              </button>
            </div>
          </div>

          <!-- Right Portfolio Cards -->
          <div class="lg:col-span-7">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                v-for="portfolio in portfolioSamples.slice(0, 3)"
                :key="portfolio.id"
                class="portfolio-card bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-lg transition-shadow cursor-pointer"
              >
                <img
                  :src="portfolio.image"
                  :alt="portfolio.title"
                  class="w-full h-48 object-cover hover:scale-105 transition-transform duration-300"
                  @error="handleImageError"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Success Stories Section -->
    <section class="success-section py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Left Content -->
          <div class="lg:col-span-5">
            <h2 class="text-display-medium font-display  text-gray-900 mb-4">
              {{ $t('architectLanding.success.title') }}
            </h2>
            <p class="text-body-large font-body-regular  text-gray-600 mb-8">
              {{ $t('architectLanding.success.subtitle') }}
            </p>
            <button class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button  hover:bg-gray-800 transition-colors">
              {{ $t('architectLanding.success.startJourney') }}
            </button>
          </div>

          <!-- Right Image -->
          <div class="lg:col-span-7">
            <img
              src="/src/assets/images/landing/everything-in-one-layer.jpg"
              alt="Successful Architect at Work"
              class="w-full h-auto rounded-xl shadow-lg"
              @error="handleImageError"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section py-20 bg-blue-600 text-white">
      <div class="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl md:text-4xl font-bold mb-6">
          {{ $t('architectLanding.cta.title') }}
        </h2>
        <p class="text-xl text-blue-100 mb-8 leading-relaxed">
          {{ $t('architectLanding.cta.subtitle') }}
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            @click="showSignInPopup = true"
            class="btn bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8 py-3 text-lg"
          >
            {{ $t('architectLanding.cta.getStarted') }}
          </button>
          <router-link
            to="/projects"
            class="btn btn-outline border-white text-white hover:bg-white hover:text-blue-600 font-semibold px-8 py-3 text-lg"
          >
            {{ $t('architectLanding.cta.browseProjects') }}
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'ArchitectLandingPage',
  data() {
    return {
      showSignInPopup: false,
      scrollPosition: 0,
      maxScrollPosition: 0,
      portfolioSamples: [
        {
          id: 1,
          title: 'Modern Residential Design',
          image: '/src/assets/images/landing/portfolio-designer-workspace.jpg',
        },
        {
          id: 2,
          title: 'Commercial Architecture',
          image: '/src/assets/images/landing/portfolio-architect-planning.jpg',
        },
        {
          id: 3,
          title: 'Interior Space Design',
          image: '/src/assets/images/landing/portfolio-interior-modern.jpg',
        }
      ]
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.updateScrollLimits()
      window.addEventListener('resize', this.updateScrollLimits)
    })
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateScrollLimits)
  },
  methods: {
    handleImageError(event) {
      // Replace broken images with placeholder
      event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xNiAxNkwyNCAyNE0xNiAyNEwyNCAyNCIgc3Ryb2tlPSIjOUI5QkEwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K'
    },
    scrollLeft() {
      const container = this.$refs.scrollContainer
      if (container) {
        const scrollAmount = 312 // width of card (w-72 = 288px) plus gap (24px)
        container.scrollLeft -= scrollAmount
      }
    },
    scrollRight() {
      const container = this.$refs.scrollContainer
      if (container) {
        const scrollAmount = 312 // width of card (w-72 = 288px) plus gap (24px)
        container.scrollLeft += scrollAmount
      }
    },
    updateScrollPosition() {
      const container = this.$refs.scrollContainer
      if (container) {
        this.scrollPosition = container.scrollLeft
      }
    },
    updateScrollLimits() {
      const container = this.$refs.scrollContainer
      if (container) {
        this.maxScrollPosition = container.scrollWidth - container.clientWidth
      }
    }
  }
}
</script>

<style scoped>
.btn {
  @apply inline-flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2;
}

.btn-primary {
  @apply bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500;
}

.btn-outline {
  @apply bg-transparent border-2 hover:bg-blue-50 focus:ring-blue-500;
}

.hero-image-container {
  position: relative;
}

.hero-image-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, rgba(59, 130, 246, 0.1), rgba(147, 197, 253, 0.1));
  border-radius: 0.5rem;
  z-index: 1;
}

.step-number-container {
  display: flex;
  justify-content: left;
}

.step-number {
  @apply text-headline-large  text-gray-900 font-bold;
}

.step-card:hover {
  transform: translateY(-4px);
  transition: transform 0.3s ease;
}

.platform-section .scroll-smooth {
  scroll-behavior: smooth;
}

.platform-section .overflow-x-hidden {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* Internet Explorer 10+ */
}

.platform-section .overflow-x-hidden::-webkit-scrollbar {
  display: none; /* Safari and Chrome */
}

.portfolio-card:hover img {
  transform: scale(1.05);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2.5rem;
  }

  .platform-section .flex {
    gap: 1rem;
    padding-left: 2rem;
    padding-right: 2rem;
  }

  .platform-section .flex-shrink-0 {
    width: 16rem; /* w-64 on mobile */
  }
}
</style>