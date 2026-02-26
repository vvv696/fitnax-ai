<template>
  <div class="flex flex-col gap-1">
    <label v-if="label" class="text-xs font-medium text-gray-400">{{ label }}</label>
    <select
      :value="modelValue"
      :disabled="disabled"
      class="w-full rounded-lg bg-apple-bg-tertiary border border-apple-separator px-3 py-2 text-sm text-gray-100
             focus:outline-none focus:border-apple-accent focus:ring-1 focus:ring-apple-accent/50
             disabled:opacity-40 disabled:cursor-not-allowed appearance-none
             bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%3E%3Cpath%20fill%3D%22%239ca3af%22%20d%3D%22M3%204.5L6%208l3-3.5%22%2F%3E%3C%2Fsvg%3E')]
             bg-[position:right_8px_center] bg-no-repeat pr-8"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  modelValue?: string
  label?: string
  placeholder?: string
  disabled?: boolean
  options: { value: string; label: string }[]
}>(), {
  modelValue: '',
  disabled: false,
})

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
