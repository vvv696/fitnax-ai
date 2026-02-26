<template>
  <MainLayout>
    <div class="p-6">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-semibold text-white">Accounts</h2>
        <AppButton @click="openCreateModal">+ New Account</AppButton>
      </div>

      <!-- Main Account Table -->
      <div class="bg-apple-card border border-apple-separator rounded-apple-lg overflow-hidden">
        <table class="apple-table">
          <thead>
            <tr>
              <th class="w-8"></th>
              <th>Account Name</th>
              <th>Type</th>
              <th>Source</th>
              <th>Address</th>
              <th>Market Value</th>
              <th>Status</th>
              <th>Organization</th>
              <th class="w-20">Operate</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="loading">
              <tr>
                <td colspan="9" class="text-center py-8 text-gray-500">Loading...</td>
              </tr>
            </template>
            <template v-else-if="accounts.length === 0">
              <tr>
                <td colspan="9" class="text-center py-8 text-gray-500">No accounts found</td>
              </tr>
            </template>
            <template v-for="account in accounts" :key="account.id">
              <!-- Main Account Row -->
              <tr class="cursor-pointer" @click="toggleExpand(account.id)">
                <td class="text-center">
                  <svg
                    class="w-4 h-4 inline-block transition-transform"
                    :class="{ 'rotate-90': expandedIds.has(account.id) }"
                    fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </td>
                <td class="font-medium text-white">{{ account.accountName }}</td>
                <td>{{ account.accountType }}</td>
                <td>{{ account.accountSource }}</td>
                <td class="font-mono text-xs">{{ truncateAddr(account.address) }}</td>
                <td>{{ account.marketValue ?? '-' }}</td>
                <td>
                  <AppTag :color="account.status === 'Active' ? 'green' : 'gray'">
                    {{ account.status }}
                  </AppTag>
                </td>
                <td>{{ account.organization }}</td>
                <td @click.stop>
                  <div class="flex items-center gap-1">
                    <button
                      class="px-2 py-1 text-xs rounded-md bg-apple-accent/10 text-apple-accent hover:bg-apple-accent/20"
                      @click="goTransactions(account.id)"
                    >
                      Transactions
                    </button>
                    <AppDropdown :items="menuItems" @select="(key: string) => handleMenu(key, account)" />
                  </div>
                </td>
              </tr>
              <!-- Sub Account Rows -->
              <template v-if="expandedIds.has(account.id)">
                <tr v-if="subAccountsLoading.has(account.id)">
                  <td colspan="9" class="text-center py-4 text-gray-500 bg-apple-bg/30">Loading sub accounts...</td>
                </tr>
                <tr
                  v-for="sub in subAccountsMap.get(account.id) ?? []"
                  :key="sub.id"
                  class="bg-apple-bg/30"
                >
                  <td></td>
                  <td class="pl-8 text-gray-300">{{ sub.subAccountName }}</td>
                  <td>{{ sub.category }}</td>
                  <td>{{ sub.accountSource }}</td>
                  <td>{{ sub.importType }}</td>
                  <td class="font-mono text-xs">{{ truncateAddr(sub.address) }}</td>
                  <td>
                    <AppTag :color="sub.status === 'Active' ? 'green' : 'gray'">
                      {{ sub.status }}
                    </AppTag>
                  </td>
                  <td></td>
                  <td @click.stop>
                    <div class="flex items-center gap-1">
                      <button
                        class="px-2 py-1 text-xs rounded-md bg-white/5 text-gray-300 hover:bg-white/10"
                        @click="openEditSubModal(sub)"
                      >
                        Edit
                      </button>
                      <button
                        class="px-2 py-1 text-xs rounded-md bg-apple-red/10 text-apple-red hover:bg-apple-red/20"
                        @click="deleteSub(sub)"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="!subAccountsLoading.has(account.id) && (subAccountsMap.get(account.id) ?? []).length === 0" class="bg-apple-bg/30">
                  <td colspan="9" class="text-center py-4 text-gray-500">No sub accounts</td>
                </tr>
              </template>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between mt-4 text-sm text-gray-400">
        <span>Total: {{ totalRow }} accounts</span>
        <div class="flex items-center gap-2">
          <AppButton variant="ghost" size="sm" :disabled="pageNumber <= 1" @click="pageNumber--; loadAccounts()">
            Previous
          </AppButton>
          <span>Page {{ pageNumber }}</span>
          <AppButton variant="ghost" size="sm" :disabled="accounts.length < pageSize" @click="pageNumber++; loadAccounts()">
            Next
          </AppButton>
        </div>
      </div>
    </div>

    <!-- Create/Edit Main Account Modal -->
    <AppModal :visible="showAccountModal" :title="editingAccount ? 'Edit Account' : 'New Account'" @close="showAccountModal = false">
      <div class="flex flex-col gap-4">
        <AppInput v-model="accountForm.accountName" label="Account Name" placeholder="Enter account name" />
        <AppSelect v-model="accountForm.accountType" label="Type" :options="typeOptions" placeholder="Select type" />
        <AppInput v-model="accountForm.accountSource" label="Source" placeholder="e.g. Binance, Coinbase" />
        <AppInput v-model="accountForm.address" label="Address" placeholder="Wallet address" />
        <AppSelect v-model="accountForm.status" label="Status" :options="statusOptions" />
        <AppInput v-model="accountForm.organization" label="Organization" placeholder="Organization name" />
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showAccountModal = false">Cancel</AppButton>
        <AppButton :loading="saving" @click="saveAccount">{{ editingAccount ? 'Update' : 'Create' }}</AppButton>
      </template>
    </AppModal>

    <!-- Edit Sub Account Modal -->
    <AppModal :visible="showSubModal" title="Edit Sub Account" @close="showSubModal = false">
      <div class="flex flex-col gap-4">
        <AppInput v-model="subForm.subAccountName" label="Sub Account Name" placeholder="Name" />
        <AppInput v-model="subForm.category" label="Category" placeholder="Category" />
        <AppInput v-model="subForm.accountSource" label="Source" placeholder="Source" />
        <AppInput v-model="subForm.importType" label="Import Type" placeholder="API / Excel" />
        <AppInput v-model="subForm.address" label="Address" placeholder="Address" />
        <AppSelect v-model="subForm.status" label="Status" :options="statusOptions" />
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showSubModal = false">Cancel</AppButton>
        <AppButton :loading="saving" @click="saveSubAccount">Update</AppButton>
      </template>
    </AppModal>

    <!-- Import File Modal -->
    <AppModal :visible="showImportModal" title="Import Excel File" size="sm" @close="showImportModal = false">
      <div class="flex flex-col gap-4">
        <p class="text-sm text-gray-400">Upload an .xlsx file to import transactions.</p>
        <input
          ref="fileInputRef"
          type="file"
          accept=".xlsx"
          class="block w-full text-sm text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0
                 file:text-sm file:bg-apple-accent file:text-white hover:file:bg-apple-accent-hover file:cursor-pointer"
          @change="onFileSelect"
        />
        <div v-if="importResult" class="p-3 rounded-apple bg-apple-bg-tertiary text-sm">
          <p class="text-apple-green">Success: {{ importResult.successCount }} rows</p>
          <p v-if="importResult.failCount > 0" class="text-apple-red mt-1">Failed: {{ importResult.failCount }} rows</p>
          <div v-for="fail in importResult.failRows" :key="fail.rowNo" class="text-xs text-gray-400 mt-1">
            Row {{ fail.rowNo }}: {{ fail.reason }}
          </div>
        </div>
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showImportModal = false">Close</AppButton>
        <AppButton :loading="importing" :disabled="!selectedFile" @click="doImport">Import</AppButton>
      </template>
    </AppModal>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AppButton from '@/components/AppButton.vue'
