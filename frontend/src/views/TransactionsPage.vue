<template>
  <MainLayout>
    <div class="p-6">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-semibold text-white">Transactions</h2>
      </div>

      <!-- Filter Bar -->
      <div class="flex flex-wrap items-end gap-3 mb-4 p-4 bg-apple-card border border-apple-separator rounded-apple-lg">
        <AppInput v-model="filters.txHash" label="Txn ID" placeholder="Search tx hash..." />
        <AppSelect v-model="filters.direction" label="Direction" :options="directionOptions" placeholder="All" />
        <AppSelect v-model="filters.category" label="Category" :options="categoryOptions" placeholder="All" />
        <AppInput v-model="filters.chainName" label="Chain" placeholder="e.g. Ethereum" />
        <AppInput v-model="filters.startTime" label="Start Time" placeholder="2025-01-01 00:00:00" />
        <AppInput v-model="filters.endTime" label="End Time" placeholder="2025-12-31 23:59:59" />
        <AppButton size="sm" @click="loadGroups()">Search</AppButton>
        <AppButton size="sm" variant="ghost" @click="clearFilters">Clear</AppButton>
      </div>

      <!-- Tx Group Table -->
      <div class="bg-apple-card border border-apple-separator rounded-apple-lg overflow-hidden">
        <table class="apple-table">
          <thead>
            <tr>
              <th class="w-8"></th>
              <th>Category</th>
              <th>Direction</th>
              <th>Account Name</th>
              <th>Trade Time</th>
              <th>Inflow</th>
              <th>Outflow</th>
              <th>Fee</th>
              <th>Txn ID</th>
              <th>Chain</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="loading">
              <tr><td colspan="11" class="text-center py-8 text-gray-500">Loading...</td></tr>
            </template>
            <template v-else-if="groups.length === 0">
              <tr><td colspan="11" class="text-center py-8 text-gray-500">No transactions found</td></tr>
            </template>
            <template v-for="group in groups" :key="group.txHash">
              <tr class="cursor-pointer" @click="toggleExpand(group.txHash)">
                <td class="text-center">
                  <svg
                    class="w-4 h-4 inline-block transition-transform"
                    :class="{ 'rotate-90': expandedHashes.has(group.txHash) }"
                    fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </td>
                <td>
                  <div class="flex items-center gap-2">
                    <AppTag :color="categoryColor(group.category)">{{ group.category }}</AppTag>
                    <span v-if="group.businessType" class="text-xs text-gray-500">{{ group.businessType }}</span>
                    <button
                      class="text-xs text-apple-accent hover:underline"
                      @click.stop="openEditBizType(group)"
                    >edit</button>
                  </div>
                </td>
                <td>
                  <div class="text-xs">
                    <div class="text-gray-300">From: {{ truncateAddr(group.fromAddress) }}</div>
                    <div class="text-gray-400">To: {{ truncateAddr(group.toAddress) }}</div>
                  </div>
                </td>
                <td>{{ group.accountName }}</td>
                <td class="text-xs">{{ formatTime(group.tradeTime) }}</td>
                <td class="text-apple-green">{{ group.inflow > 0 ? '+' + group.inflow : '' }}</td>
                <td class="text-apple-red">{{ group.outflow > 0 ? '-' + group.outflow : '' }}</td>
                <td class="text-gray-400">{{ group.fee || '-' }}</td>
                <td class="font-mono text-xs">
                  {{ truncateAddr(group.txHash) }}
                  <button class="ml-1 text-gray-500 hover:text-gray-300" @click.stop="copyToClipboard(group.txHash)">
                    <svg class="w-3.5 h-3.5 inline" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <rect x="9" y="9" width="13" height="13" rx="2" />
                      <path d="M5 15V5a2 2 0 012-2h10" />
                    </svg>
                  </button>
                </td>
                <td>{{ group.chainName }}</td>
                <td>
                  <div class="flex items-center gap-1">
                    <span class="text-xs text-gray-400 truncate max-w-[100px]">{{ group.description || '-' }}</span>
                    <button class="text-xs text-apple-accent hover:underline" @click.stop="openEditDesc(group)">edit</button>
                  </div>
                </td>
              </tr>

              <!-- Detail Rows -->
              <template v-if="expandedHashes.has(group.txHash)">
                <tr v-if="detailsLoading.has(group.txHash)">
                  <td colspan="11" class="text-center py-4 text-gray-500 bg-apple-bg/30">Loading details...</td>
                </tr>
                <tr
                  v-for="detail in detailsMap.get(group.txHash) ?? []"
                  :key="detail.id"
                  class="bg-apple-bg/30"
                >
                  <td></td>
                  <td>
                    <AppTag :color="detail.direction === 'Receive' ? 'green' : 'red'" >
                      {{ detail.category }}
                    </AppTag>
                  </td>
                  <td>
                    <span :class="detail.direction === 'Receive' ? 'text-apple-green' : 'text-apple-red'">
                      {{ detail.direction === 'Receive' ? '+' : '-' }}{{ detail.amount }} {{ detail.currency }}
                    </span>
                  </td>
                  <td class="font-mono text-xs text-gray-400">{{ truncateAddr(detail.fromAddress) }}</td>
                  <td class="font-mono text-xs text-gray-400">{{ truncateAddr(detail.toAddress) }}</td>
                  <td colspan="6"></td>
                </tr>
              </template>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between mt-4 text-sm text-gray-400">
        <span>Total: {{ totalRow }} groups</span>
        <div class="flex items-center gap-2">
          <AppButton variant="ghost" size="sm" :disabled="pageNumber <= 1" @click="pageNumber--; loadGroups()">Previous</AppButton>
          <span>Page {{ pageNumber }}</span>
          <AppButton variant="ghost" size="sm" :disabled="groups.length < pageSize" @click="pageNumber++; loadGroups()">Next</AppButton>
        </div>
      </div>
    </div>

    <!-- Edit Description Modal -->
    <AppModal :visible="showDescModal" title="Edit Description" size="sm" @close="showDescModal = false">
      <AppInput v-model="editDescValue" label="Description" placeholder="Enter description" />
      <template #footer>
        <AppButton variant="secondary" @click="showDescModal = false">Cancel</AppButton>
        <AppButton :loading="saving" @click="saveDesc">Save</AppButton>
      </template>
    </AppModal>

    <!-- Edit Business Type Modal -->
    <AppModal :visible="showBizModal" title="Edit Business Type" size="sm" @close="showBizModal = false">
      <AppInput v-model="editBizValue" label="Business Type" placeholder="e.g. InternalTransfer, Staking" />
      <template #footer>
        <AppButton variant="secondary" @click="showBizModal = false">Cancel</AppButton>
        <AppButton :loading="saving" @click="saveBizType">Save</AppButton>
      </template>
    </AppModal>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AppButton from '@/components/AppButton.vue'
