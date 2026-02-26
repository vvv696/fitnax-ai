<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="$emit('close')" />
        <div
          class="relative bg-apple-bg-secondary border border-apple-separator rounded-apple-lg shadow-apple-lg
                 w-full max-h-[85vh] overflow-y-auto"
          :class="widthClass"
        >
          <div class="flex items-center justify-between px-6 py-4 border-b border-apple-separator">
            <h3 class="text-lg font-semibold text-white">{{ title }}</h3>
            <button
              class="p-1 rounded-lg hover:bg-white/[0.06] text-gray-400 hover:text-gray-200"
              @click="$emit('close')"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="px-6 py-4">
            <slot />
          </div>
          <div v-if="$slots.footer" class="flex items-center justify-end gap-2 px-6 py-4 border-t border-apple-separator">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title: string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  size: 'md',
})

defineEmits<{
  close: []
}>()

const widthClass = computed(() => ({
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
}[props.size]))
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 200ms ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