import AppInput from '@/components/AppInput.vue'
import AppSelect from '@/components/AppSelect.vue'
import AppModal from '@/components/AppModal.vue'
import AppDropdown from '@/components/AppDropdown.vue'
import AppTag from '@/components/AppTag.vue'
import { mainAccountApi, subAccountApi } from '@/api'
import type { MainAccount, SubAccount, ImportResult } from '@/api'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()

// State
const accounts = ref<MainAccount[]>([])
const totalRow = ref(0)
const pageNumber = ref(1)
const pageSize = 20
const loading = ref(false)
const saving = ref(false)

// Expand
const expandedIds = ref(new Set<number>())
const subAccountsMap = ref(new Map<number, SubAccount[]>())
const subAccountsLoading = ref(new Set<number>())

// Modals
const showAccountModal = ref(false)
const editingAccount = ref<MainAccount | null>(null)
const accountForm = reactive({
  accountName: '',
  accountType: '',
  accountSource: '',
  address: '',
  status: 'Active',
  organization: '',
})

const showSubModal = ref(false)
const editingSub = ref<SubAccount | null>(null)
const subForm = reactive({
  subAccountName: '',
  category: '',
  accountSource: '',
  importType: '',
  address: '',
  status: 'Active',
})

const showImportModal = ref(false)
const importAccountId = ref<number>(0)
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importResult = ref<ImportResult | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

// Options
const typeOptions = [
  { value: 'Exchange', label: 'Exchange' },
  { value: 'Wallet', label: 'Wallet' },
  { value: 'DeFi', label: 'DeFi' },
  { value: 'Custodial', label: 'Custodial' },
]

