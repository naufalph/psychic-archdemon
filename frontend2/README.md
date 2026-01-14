# Rumantra Frontend2 - Premium Architecture Marketplace

Fresh Vue 3 implementation of Rumantra's marketing website with premium design from the React prototype.

## Features

- **Bilingual Support**: Full Indonesian & English translations (toggle with language switcher)
- **Premium Design**: Modern, clean aesthetic with animations
- **Component Library**: Reusable UI components (BaseButton, BaseInput, BaseCard)
- **Marketing Pages**: Landing page with hero, features, gallery sections
- **Backend Ready**: Auth store and API service copied from original frontend

## Tech Stack

- Vue 3 with Composition API
- Vite (dev server on port 3001)
- Tailwind CSS with custom design tokens
- Pinia (state management)
- Vue Router 4
- @vueuse/motion (animations)
- lucide-vue-next (icons)
- vue-chartjs & chart.js (charts)

## Development

```bash
npm run dev      # Start dev server (http://localhost:3001)
npm run build    # Build for production
npm run preview  # Preview production build
```

## i18n System

Simple composable-based i18n:
- Locales: src/locales/id.js (Indonesian), src/locales/en.js (English)
- Usage: const { t } = useI18n()
- Language switcher in Navbar

## Port Configuration

- Dev server: 3001 (different from original frontend:3000)
- Backend proxy: /api → http://localhost:8080
