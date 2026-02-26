<template>
  <div class="flex flex-col gap-1">
    <label v-if="label" class="text-xs font-medium text-gray-400">{{ label }}</label>
    <input
      :value="modelValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      class="w-full rounded-lg bg-apple-bg-tertiary border border-apple-separator px-3 py-2 text-sm text-gray-100
             placeholder-gray-500 focus:outline-none focus:border-apple-accent focus:ring-1 focus:ring-apple-accent/50
             disabled:opacity-40 disabled:cursor-not-allowed"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  modelValue?: string
  label?: string
  placeholder?: string
  type?: string
  disabled?: boolean
}>(), {
  modelValue: '',
  type: 'text',
  disabled: false,
})

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
