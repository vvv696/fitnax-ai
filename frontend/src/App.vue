<template>
  <RouterView />
  <AppToast ref="toastRef" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterView } from 'vue-router'
import AppToast from '@/components/AppToast.vue'
import { setToastInstance } from '@/composables/useToast'

const toastRef = ref<InstanceType<typeof AppToast> | null>(null)

onMounted(() => {
  if (toastRef.value) {
    setToastInstance(toastRef.value)
  }
})
</script>
