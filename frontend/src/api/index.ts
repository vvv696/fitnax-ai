const BASE = '/api'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

interface PageResult<T> {
  records: T[]
  totalRow: number
  pageNumber: number
  pageSize: number
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const json: ApiResult<T> = await res.json()
  if (json.code !== 200) {
    throw new Error(json.message || 'Request failed')
  }
  return json.data
}

// ========== Main Account ==========

export interface MainAccount {
  id: number
  accountName: string
  accountType: string
  accountSource: string
  address: string
  marketValue: number | null
  status: string
  organization: string
  createdAt: string
  updatedAt: string
}

export interface SubAccount {
  id: number
  mainAccountId: number
  subAccountName: string
  category: string
  accountSource: string
  importType: string
  importFileName: string | null
  importBatchId: string | null
  address: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface ImportResult {
  successCount: number
  failCount: number
  failRows: { rowNo: number; reason: string }[]
}

export const mainAccountApi = {
  list(pageNumber = 1, pageSize = 20) {
    return request<PageResult<MainAccount>>(`/main-accounts?pageNumber=${pageNumber}&pageSize=${pageSize}`)
  },
  getById(id: number) {
    return request<MainAccount>(`/main-accounts/${id}`)
  },
  create(data: Partial<MainAccount>) {
    return request<MainAccount>('/main-accounts', { method: 'POST', body: JSON.stringify(data) })
  },
  update(id: number, data: Partial<MainAccount>) {
    return request<MainAccount>(`/main-accounts/${id}`, { method: 'PUT', body: JSON.stringify(data) })
  },
  delete(id: number) {
    return request<void>(`/main-accounts/${id}`, { method: 'DELETE' })
  },
  importFile(id: number, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<ImportResult>(`/main-accounts/${id}/import`, {
      method: 'POST',
      body: formData,
      headers: {}, // let browser set Content-Type for multipart
    } as RequestInit)
  },
}

// ========== Sub Account ==========

export const subAccountApi = {
  listByMainAccount(mainAccountId: number) {
    return request<SubAccount[]>(`/sub-accounts/by-main-account/${mainAccountId}`)
  },
  create(data: Partial<SubAccount>) {
    return request<SubAccount>('/sub-accounts', { method: 'POST', body: JSON.stringify(data) })
  },
  update(id: number, data: Partial<SubAccount>) {
    return request<SubAccount>(`/sub-accounts/${id}`, { method: 'PUT', body: JSON.stringify(data) })
  },
  delete(id: number) {
    return request<void>(`/sub-accounts/${id}`, { method: 'DELETE' })
  },
}

// ========== Crypto Tx ==========

export interface TxGroup {
  txHash: string
  category: string
  businessType: string
  fromAddress: string
  toAddress: string
  accountName: string
  tradeTime: string
  inflow: number
  outflow: number
  fee: number
  chainName: string
  description: string
  detailCount: number
}

export interface TxDetail {
  id: number
  txHash: string
  direction: string
  category: string
  businessType: string
  currency: string
  amount: number
  fromAddress: string
  toAddress: string
  fee: number
  feeCurrency: string
  chainName: string
  tradeTime: string
  description: string
  mainAccountId: number
  subAccountId: number
}

export const cryptoTxApi = {
  groups(params: Record<string, string | number | undefined>) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
      .join('&')
    return request<PageResult<TxGroup>>(`/crypto-tx/groups?${qs}`)
  },
  details(txHash: string, mainAccountId?: number, subAccountId?: number) {
    let url = `/crypto-tx/details?txHash=${encodeURIComponent(txHash)}`
    if (mainAccountId) url += `&mainAccountId=${mainAccountId}`
    if (subAccountId) url += `&subAccountId=${subAccountId}`
    return request<TxDetail[]>(url)
  },
  updateDescription(txHash: string, description: string) {
    return request<number>('/crypto-tx/description', {
      method: 'PUT',
      body: JSON.stringify({ txHash, description }),
    })
  },
  updateBusinessType(txHash: string, businessType: string) {
    return request<number>('/crypto-tx/business-type', {
      method: 'PUT',
      body: JSON.stringify({ txHash, businessType }),
    })
  },
}

// ========== Reconcile ==========

export interface ReconcileSnapshot {
  id: number
  mainAccountId: number
  subAccountId: number
  subAccountName: string
  currency: string
  systemTotal: number
  onchainBalance: number
  diff: number
  diffAbs: number
  diffStatus: string
  snapshotTime: string
  createdAt: string
}

export const reconcileApi = {
  generate(mainAccountId: number, subAccountId?: number) {
    return request<ReconcileSnapshot[]>('/reconcile/generate', {
      method: 'POST',
      body: JSON.stringify({ mainAccountId, subAccountId }),
    })
  },
  list(params: Record<string, string | number | undefined>) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
      .join('&')
    return request<PageResult<ReconcileSnapshot>>(`/reconcile/list?${qs}`)
  },
  exportUrl(params: Record<string, string | number | undefined>) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
      .join('&')
    return `${BASE}/reconcile/export?${qs}`
  },
}

export type { PageResult }
