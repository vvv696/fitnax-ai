import { ref } from 'vue'

interface ToastInstance {
  add: (message: string, type?: 'success' | 'error' | 'info', duration?: number) => void
}

const toastRef = ref<ToastInstance | null>(null)

export function setToastInstance(instance: ToastInstance) {
  toastRef.value = instance
}

export function useToast() {
  return {
    success(msg: string) {
      toastRef.value?.add(msg, 'success')
    },
    error(msg: string) {
      toastRef.value?.add(msg, 'error')
    },
    info(msg: string) {
      toastRef.value?.add(msg, 'info')
    },
  }
}
