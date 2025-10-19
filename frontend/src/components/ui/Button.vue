<template>
  <button
    :class="[
      'btn',
      `btn-${size}`,
      `btn-${variant}`,
      {
        'btn-icon-left': leftIcon,
        'btn-icon-right': rightIcon
      }
    ]"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <i v-if="leftIcon" class="btn-icon material-icons">{{ leftIcon }}</i>
    <slot>{{ text }}</slot>
    <i v-if="rightIcon" class="btn-icon material-icons">{{ rightIcon }}</i>
  </button>
</template>

<script>
export default {
  name: 'Button',
  props: {
    text: {
      type: String,
      default: 'Button'
    },
    size: {
      type: String,
      default: 'medium',
      validator: value => ['medium', 'large'].includes(value)
    },
    variant: {
      type: String,
      default: 'filled',
      validator: value => ['filled', 'outlined', 'gradient'].includes(value)
    },
    leftIcon: {
      type: String,
      default: null
    },
    rightIcon: {
      type: String,
      default: null
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
}
</script>

<style scoped>
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 100px;
  font-family: 'Inter', sans-serif;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
  outline: none;
  border: none;
}

.btn-medium {
  width: 159px;
  height: 40px;
  padding: 10px 20px;
  font-size: 14px;
}

.btn-large {
  width: 206px;
  height: 44px;
  padding: 10px 40px;
  font-size: 16px;
}

.btn-filled {
  background-color: #000000;
  color: #ffffff;
}

.btn-filled:hover {
  background-color: #333333;
}

.btn-filled:active {
  background-color: #4d4d4d;
}

.btn-outlined {
  background-color: #ffffff;
  color: #000000;
  border: 2px solid #000000;
}

.btn-outlined:hover {
  background-color: #e6e6e6;
}

.btn-outlined:active {
  background-color: #cccccc;
}

.btn-gradient {
  background: linear-gradient(to right, #185c93, #0c82ee);
  background-size: 200% 100%;
  color: #ffffff;
  animation: gradientShift 3s ease-in-out infinite;
}

.btn-gradient:hover {
  background-position: 100% 0;
}

.btn-gradient:active {
  background-position: 0% 0;
}

.btn-icon {
  font-size: 20px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
