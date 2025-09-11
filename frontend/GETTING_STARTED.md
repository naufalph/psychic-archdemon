# Getting Started with Rumantra Frontend

Welcome to the Rumantra frontend! This guide will help you get up and running with the Vue 3 application for the Architecture Marketplace platform.

## 🚀 Quick Start

### Prerequisites
- **Node.js**: Version 18.0.0 or higher
- **npm**: Version 9.0.0 or higher
- **Backend API**: Running on `http://localhost:8080`

### Installation

1. **Navigate to the frontend directory:**
   ```bash
   cd psychic-archdemon/frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   npm run dev
   ```

4. **Open your browser:**
   ```
   http://localhost:3000
   ```

🎉 **You should now see the Rumantra homepage!**

## 📋 Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter
npm run lint

# Format code
npm run format
```

## 🏗️ Project Structure

```
src/
├── assets/          # Global styles and static assets
│   └── main.css     # Tailwind CSS with custom utilities
├── components/      # Reusable Vue components
│   └── ui/          # Base UI components
├── router/          # Vue Router configuration
│   └── index.js     # Routes and navigation guards
├── services/        # API services and HTTP client
│   └── api.js       # Axios configuration and endpoints
├── stores/          # Pinia stores for state management
│   └── auth.js      # Authentication store
├── views/           # Page components
│   ├── auth/        # Login, Register
│   ├── dashboard/   # User dashboard
│   ├── projects/    # Project listing and details
│   ├── architects/  # Architect profiles and listing
│   ├── user/        # User profile management
│   ├── admin/       # Admin dashboard
│   └── errors/      # Error pages (404, etc.)
├── App.vue          # Root component with navigation
└── main.js          # Application entry point
```

## 🎨 Key Features

### Authentication System
- **Login/Register** with form validation
- **JWT token management** with automatic refresh
- **Role-based access** (Client, Architect, Admin)
- **Protected routes** with navigation guards

### User Roles & Permissions
- **Client**: Post projects, review architect proposals
- **Architect**: Browse projects, submit proposals, manage portfolio
- **Admin**: System management and user oversight

### Responsive Design
- **Mobile-first** approach with Tailwind CSS
- **Responsive navigation** with mobile menu
- **Touch-friendly** interfaces
- **Progressive enhancement**

### State Management
- **Pinia stores** for centralized state
- **Authentication state** persistence
- **Reactive updates** across components

## 🔧 Development Workflow

### 1. Understanding the Codebase

**Start with these files:**
- `src/App.vue` - Main application layout
- `src/views/Home.vue` - Homepage component
- `src/router/index.js` - Route definitions
- `src/stores/auth.js` - Authentication logic

### 2. Adding New Pages

1. **Create a new Vue component** in `src/views/`
2. **Add the route** in `src/router/index.js`
3. **Update navigation** in `src/App.vue` if needed

Example:
```vue
<!-- src/views/MyNewPage.vue -->
<template>
  <div class="my-new-page">
    <h1>My New Page</h1>
  </div>
</template>

<script>
export default {
  name: 'MyNewPage'
}
</script>
```

### 3. Working with the API

The API service is configured in `src/services/api.js`:

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

### 4. Styling Components

Use the predefined CSS classes in `src/assets/main.css`:

```vue
<template>
  <div class="card">
    <div class="card-header">
      <h2>Project Title</h2>
    </div>
    <div class="card-body">
      <p>Project description...</p>
      <button class="btn btn-primary">View Details</button>
    </div>
  </div>
</template>
```

## 🎯 Common Patterns

### Form Handling with Validation

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <div>
      <label class="form-label">Email</label>
      <input
        v-model="form.email"
        type="email"
        class="form-input"
        :class="{ 'border-red-300': errors.email }"
        @blur="validateField('email')"
      />
      <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
  </form>
</template>

<script>
export default {
  data() {
    return {
      form: { email: '' },
      errors: {}
    }
  },
  methods: {
    validateField(field) {
      // Validation logic
    },
    handleSubmit() {
      // Form submission
    }
  }
}
</script>
```

### Using Pinia Stores

```vue
<script>
import { mapState, mapActions } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export default {
  computed: {
    ...mapState(useAuthStore, ['user', 'isAuthenticated'])
  },
  methods: {
    ...mapActions(useAuthStore, ['login', 'logout'])
  }
}
</script>
```

### Loading States

```vue
<template>
  <div>
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="spinner w-6 h-6"></div>
      <span class="ml-2">Loading...</span>
    </div>
    <div v-else>
      <!-- Content -->
    </div>
  </div>
</template>
```

## 🛠️ Development Tips

### 1. Hot Reload
The development server supports hot reload. Changes to `.vue`, `.js`, and `.css` files will automatically update the browser.

### 2. Vue DevTools
Install the [Vue DevTools browser extension](https://devtools.vuejs.org/) for debugging:
- Component inspection
- Pinia state management
- Route debugging
- Performance profiling

### 3. Code Formatting
The project uses ESLint and Prettier:
```bash
# Check for linting errors
npm run lint

# Auto-format code
npm run format
```

### 4. Environment Variables
Create a `.env.local` file for local development:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_DEBUG_MODE=true
```

### 5. Mock Data
Many components include mock data for development. Look for `TODO` comments to replace with real API calls.

## 🚨 Common Issues & Solutions

### 1. CORS Errors
If you see CORS errors, ensure your backend allows requests from `http://localhost:3000`.

### 2. Route Not Found
Check that:
- Route is defined in `src/router/index.js`
- Component file exists and is properly imported
- Path matches exactly (case-sensitive)

### 3. Authentication Issues
- Check browser console for token errors
- Verify backend is running on port 8080
- Clear localStorage if needed: `localStorage.clear()`

### 4. Styling Issues
- Ensure Tailwind classes are correct
- Check if custom CSS classes are defined in `main.css`
- Use browser dev tools to inspect element styles

## 📚 Next Steps

### For Beginners
1. **Explore the Homepage** (`src/views/Home.vue`)
2. **Try the Login flow** (`src/views/auth/Login.vue`)
3. **Browse the Dashboard** (`src/views/dashboard/Dashboard.vue`)
4. **Check out the Project listing** (`src/views/projects/ProjectList.vue`)

### For Developers
1. **Study the router configuration** for navigation patterns
2. **Examine the auth store** for state management
3. **Review the API service** for backend integration
4. **Customize the Tailwind config** for your design needs

### Adding Real Features
1. **Connect to actual backend APIs**
2. **Implement file upload functionality**
3. **Add real-time notifications**
4. **Integrate payment processing**
5. **Build advanced search and filtering**

## 🤝 Contributing

1. **Create a feature branch**
2. **Follow the coding standards** (ESLint + Prettier)
3. **Test your changes thoroughly**
4. **Update documentation as needed**
5. **Submit a pull request**

## 📞 Getting Help

- **Documentation**: Check the README.md for detailed information
- **Code Comments**: Look for inline comments explaining complex logic
- **Console Logs**: Use browser dev tools to debug issues
- **Vue 3 Docs**: https://vuejs.org/guide/
- **Tailwind CSS**: https://tailwindcss.com/docs
