<template>
  <div class="flex h-screen overflow-hidden">
    <!-- Sidebar -->
    <aside class="w-[240px] flex-shrink-0 bg-[#1c1c1e] flex flex-col">
      <!-- Logo -->
      <div class="px-6 py-5">
        <h1 class="text-xl font-semibold tracking-wide text-white">Fintax</h1>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-3 py-2 space-y-6">
        <!-- Accounts Group -->
        <div>
          <p class="px-3 mb-2 text-xs font-medium uppercase tracking-wider text-gray-500">
            Accounts
          </p>
          <ul class="space-y-1">
            <li>
              <RouterLink
                to="/accounts"
                class="flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors duration-150"
                :class="isActive('/accounts')
                  ? 'bg-white/10 text-white'
                  : 'text-gray-400 hover:bg-white/5 hover:text-gray-200'"
              >
                <svg class="w-5 h-5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z" />
                </svg>
                Account
              </RouterLink>
            </li>
          </ul>
        </div>

        <!-- Crypto Group -->
        <div>
          <p class="px-3 mb-2 text-xs font-medium uppercase tracking-wider text-gray-500">
            Crypto
          </p>
          <ul class="space-y-1">
            <li>
              <RouterLink
                to="/crypto/transactions"
                class="flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors duration-150"
                :class="isActive('/crypto/transactions')
                  ? 'bg-white/10 text-white'
                  : 'text-gray-400 hover:bg-white/5 hover:text-gray-200'"
              >
                <svg class="w-5 h-5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M7.5 21L3 16.5m0 0L7.5 12M3 16.5h13.5m0-13.5L21 7.5m0 0L16.5 12M21 7.5H7.5" />
                </svg>
                Transactions
              </RouterLink>
            </li>
          </ul>
        </div>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 bg-[#2c2c2e] overflow-auto">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute, RouterLink } from 'vue-router'

const route = useRoute()

function isActive(path: string): boolean {
  return route.path === path
}
</script>
