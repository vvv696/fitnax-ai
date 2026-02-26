<template>
  <div class="relative" ref="containerRef">
    <button
      class="p-1.5 rounded-lg hover:bg-white/[0.06] text-gray-400 hover:text-gray-200"
      @click.stop="open = !open"
    >
      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <circle cx="10" cy="4" r="1.5" />
        <circle cx="10" cy="10" r="1.5" />
        <circle cx="10" cy="16" r="1.5" />
      </svg>
    </button>
    <Transition name="dropdown">
      <div
        v-if="open"
        class="absolute right-0 mt-1 py-1 w-48 bg-apple-bg-tertiary border border-apple-separator
               rounded-apple shadow-apple-lg z-40"
      >
        <button
          v-for="item in items"
          :key="item.key"
          class="w-full text-left px-3 py-2 text-sm hover:bg-white/[0.06] flex items-center gap-2"
          :class="item.danger ? 'text-apple-red' : 'text-gray-200'"
          @click="handleClick(item)"
        >
          {{ item.label }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

export interface DropdownItem {
  key: string
  label: string
  danger?: boolean
}

defineProps<{
  items: DropdownItem[]
}>()

const emit = defineEmits<{
  select: [key: string]
}>()

const open = ref(false)
const containerRef = ref<HTMLElement | null>(null)

function handleClick(item: DropdownItem) {
  open.value = false
  emit('select', item.key)
}

function onClickOutside(e: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(e.target as Node)) {
    open.value = false
  }
}

onMounted(() => document.addEventListener('click', onClickOutside))
onBeforeUnmount(() => document.removeEventListener('click', onClickOutside))
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 150ms ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
