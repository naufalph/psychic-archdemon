<template>
  <div class="client-landing">
    <!-- Hero Section -->
    <section class="hero-section bg-gradient-to-br from-black to-blue-800 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h1 class="text-display-large font-display font-inter text-white mb-6 text-balance">
              {{ $t('clientLanding.hero.title') }}
              <span class="text-blue-200">{{ $t('clientLanding.hero.titleHighlight') }}</span>
              {{ $t('clientLanding.hero.titleSuffix') }}
            </h1>
            <p class="text-body-large font-body-regular font-inter text-blue-100 mb-8 leading-relaxed">
              {{ $t('clientLanding.hero.subtitle') }}
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <button
                @click="showSignInPopup = true"
                class="btn btn-primary bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8 py-3 text-lg"
              >
                {{ $t('clientLanding.hero.startProject') }}
              </button>
              <router-link
                to="/architects"
                class="btn btn-outline border-white text-white hover:bg-white hover:text-blue-600 font-semibold px-8 py-3 text-lg"
              >
                {{ $t('clientLanding.hero.viewArchitects') }}
              </router-link>
            </div>
          </div>
          <div class="hidden lg:block">
            <div class="hero-image-container">
              <img
                src="/src/assets/images/landing/hero-architecture.jpg"
                alt="Modern Architecture Design"
                class="w-full h-96 object-cover rounded-lg shadow-2xl"
                @error="handleImageError"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Services Section -->
    <section class="services-section py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-left mb-6">
          <h2 class="text-display-small font-display font-inter text-gray-900 mb-4">
            {{ $t('clientLanding.services.title') }}
          </h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="service in services"
            :key="service.id"
            class="service-card bg-white rounded-xl overflow-hidden hover:shadow-lg transition-shadow"
          >
            <div class="service-image mb-6">
              <img
                :src="service.image"
                :alt="$t(service.titleKey)"
                class="w-full h-55 object-cover"
                @error="handleImageError"
              />
            </div>
            <div class="px-6 pb-6">
              <h3 class="text-title-large font-title font-inter text-gray-900 mb-3">{{ $t(service.titleKey) }}</h3>
              <p class="text-body-medium font-body-regular font-inter text-gray-600 leading-relaxed">
                {{ $t(service.descriptionKey) }}
              </p>
            </div>
          </div>
        </div>

        <div class="text-left mt-6">
          <button
            class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button font-inter hover:bg-gray-800 transition-colors"
          >
            {{ $t('clientLanding.services.learnMore') }}
          </button>
        </div>
      </div>
    </section>

    <!-- Why Choose Us Section -->
    <section class="why-choose-section py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Left Content - 5 columns (10/22 ≈ 45%) -->
          <div class="lg:col-span-5">
            <h2 class="text-display-medium font-display font-inter text-gray-900 mb-4">
              {{ $t('clientLanding.whyChoose.title') }}
            </h2>
            <p class="text-body-large font-body-regular font-inter text-gray-600 mb-8">
              {{ $t('clientLanding.whyChoose.subtitle') }}
            </p>
            <button
              class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button font-inter hover:bg-gray-800 transition-colors"
            >
              {{ $t('clientLanding.whyChoose.tryNow') }}
            </button>
          </div>

          <!-- Right Image - 7 columns (12/22 ≈ 55%) -->
          <div class="lg:col-span-7">
            <img
              src="/src/assets/images/landing/everything-in-one-layer.jpg"
              alt="Everything in one layer - Modern workspace"
              class="w-full h-auto rounded-xl shadow-lg"
              @error="handleImageError"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- Popular Categories Section -->
    <section class="categories-section py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">Wujudkan Desain Impian Anda</h2>
          <p class="text-xl text-gray-600">Berbagai kategori desain sesuai kebutuhan Anda</p>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-card bg-gray-50 rounded-xl p-6 text-center hover:bg-blue-50 hover:border-blue-200 border border-transparent transition-all cursor-pointer"
            @click="searchByCategory(category.name)"
          >
            <div class="category-icon mb-4">
              <svg class="w-8 h-8 mx-auto text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="category.iconPath" />
              </svg>
            </div>
            <h3 class="text-sm font-medium text-gray-900">{{ category.name }}</h3>
            <p class="text-xs text-gray-500 mt-1">{{ category.count }}+ proyek</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Portfolio Preview Section -->
    <section class="portfolio-section py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <!-- Left Content Card -->
          <div class="lg:col-span-5">
            <div class="bg-white rounded-xl p-8 shadow-sm hover:shadow-lg transition-shadow">
              <h2 class="text-display-medium font-display font-inter text-gray-900 mb-4">
                {{ $t('clientLanding.portfolio.title') }}
              </h2>
              <p class="text-body-large font-body-regular font-inter text-gray-600 mb-8">
                {{ $t('clientLanding.portfolio.subtitle') }}
              </p>
              <button
                class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button font-inter hover:bg-gray-800 transition-colors"
              >
                {{ $t('clientLanding.portfolio.viewAll') }}
              </button>
            </div>
          </div>

          <!-- Right Portfolio Cards -->
          <div class="lg:col-span-7">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                v-for="portfolio in portfolios.slice(0, 3)"
                :key="portfolio.id"
                class="portfolio-card bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-lg transition-shadow cursor-pointer"
                @click="viewPortfolio(portfolio.id)"
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

    <!-- Inspiration Section -->
    <section class="inspiration-section py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Left Content -->
          <div class="lg:col-span-5">
            <h2 class="text-display-medium font-display font-inter text-gray-900 mb-4">
              {{ $t('clientLanding.inspiration.title') }}
            </h2>
            <p class="text-body-large font-body-regular font-inter text-gray-600 mb-8">
              {{ $t('clientLanding.inspiration.subtitle') }}
            </p>
            <button
              class="bg-black text-white px-6 py-3 rounded-full text-button-medium font-button font-inter hover:bg-gray-800 transition-colors"
            >
              {{ $t('clientLanding.inspiration.explore') }}
            </button>
          </div>

          <!-- Right Image -->
          <div class="lg:col-span-7">
            <img
              src="/src/assets/images/landing/inspiration-interior-design.jpg"
              alt="Interior Design Inspiration"
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
        <h2 class="text-3xl md:text-4xl font-bold mb-6">Siap Mulai Proyek Impian Anda?</h2>
        <p class="text-xl text-blue-100 mb-8 leading-relaxed">
          Bergabunglah dengan ribuan klien yang telah mewujudkan rumah impian mereka
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            @click="showSignInPopup = true"
            class="btn bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8 py-3 text-lg"
          >
            Mulai Sekarang
          </button>
          <router-link
            to="/about"
            class="btn btn-outline border-white text-white hover:bg-white hover:text-blue-600 font-semibold px-8 py-3 text-lg"
          >
            Pelajari Lebih Lanjut
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { mapState } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'ClientLandingPage',
  data() {
    return {
      showSignInPopup: false,
      services: [
        {
          id: 1,
          titleKey: 'clientLanding.services.consultation.title',
          descriptionKey: 'clientLanding.services.consultation.description',
          image: '/src/assets/images/landing/service-posting-project.jpg'
        },
        {
          id: 2,
          titleKey: 'clientLanding.services.customDesign.title',
          descriptionKey: 'clientLanding.services.customDesign.description',
          image: '/src/assets/images/landing/service-receive-offers.jpg'
        },
        {
          id: 3,
          titleKey: 'clientLanding.services.supervision.title',
          descriptionKey: 'clientLanding.services.supervision.description',
          image: '/src/assets/images/landing/service-payment-collaboration.jpg'
        }
      ],
      features: [
        {
          id: 1,
          title: 'Arsitek Bersertifikat',
          description: 'Semua arsitek telah terverifikasi dan memiliki sertifikasi resmi',
          iconPath:
            'M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z'
        },
        {
          id: 2,
          title: 'Harga Transparan',
          description: 'Tidak ada biaya tersembunyi, semua harga jelas dan kompetitif',
          iconPath:
            'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1'
        },
        {
          id: 3,
          title: 'Support 24/7',
          description: 'Tim support siap membantu Anda kapan saja selama proses berlangsung',
          iconPath:
            'M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z'
        }
      ],
      categories: [
        {
          id: 1,
          name: 'Rumah Tinggal',
          count: 150,
          iconPath:
            'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6'
        },
        {
          id: 2,
          name: 'Apartemen',
          count: 89,
          iconPath:
            'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4'
        },
        {
          id: 3,
          name: 'Kantor',
          count: 67,
          iconPath:
            'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4'
        },
        {
          id: 4,
          name: 'Restoran',
          count: 45,
          iconPath:
            'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'
        },
        {
          id: 5,
          name: 'Hotel',
          count: 32,
          iconPath: 'M8 14v3m4-3v3m4-3v3M3 21h18M3 10h18M3 7l9-4 9 4M4 10h16v11H4V10z'
        },
        {
          id: 6,
          name: 'Lainnya',
          count: 98,
          iconPath:
            'M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4'
        }
      ],
      portfolios: [
        {
          id: 1,
          title: 'Creative Professional at Work',
          description: 'Designer workspace inspiration',
          image: '/src/assets/images/landing/portfolio-designer-workspace.jpg',
          category: 'Workspace'
        },
        {
          id: 2,
          title: 'Architectural Planning Session',
          description: 'Professional architect reviewing designs',
          image: '/src/assets/images/landing/portfolio-architect-planning.jpg',
          category: 'Professional'
        },
        {
          id: 3,
          title: 'Modern Interior Design',
          description: 'Contemporary living space design',
          image: '/src/assets/images/landing/portfolio-interior-modern.jpg',
          category: 'Interior'
        },
        {
          id: 4,
          title: 'Kitchen Design Excellence',
          description: 'Functional and beautiful kitchen space',
          image: '/src/assets/images/landing/portfolio-kitchen-design.jpg',
          category: 'Kitchen'
        }
      ],
      inspirationImages: [
        { src: '/src/assets/images/landing/inspiration-1.jpg', alt: 'Modern Kitchen Design' },
        { src: '/src/assets/images/landing/inspiration-2.jpg', alt: 'Living Room Interior' },
        { src: '/src/assets/images/landing/inspiration-3.jpg', alt: 'Bedroom Design' },
        { src: '/src/assets/images/landing/inspiration-4.jpg', alt: 'Bathroom Interior' }
      ]
    }
  },
  computed: {
    ...mapState(useAuthStore, ['isAuthenticated'])
  },
  methods: {
    handleImageError(event) {
      // Replace broken images with placeholder
      event.target.src =
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xNiAxNkwyNCAyNE0xNiAyNEwyNCAyNCIgc3Ryb2tlPSIjOUI5QkEwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K'
    },
    searchByCategory(category) {
      this.$router.push(`/architects?category=${encodeURIComponent(category)}`)
    },
    viewPortfolio(portfolioId) {
      this.$router.push(`/portfolios/${portfolioId}`)
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

.btn-lg {
  @apply px-8 py-4 text-lg;
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

.service-card:hover {
  transform: translateY(-4px);
}

.portfolio-card:hover .portfolio-card img {
  transform: scale(1.05);
}

.category-card:hover {
  transform: translateY(-2px);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2.5rem;
  }

  .services-section .grid {
    grid-template-columns: 1fr;
  }

  .categories-section .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
