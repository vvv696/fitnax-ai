import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/accounts',
    },
    {
      path: '/accounts',
      name: 'accounts',
      component: () => import('../views/AccountPage.vue'),
    },
    {
      path: '/crypto/transactions',
      name: 'transactions',
      component: () => import('../views/TransactionsPage.vue'),
    },
    {
      path: '/assets/:mainAccountId',
      name: 'assets',
      component: () => import('../views/AssetsPage.vue'),
    },
  ],
})

export default router
