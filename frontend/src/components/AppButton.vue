<template>
  <button
    :class="[
      'inline-flex items-center justify-center gap-1.5 rounded-lg font-medium transition-all',
      'focus:outline-none focus:ring-2 focus:ring-apple-accent/50',
      'disabled:opacity-40 disabled:cursor-not-allowed',
      sizeClasses,
      variantClasses,
    ]"
    :disabled="disabled || loading"
  >
    <svg v-if="loading" class="animate-spin h-4 w-4" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
}>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
})

const sizeClasses = computed(() => ({
  sm: 'px-2.5 py-1.5 text-xs',
  md: 'px-3.5 py-2 text-sm',
  lg: 'px-5 py-2.5 text-base',
}[props.size]))

const variantClasses = computed(() => ({
  primary: 'bg-apple-accent hover:bg-apple-accent-hover text-white',
  secondary: 'bg-apple-bg-tertiary hover:bg-[#48484a] text-gray-200',
  ghost: 'bg-transparent hover:bg-white/[0.06] text-gray-300',
  danger: 'bg-apple-red/10 hover:bg-apple-red/20 text-apple-red',
}[props.variant]))
</script>
