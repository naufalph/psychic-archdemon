<template>
  <div class="about">
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-primary-600 to-primary-800 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
        <div class="text-center">
          <h1 class="text-4xl md:text-6xl font-bold mb-6">
            About ArchMatch
          </h1>
          <p class="text-xl text-primary-100 mb-8 max-w-3xl mx-auto leading-relaxed">
            We're revolutionizing the way architects and clients connect, making it easier than ever
            to bring architectural visions to life.
          </p>
        </div>
      </div>
    </section>

    <!-- Mission Section -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
              Our Mission
            </h2>
            <p class="text-lg text-gray-600 mb-6 leading-relaxed">
              At ArchMatch, we believe that great architecture should be accessible to everyone.
              Our platform connects talented architects with clients who share their vision for
              exceptional design and sustainable building practices.
            </p>
            <p class="text-lg text-gray-600 mb-8 leading-relaxed">
              Whether you're a homeowner looking to renovate, a business planning a new office space,
              or a developer working on a large-scale project, we help you find the perfect architect
              for your needs.
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <router-link to="/projects" class="btn btn-primary">
                Browse Projects
              </router-link>
              <router-link to="/architects" class="btn btn-outline">
                Meet Our Architects
              </router-link>
            </div>
          </div>
          <div class="relative">
            <div class="aspect-square bg-gradient-to-br from-primary-100 to-primary-200 rounded-2xl flex items-center justify-center">
              <svg class="w-48 h-48 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2-2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Values Section -->
    <section class="py-24 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Our Values
          </h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            The principles that guide everything we do
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-12">
          <div v-for="value in values" :key="value.title" class="text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <component :is="value.icon" class="w-8 h-8 text-primary-600" />
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-4">{{ value.title }}</h3>
            <p class="text-gray-600 leading-relaxed">{{ value.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            How ArchMatch Works
          </h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            Our streamlined process makes it easy to find the right architectural partner
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div v-for="(step, index) in steps" :key="step.title" class="relative">
            <div class="text-center">
              <div class="w-12 h-12 bg-primary-600 rounded-full flex items-center justify-center mx-auto mb-6 text-white font-bold text-lg">
                {{ index + 1 }}
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-4">{{ step.title }}</h3>
              <p class="text-gray-600 text-sm leading-relaxed">{{ step.description }}</p>
            </div>

            <!-- Arrow for desktop -->
            <div v-if="index < steps.length - 1" class="hidden md:block absolute top-6 left-full w-full">
              <svg class="w-6 h-6 text-primary-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="py-24 bg-primary-600 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold mb-4">
            Our Impact
          </h2>
          <p class="text-xl text-primary-100 max-w-3xl mx-auto">
            Numbers that show how we're making a difference in the architecture industry
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 text-center">
          <div v-for="stat in stats" :key="stat.label" class="animate-fade-in">
            <div class="text-4xl md:text-5xl font-bold mb-2">{{ stat.value }}</div>
            <div class="text-primary-200 font-medium">{{ stat.label }}</div>
            <div v-if="stat.description" class="text-primary-100 text-sm mt-2">{{ stat.description }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Team Section -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Meet Our Team
          </h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            The passionate people behind ArchMatch
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-12">
          <div v-for="member in team" :key="member.name" class="text-center group">
            <div class="relative mb-6">
              <div class="w-32 h-32 bg-gray-200 rounded-full mx-auto overflow-hidden group-hover:shadow-lg transition-shadow">
                <img
                  v-if="member.photo"
                  :src="member.photo"
                  :alt="member.name"
                  class="w-full h-full object-cover"
                >
                <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200">
                  <svg class="w-16 h-16 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                </div>
              </div>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-2">{{ member.name }}</h3>
            <p class="text-primary-600 font-medium mb-4">{{ member.role }}</p>
            <p class="text-gray-600 text-sm leading-relaxed">{{ member.bio }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-24 bg-gray-50">
      <div class="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
          Ready to Get Started?
        </h2>
        <p class="text-xl text-gray-600 mb-8 leading-relaxed">
          Join our community of architects and clients creating amazing spaces together
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link to="/register" class="btn btn-primary btn-lg">
            Join ArchMatch Today
          </router-link>
          <router-link to="/projects" class="btn btn-outline btn-lg">
            Explore Projects
          </router-link>
        </div>
      </div>
    </section>

    <!-- Contact Info -->
    <section class="py-16 bg-white border-t border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
          <div>
            <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">Email Us</h3>
            <p class="text-gray-600">hello@archmatch.com</p>
            <p class="text-gray-600">support@archmatch.com</p>
          </div>

          <div>
            <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">Call Us</h3>
            <p class="text-gray-600">+1 (555) 123-4567</p>
            <p class="text-gray-600 text-sm">Mon-Fri 9AM-6PM EST</p>
          </div>

          <div>
            <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">Visit Us</h3>
            <p class="text-gray-600">123 Architecture Ave</p>
            <p class="text-gray-600">Design City, DC 12345</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'About',
  data() {
    return {
      values: [
        {
          title: 'Excellence',
          description: 'We maintain the highest standards in connecting clients with exceptional architectural talent.',
          icon: 'StarIcon'
        },
        {
          title: 'Innovation',
          description: 'We embrace new technologies and methodologies to improve the architectural collaboration process.',
          icon: 'LightBulbIcon'
        },
        {
          title: 'Transparency',
          description: 'We believe in clear communication, fair pricing, and honest relationships between all parties.',
          icon: 'EyeIcon'
        }
      ],
      steps: [
        {
          title: 'Post Project',
          description: 'Describe your architectural needs, budget, and timeline to attract qualified architects.'
        },
        {
          title: 'Receive Proposals',
          description: 'Review detailed proposals from pre-screened architects with portfolios and pricing.'
        },
        {
          title: 'Choose Partner',
          description: 'Select the architect that best matches your vision, style, and budget requirements.'
        },
        {
          title: 'Build Together',
          description: 'Collaborate on your project with built-in tools for communication and progress tracking.'
        }
      ],
      stats: [
        {
          value: '500+',
          label: 'Projects Completed',
          description: 'Successful collaborations'
        },
        {
          value: '300+',
          label: 'Verified Architects',
          description: 'Talented professionals'
        },
        {
          value: '1,000+',
          label: 'Happy Clients',
          description: 'Satisfied customers'
        },
        {
          value: '98%',
          label: 'Success Rate',
          description: 'Project completion'
        }
      ],
      team: [
        {
          name: 'Sarah Johnson',
          role: 'CEO & Founder',
          bio: 'Former architect with 15 years of experience in residential and commercial design. Passionate about connecting great design with the right clients.',
          photo: null
        },
        {
          name: 'Michael Chen',
          role: 'CTO',
          bio: 'Technology leader with expertise in marketplace platforms. Previously built successful platforms in construction and real estate.',
          photo: null
        },
        {
          name: 'Emily Rodriguez',
          role: 'Head of Community',
          bio: 'Community builder focused on creating meaningful connections between architects and clients. Former project manager at leading architecture firms.',
          photo: null
        }
      ]
    }
  },
  methods: {
    // Add methods here if needed
  }
}
</script>

<style scoped>
/* Animation for stats on scroll */
@media (prefers-reduced-motion: no-preference) {
  .animate-fade-in {
    opacity: 0;
    animation: fadeInUp 0.6s ease-out forwards;
  }

  .animate-fade-in:nth-child(1) { animation-delay: 0.1s; }
  .animate-fade-in:nth-child(2) { animation-delay: 0.2s; }
  .animate-fade-in:nth-child(3) { animation-delay: 0.3s; }
  .animate-fade-in:nth-child(4) { animation-delay: 0.4s; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Team member hover effects */
.group:hover .group-hover\:shadow-lg {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* Step number styling */
.step-number {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

/* Value cards hover effect */
.value-card {
  transition: transform 0.2s ease-in-out;
}

.value-card:hover {
  transform: translateY(-5px);
}

/* Responsive text sizing */
@media (max-width: 640px) {
  h1 {
    font-size: 2.5rem;
  }

  h2 {
    font-size: 2rem;
  }
}

/* Custom gradient backgrounds */
.bg-gradient-to-br {
  background-image: linear-gradient(to bottom right, var(--tw-gradient-stops));
}

/* Contact section styling */
.contact-item {
  transition: transform 0.2s ease;
}

.contact-item:hover {
  transform: translateY(-2px);
}
</style>
