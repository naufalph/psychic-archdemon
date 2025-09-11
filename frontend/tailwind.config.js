/** @type {import('tailwindcss').Config} */
const typography = require('./src/design-system/typography.js')

export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
          950: '#172554',
        },
        secondary: {
          50: '#f9fafb',
          100: '#f3f4f6',
          200: '#e5e7eb',
          300: '#d1d5db',
          400: '#9ca3af',
          500: '#6b7280',
          600: '#4b5563',
          700: '#374151',
          800: '#1f2937',
          900: '#111827',
          950: '#030712',
        }
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'ui-monospace', 'monospace'],
        'inter': ['Inter', 'sans-serif']
      },
      fontSize: {
        'display-large': typography.typography.display.large.fontSize,
        'display-medium': typography.typography.display.medium.fontSize,
        'display-small': typography.typography.display.small.fontSize,
        'headline-large': typography.typography.headline.large.fontSize,
        'headline-medium': typography.typography.headline.medium.fontSize,
        'headline-small': typography.typography.headline.small.fontSize,
        'title-large': typography.typography.title.large.fontSize,
        'title-medium': typography.typography.title.medium.fontSize,
        'title-small': typography.typography.title.small.fontSize,
        'body-large': typography.typography.body.large.fontSize,
        'body-medium': typography.typography.body.medium.fontSize,
        'body-small': typography.typography.body.small.fontSize,
        'button-large': typography.typography.button.large.fontSize,
        'button-medium': typography.typography.button.medium.fontSize
      },
      fontWeight: {
        'display': '700',
        'headline': '500',
        'title': '500',
        'body-regular': '400',
        'button': '500'
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-in': 'slideIn 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
