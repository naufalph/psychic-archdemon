# ArchMatch Frontend

A Vue 3 frontend application for the Architecture Marketplace platform, connecting architects with clients for building and design projects.

## 🚀 Tech Stack

- **Vue 3** - Progressive JavaScript framework (Options API + SFC)
- **Vite** - Fast build tool and dev server
- **Vue Router 4** - Client-side routing
- **Pinia** - State management
- **Tailwind CSS** - Utility-first CSS framework
- **HeadlessUI** - Unstyled UI components
- **VeeValidate** - Form validation
- **Chart.js** - Data visualization
- **Axios** - HTTP client
- **ESLint + Prettier** - Code linting and formatting

## 📋 Prerequisites

- Node.js 18.0.0 or higher
- npm 9.0.0 or higher
- Backend API running on `http://localhost:8080`

## 🛠️ Installation

1. **Clone the repository and navigate to frontend directory:**
   ```bash
   cd psychic-archdemon/frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` file with your configuration:
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api
   VITE_APP_NAME=ArchMatch
   VITE_DEBUG_MODE=true
   ```

## 🚦 Development

### Start development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Other commands:
```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Format code
npm run format
```

## 📁 Project Structure

```
src/
├── assets/          # Static assets (images, fonts, styles)
│   └── main.css     # Global styles with Tailwind
├── components/      # Reusable components
│   └── ui/          # Base UI components
├── router/          # Vue Router configuration
│   └── index.js     # Route definitions and guards
├── services/        # API services and utilities
│   └── api.js       # Axios configuration and API endpoints
├── stores/          # Pinia stores
│   └── auth.js      # Authentication store
├── utils/           # Utility functions
├── views/           # Page components
│   ├── auth/        # Authentication pages
│   ├── dashboard/   # Dashboard pages
│   ├── projects/    # Project-related pages
│   ├── architects/  # Architect-related pages
│   ├── admin/       # Admin pages
│   └── errors/      # Error pages
├── App.vue          # Root component
└── main.js          # Application entry point
```

## 🎨 Styling Guide

### Tailwind CSS Classes
We use a consistent set of custom CSS classes defined in `main.css`:

```css
/* Buttons */
.btn              /* Base button class */
.btn-primary      /* Primary button style */
.btn-secondary    /* Secondary button style */
.btn-outline      /* Outlined button style */
.btn-danger       /* Danger/delete button style */

/* Form elements */
.form-input       /* Input field styling */
.form-label       /* Label styling */
.form-error       /* Error message styling */

/* Cards */
.card             /* Card container */
.card-header      /* Card header section */
.card-body        /* Card content area */
.card-footer      /* Card footer section */

/* Navigation */
.nav-link         /* Navigation link styling */
.nav-link.active  /* Active navigation state */
```

### Color Palette
- **Primary**: Blue shades (`primary-50` to `primary-950`)
- **Secondary**: Gray shades (`gray-50` to `gray-950`)
- **Success**: Green (`green-100`, `green-600`, `green-800`)
- **Warning**: Yellow (`yellow-100`, `yellow-600`, `yellow-800`)
- **Danger**: Red (`red-100`, `red-600`, `red-800`)

## 🔐 Authentication

The app uses JWT token-based authentication with the following features:

- Login/Register forms with validation
- Token refresh mechanism
- Protected routes with role-based access
- Persistent login (remember me)
- Account lockout after failed attempts

### User Roles
- **CLIENT** - Can post projects and hire architects
- **ARCHITECT** - Can bid on projects and showcase portfolio
- **ADMIN** - Full system access and management

## 🚀 API Integration

### Base Configuration
```javascript
// services/api.js
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000
});
```

### Available API Modules
- `authAPI` - Authentication endpoints
- `userAPI` - User management
- `projectAPI` - Project operations
- `architectAPI` - Architect profiles and portfolios
- `bidAPI` - Bidding system
- `adminAPI` - Admin operations

### Usage Example
```javascript
import { projectAPI } from '@/services/api'

export default {
  async mounted() {
    try {
      const response = await projectAPI.getAll()
      this.projects = response.data
    } catch (error) {
      console.error('Failed to load projects:', error)
    }
  }
}
```

## 🛡️ Route Protection

Routes are protected using navigation guards in `router/index.js`:

```javascript
// Protected route example
{
  path: '/dashboard',
  name: 'Dashboard',
  component: Dashboard,
  meta: {
    requiresAuth: true
  }
}

// Role-based protection
{
  path: '/admin',
  name: 'AdminDashboard',
  component: AdminDashboard,
  meta: {
    requiresAuth: true,
    roles: ['ADMIN']
  }
}
```

## 📱 Responsive Design

The application is fully responsive with breakpoints:
- **Mobile**: < 640px
- **Tablet**: 640px - 1024px
- **Desktop**: > 1024px

Key responsive patterns:
- Mobile-first CSS approach
- Collapsible navigation menu
- Responsive grid layouts
- Touch-friendly interface elements

## 🧪 Form Validation

Using VeeValidate for form validation:

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <input
      v-model="form.email"
      type="email"
      class="form-input"
      :class="{ 'border-red-300': errors.email }"
      @blur="validateField('email')"
    />
    <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
  </form>
</template>
```

## 🎯 State Management

Using Pinia for state management with organized stores:

```javascript
// stores/auth.js
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('auth_token'),
    isLoading: false
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    userRole: (state) => state.user?.role || null
  },
  
  actions: {
    async login(credentials) {
      // Login implementation
    }
  }
})
```

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

### Environment Variables for Production
```env
NODE_ENV=production
VITE_API_BASE_URL=https://api.your-domain.com/api
VITE_DEBUG_MODE=false
VITE_ENABLE_ANALYTICS=true
```

### Deployment Options
- **Vercel**: Connect GitHub repo and deploy automatically
- **Netlify**: Drag & drop `dist` folder or connect repository
- **AWS S3 + CloudFront**: Upload build files to S3 bucket
- **Nginx**: Serve static files with proper routing configuration

### Nginx Configuration Example
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/archmatch-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://your-backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🔧 Development Guidelines

### Code Style
- Use Vue 3 Options API for consistency
- Follow ESLint and Prettier configurations
- Use meaningful component and variable names
- Add JSDoc comments for complex functions

### Component Structure
```vue
<template>
  <!-- Template content -->
</template>

<script>
export default {
  name: 'ComponentName',
  props: {
    // Component props
  },
  data() {
    return {
      // Component data
    }
  },
  computed: {
    // Computed properties
  },
  methods: {
    // Component methods
  },
  mounted() {
    // Lifecycle hooks
  }
}
</script>

<style scoped>
/* Component-specific styles */
</style>
```

### Performance Best Practices
- Use lazy loading for route components
- Optimize images and assets
- Implement proper error boundaries
- Use v-memo for expensive list rendering
- Minimize bundle size with tree shaking

## 🐛 Debugging

### Development Tools
- Vue DevTools browser extension
- Network tab for API calls
- Console logging in development mode
- Source maps for debugging built code

### Common Issues
1. **CORS errors**: Ensure backend allows frontend origin
2. **Route not found**: Check route definitions in `router/index.js`
3. **API errors**: Verify backend is running and endpoints exist
4. **Authentication issues**: Clear localStorage and check token validity

## 📚 Resources

- [Vue 3 Documentation](https://vuejs.org/guide/)
- [Vue Router Documentation](https://router.vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Vite Documentation](https://vitejs.dev/guide/)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run tests and linting
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.