<template>
  <MainLayout>
    <div class="p-6">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-2xl font-semibold text-white">Asset Reconciliation</h2>
          <p class="text-sm text-gray-400 mt-1">Account ID: {{ mainAccountId }}</p>
        </div>
        <div class="flex items-center gap-2">
          <AppButton @click="generateSnapshot" :loading="generating">Generate Snapshot</AppButton>
          <AppButton variant="secondary" @click="exportAssets">Export Excel</AppButton>
          <AppButton variant="ghost" @click="$router.push('/accounts')">Back</AppButton>
        </div>
      </div>

      <!-- Filters -->
      <div class="flex flex-wrap items-end gap-3 mb-4 p-4 bg-apple-card border border-apple-separator rounded-apple-lg">
        <AppInput v-model="filters.currency" label="Currency" placeholder="e.g. ETH" />
        <AppInput v-model="filters.startTime" label="Start Time" placeholder="2025-01-01 00:00:00" />
        <AppInput v-model="filters.endTime" label="End Time" placeholder="2025-12-31 23:59:59" />
        <AppButton size="sm" @click="loadSnapshots()">Search</AppButton>
      </div>

      <!-- Reconcile Table -->
      <div class="bg-apple-card border border-apple-separator rounded-apple-lg overflow-hidden">
        <table class="apple-table">
          <thead>
            <tr>
              <th>Sub Account</th>
              <th>Currency</th>
              <th>Timestamp</th>
              <th>System Total</th>
              <th>On-chain Balance</th>
              <th>Diff</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="loading">
              <tr><td colspan="7" class="text-center py-8 text-gray-500">Loading...</td></tr>
            </template>
            <template v-else-if="snapshots.length === 0">
              <tr><td colspan="7" class="text-center py-8 text-gray-500">No snapshots found. Click "Generate Snapshot" to create one.</td></tr>
            </template>
            <tr v-for="snap in snapshots" :key="snap.id">
              <td>{{ snap.subAccountName || 'Sub #' + snap.subAccountId }}</td>
              <td><AppTag color="blue">{{ snap.currency }}</AppTag></td>
              <td class="text-xs">{{ formatTime(snap.snapshotTime) }}</td>
              <td class="font-mono">{{ snap.systemTotal }}</td>
              <td class="font-mono">{{ snap.onchainBalance }}</td>
              <td :class="snap.diff === 0 ? 'text-gray-400' : 'text-apple-red'" class="font-mono">
                {{ snap.diff }}
              </td>
              <td>
                <AppTag :color="snap.diffStatus === 'Matched' ? 'green' : 'red'">
                  {{ snap.diffStatus }}
                </AppTag>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between mt-4 text-sm text-gray-400">
        <span>Total: {{ totalRow }} snapshots</span>
        <div class="flex items-center gap-2">
          <AppButton variant="ghost" size="sm" :disabled="pageNumber <= 1" @click="pageNumber--; loadSnapshots()">Previous</AppButton>
          <span>Page {{ pageNumber }}</span>
          <AppButton variant="ghost" size="sm" :disabled="snapshots.length < pageSize" @click="pageNumber++; loadSnapshots()">Next</AppButton>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AppButton from '@/components/AppButton.vue'
import AppInput from '@/components/AppInput.vue'
import AppTag from '@/components/AppTag.vue'
import { reconcileApi } from '@/api'
import type { ReconcileSnapshot } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()

const mainAccountId = computed(() => Number(route.params.mainAccountId))

const snapshots = ref<ReconcileSnapshot[]>([])
const totalRow = ref(0)
const pageNumber = ref(1)
const pageSize = 20
const loading = ref(false)
const generating = ref(false)

const filters = reactive({
  currency: '',
  startTime: '',
  endTime: '',
})

onMounted(() => loadSnapshots())

async function loadSnapshots() {
  loading.value = true
  try {
    const data = await reconcileApi.list({
      mainAccountId: mainAccountId.value,
      pageNumber: pageNumber.value,
      pageSize,
      currency: filters.currency || undefined,
      startTime: filters.startTime || undefined,
      endTime: filters.endTime || undefined,
    })
    snapshots.value = data.records
    totalRow.value = data.totalRow
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function generateSnapshot() {
  generating.value = true
  try {
    const result = await reconcileApi.generate(mainAccountId.value)
    toast.success(`Generated ${result.length} snapshot(s)`)
    await loadSnapshots()
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    generating.value = false
  }
}

function exportAssets() {
  const url = reconcileApi.exportUrl({
    mainAccountId: mainAccountId.value,
    currency: filters.currency || undefined,
    startTime: filters.startTime || undefined,
    endTime: filters.endTime || undefined,
  })
  window.open(url, '_blank')
}

function formatTime(time: string | null): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>
