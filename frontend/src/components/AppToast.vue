<template>
  <Teleport to="body">
    <TransitionGroup
      name="toast"
      tag="div"
      class="fixed top-4 right-4 z-[60] flex flex-col gap-2"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
          'flex items-center gap-2 px-4 py-3 rounded-apple shadow-apple-lg text-sm min-w-[280px] max-w-[400px]',
          'border backdrop-blur-apple',
          typeClasses[toast.type],
        ]"
      >
        <span class="flex-1">{{ toast.message }}</span>
        <button class="text-current opacity-60 hover:opacity-100" @click="remove(toast.id)">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

const toasts = ref<Toast[]>([])
let nextId = 0

const typeClasses: Record<string, string> = {
  success: 'bg-apple-green/10 border-apple-green/20 text-apple-green',
  error: 'bg-apple-red/10 border-apple-red/20 text-apple-red',
  info: 'bg-apple-accent/10 border-apple-accent/20 text-apple-accent',
}

function add(message: string, type: Toast['type'] = 'info', duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, message, type })
  setTimeout(() => remove(id), duration)
}

function remove(id: number) {
  const idx = toasts.value.findIndex((t) => t.id === id)
  if (idx >= 0) toasts.value.splice(idx, 1)
}

defineExpose({ add })
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 250ms ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(40px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(40px);
}
</style>