const statusOptions = [
  { value: 'Active', label: 'Active' },
  { value: 'Inactive', label: 'Inactive' },
]

const menuItems = [
  { key: 'import', label: 'Import File' },
  { key: 'assets', label: 'View Assets' },
  { key: 'export', label: 'Export Assets' },
  { key: 'delete', label: 'Delete', danger: true },
]

onMounted(() => loadAccounts())

async function loadAccounts() {
  loading.value = true
  try {
    const data = await mainAccountApi.list(pageNumber.value, pageSize)
    accounts.value = data.records
    totalRow.value = data.totalRow
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function toggleExpand(id: number) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
    return
  }
  expandedIds.value.add(id)
  if (!subAccountsMap.value.has(id)) {
    subAccountsLoading.value.add(id)
    try {
      const subs = await subAccountApi.listByMainAccount(id)
      subAccountsMap.value.set(id, subs)
    } catch (e: any) {
      toast.error(e.message)
    } finally {
      subAccountsLoading.value.delete(id)
    }
  }
}

function openCreateModal() {
  editingAccount.value = null
  Object.assign(accountForm, {
    accountName: '', accountType: '', accountSource: '',
    address: '', status: 'Active', organization: '',
  })
  showAccountModal.value = true
}

async function saveAccount() {
  saving.value = true
  try {
    if (editingAccount.value) {
      await mainAccountApi.update(editingAccount.value.id, accountForm)
      toast.success('Account updated')
    } else {
      await mainAccountApi.create(accountForm)
      toast.success('Account created')
    }
    showAccountModal.value = false
    await loadAccounts()
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

function openEditSubModal(sub: SubAccount) {
  editingSub.value = sub
  Object.assign(subForm, {
    subAccountName: sub.subAccountName,
    category: sub.category || '',
    accountSource: sub.accountSource || '',
    importType: sub.importType || '',
    address: sub.address || '',
    status: sub.status,
  })
  showSubModal.value = true
}

async function saveSubAccount() {
  if (!editingSub.value) return
  saving.value = true
  try {
    await subAccountApi.update(editingSub.value.id, subForm)
    toast.success('Sub account updated')
    showSubModal.value = false
    subAccountsMap.value.delete(editingSub.value.mainAccountId)
    const subs = await subAccountApi.listByMainAccount(editingSub.value.mainAccountId)
    subAccountsMap.value.set(editingSub.value.mainAccountId, subs)
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

async function deleteSub(sub: SubAccount) {
  if (!confirm(`Delete sub account "${sub.subAccountName}"?`)) return
  try {
    await subAccountApi.delete(sub.id)
    toast.success('Sub account deleted')
    subAccountsMap.value.delete(sub.mainAccountId)
    const subs = await subAccountApi.listByMainAccount(sub.mainAccountId)
    subAccountsMap.value.set(sub.mainAccountId, subs)
  } catch (e: any) {
    toast.error(e.message)
  }
}

async function handleMenu(key: string, account: MainAccount) {
  switch (key) {
    case 'delete':
      if (!confirm(`Delete account "${account.accountName}"?`)) return
      try {
        await mainAccountApi.delete(account.id)
        toast.success('Account deleted')
        await loadAccounts()
      } catch (e: any) {
        toast.error(e.message)
      }
      break
    case 'import':
      importAccountId.value = account.id
      selectedFile.value = null
      importResult.value = null
      showImportModal.value = true
      break
    case 'assets':
      router.push({ name: 'assets', params: { mainAccountId: account.id } })
      break
    case 'export':
      window.open(`/api/reconcile/export?mainAccountId=${account.id}`, '_blank')
      break
  }
}

function goTransactions(mainAccountId: number) {
  router.push({ name: 'transactions', query: { mainAccountId: String(mainAccountId) } })
}

function onFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
}

async function doImport() {
  if (!selectedFile.value) return
  importing.value = true
  importResult.value = null
  try {
    const result = await mainAccountApi.importFile(importAccountId.value, selectedFile.value)
    importResult.value = result
    toast.success(`Imported ${result.successCount} rows`)
    subAccountsMap.value.delete(importAccountId.value)
    if (expandedIds.value.has(importAccountId.value)) {
      const subs = await subAccountApi.listByMainAccount(importAccountId.value)
      subAccountsMap.value.set(importAccountId.value, subs)
    }
  } catch (e: any) {
    toast.error(e.message)
  } finally {
    importing.value = false
  }
}

function truncateAddr(addr: string | null): string {
  if (!addr) return '-'
  if (addr.length <= 16) return addr
  return `${addr.slice(0, 8)}...${addr.slice(-6)}`
}
</script>