import AppInput from '@/components/AppInput.vue'
import AppSelect from '@/components/AppSelect.vue'
import AppModal from '@/components/AppModal.vue'
import AppTag from '@/components/AppTag.vue'
import { cryptoTxApi } from '@/api'
import type { TxGroup, TxDetail } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()

const groups = ref<TxGroup[]>([])
const totalRow = ref(0)
const pageNumber = ref(1)
const pageSize = 20
const loading = ref(false)
const saving = ref(false)

const filters = reactive({
  txHash: '',
  direction: '',
  category: '',
  chainName: '',
  startTime: '',
  endTime: '',
})

const expandedHashes = ref(new Set<string>())
const detailsMap = ref(new Map<string, TxDetail[]>())
const detailsLoading = ref(new Set<string>())

const showDescModal = ref(false)
const showBizModal = ref(false)
const editTxHash = ref('')
const editDescValue = ref('')
const editBizValue = ref('')

const directionOptions = [
  { value: '', label: 'All' },
  { value: 'Send', label: 'Send' },
  { value: 'Receive', label: 'Receive' },
]

const categoryOptions = [
  { value: '', label: 'All' },
  { value: 'Transfer', label: 'Transfer' },
  { value: 'Swap', label: 'Swap' },
  { value: 'Mixed', label: 'Mixed' },
]

onMounted(() => {
  const mainAccountId = route.query.mainAccountId
  if (mainAccountId) {
    filters.txHash = '' // clear so it uses mainAccountId filter
  }
  loadGroups()
})

async function loadGroups() {
  loading.value = true
  try {
    const params: Record<string, string | number | undefined> = {
      pageNumber: pageNumber.value,
      pageSize,
      ...filters,
    }
    const mainAccountId = route.query.mainAccountId as string | undefined
    if (mainAccountId) {
      params.mainAccountId = Number(mainAccountId)
    }
    const data = await cryptoTxApi.groups(params)
    groups.value = data.records
    totalRow.value = data.totalRow
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  Object.assign(filters, { txHash: '', direction: '', category: '', chainName: '', startTime: '', endTime: '' })
  pageNumber.value = 1
  loadGroups()
}

async function toggleExpand(txHash: string) {
  if (expandedHashes.value.has(txHash)) {
    expandedHashes.value.delete(txHash)
    return
  }
  expandedHashes.value.add(txHash)
  if (!detailsMap.value.has(txHash)) {
    detailsLoading.value.add(txHash)
    try {
      const details = await cryptoTxApi.details(txHash)
      detailsMap.value.set(txHash, details)
    } catch (e: any) {
      toast.error(e.message)
    } finally {
      detailsLoading.value.delete(txHash)
    }
  }
}

function openEditDesc(group: TxGroup) {
  editTxHash.value = group.txHash
  editDescValue.value = group.description || ''
  showDescModal.value = true
}

function openEditBizType(group: TxGroup) {
  editTxHash.value = group.txHash
  editBizValue.value = group.businessType || ''
  showBizModal.value = true
}

async function saveDesc() {
  saving.value = true
  try {
    await cryptoTxApi.updateDescription(editTxHash.value, editDescValue.value)
    toast.success('Description updated')
    showDescModal.value = false
    await loadGroups()
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function saveBizType() {
  saving.value = true
  try {
    await cryptoTxApi.updateBusinessType(editTxHash.value, editBizValue.value)
    toast.success('Business type updated')
    showBizModal.value = false
    await loadGroups()
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

function categoryColor(cat: string): 'green' | 'red' | 'orange' | 'gray' {
  if (cat === 'Send') return 'red'
  if (cat === 'Receive') return 'green'
  if (cat === 'Mixed') return 'orange'
  return 'gray'
}

function formatTime(time: string | null): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function truncateAddr(addr: string | null): string {
  if (!addr) return '-'
  if (addr.length <= 16) return addr
  return `${addr.slice(0, 8)}...${addr.slice(-6)}`
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
  toast.info('Copied to clipboard')
}
</script>
