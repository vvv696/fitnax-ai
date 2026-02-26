<template>
  <span
    :class="[
      'inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium',
      colorClasses,
    ]"
  >
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  color?: 'blue' | 'green' | 'red' | 'orange' | 'gray' | 'yellow'
}>(), {
  color: 'gray',
})

const colorClasses = computed(() => ({
  blue: 'bg-apple-accent/15 text-apple-accent',
  green: 'bg-apple-green/15 text-apple-green',
  red: 'bg-apple-red/15 text-apple-red',
  orange: 'bg-apple-orange/15 text-apple-orange',
  yellow: 'bg-apple-yellow/15 text-apple-yellow',
  gray: 'bg-white/10 text-gray-300',
}[props.color]))
</script>
