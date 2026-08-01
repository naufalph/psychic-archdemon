/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Neutral / marketing family — landing & shared chrome
        'ink-900': '#0A0A0A',
        'ink-700': '#1C1C1C',
        'ink-500': '#333333',
        'ink-400': '#666666',
        'ink-300': '#888888',
        'ink-200': '#AAAAAA',
        'surface': '#FAFAFA',
        'surface-alt': '#F4F5F7',
        'surface-muted': '#F5F5F5',
        'surface-card': '#FFFFFF',
        'hairline': '#E8E8E8',
        'hairline-alt': '#CCCCCC',
        'border-gray': '#E5E7EB',
        // Premium / dashboard-auth family
        'brand-brown': '#7C4728',
        'brand-brown-dark': '#6A3D22',
        'brand-brown-light': '#9B5E3C',
        'brand-brown-900': '#3D2114',
        'brand-brown-hover': '#5A3419',
        'brand-gold': '#C5A17A',
        'brand-gold-light': '#B39069',
        'brand-tan': '#F5E6D3',
        'brand-cream': '#FDF6EE',
        // Shared / status
        'brand-green': '#10B981',
        'brand-yellow': '#FBBF24',
        'status-green-dark': '#1A7A2E',
        // Net-new accents introduced by the marketing design handoff
        'accent-blue': '#185C93',
        'cta-blue': '#2F7DC0',
        'surface-blue': '#E6F0F8',
      },
      fontSize: {
        h1: ['56px', { lineHeight: '1.1', letterSpacing: '-0.03em', fontWeight: '700' }],
        h2: ['48px', { lineHeight: '1.1', letterSpacing: '-0.03em', fontWeight: '600' }],
        'h2-alt': ['32px', { lineHeight: '1.15', letterSpacing: '-0.02em', fontWeight: '700' }],
        'section-h1': ['40px', { lineHeight: '1.15', letterSpacing: '-0.02em', fontWeight: '700' }],
        h3: ['20px', { lineHeight: '1.3', letterSpacing: '-0.02em', fontWeight: '600' }],
        numeral: ['64px', { lineHeight: '1', fontWeight: '900' }],
        'body-lg': ['18px', { lineHeight: '1.6', fontWeight: '400' }],
        body: ['16px', { lineHeight: '1.6', fontWeight: '400' }],
        'body-sm': ['15px', { lineHeight: '1.5', fontWeight: '400' }],
        caption: ['14px', { lineHeight: '1.4', fontWeight: '500' }],
        'caption-sm': ['13px', { lineHeight: '1.4', fontWeight: '500' }],
        micro: ['12px', { lineHeight: '1.3', fontWeight: '500' }],
        'micro-sm': ['11px', { lineHeight: '1.3', fontWeight: '600' }],
        nano: ['10px', { lineHeight: '1.2', fontWeight: '600', letterSpacing: '0.02em' }],
        'micro-cap': ['9px', { lineHeight: '1.2', fontWeight: '700', letterSpacing: '0.05em' }],
      },
      borderRadius: {
        card: '20px',
      },
      boxShadow: {
        'soft': '0 4px 20px -2px rgba(0, 0, 0, 0.05)',
        'glow': '0 0 15px rgba(16, 185, 129, 0.2)',
        'popover': '0 8px 30px rgba(10, 10, 10, 0.15)',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-up': 'slideUp 0.6s ease-out',
        'float': 'float 4s ease-in-out infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-6px)' },
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
