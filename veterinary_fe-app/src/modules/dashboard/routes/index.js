// src/modules/dashboard/routes/index.js
export default [
  {
    path: '/dashboard',
    name: 'DashboardHome',
    component: () => import('../pages/DashboardHome.vue'),
  },
];
