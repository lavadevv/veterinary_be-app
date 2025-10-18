<template>
  <div class="min-h-full space-y-6">
    <!-- Header and filters -->
    <div class="bg-white border border-gray-200 rounded-2xl shadow-sm p-4">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <h1 class="text-xl font-semibold text-gray-900">Quản lý Kho (Demo)</h1>
        <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
          <div class="relative">
            <input v-model="query" type="text" placeholder="Tìm theo mã/tên kho" class="w-full sm:w-64 pl-10 pr-3 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent" />
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 10a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <select v-model="status" class="pl-3 pr-8 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Đang hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
          <button class="inline-flex items-center justify-center px-4 py-2 rounded-lg bg-purple-600 text-white hover:bg-purple-700">Thêm kho</button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white border border-gray-200 rounded-2xl shadow-sm p-3">
      <div class="overflow-hidden rounded-xl">
        <DataTable
          :columns="columns"
          :rows="pagedRows"
          row-key="id"
          :actions="rowActions"
          :loading="loading"
        />
      </div>

      <!-- Pagination -->
      <div class="mt-4">
        <Pagination
          :page="page"
          :page-size="pageSize"
          :total="filteredRows.length"
          :page-size-options="[5,10,20]"
          @update:page="page = $event"
          @update:pageSize="(v) => { pageSize = v; page = 1 }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, h } from 'vue'
import DataTable from '@/components/ui/DataTable.vue'
import Pagination from '@/components/ui/Pagination.vue'

const loading = ref(false)
const query = ref('')
const status = ref('')
const page = ref(1)
const pageSize = ref(10)

const columns = [
  { key: 'code', label: 'Mã kho', priority: 1 },
  { key: 'name', label: 'Tên kho', priority: 1 },
  { key: 'type', label: 'Loại', priority: 2 },
  { key: 'statusLabel', label: 'Trạng thái', priority: 2 },
  { key: 'manager', label: 'Quản lý', priority: 4 },
  { key: 'updatedAt', label: 'Cập nhật', priority: 5, align: 'right' }
]

const allRows = ref([
  { id: 1, code: 'WH-001', name: 'Kho Tổng A', type: 'Tổng', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Nguyễn Văn A', updatedAt: '2025-01-05 10:35' },
  { id: 2, code: 'WH-002', name: 'Kho Vật tư B', type: 'Vật tư', status: 'inactive', statusLabel: 'Ngừng hoạt động', manager: 'Trần Thị B', updatedAt: '2025-01-03 14:10' },
  { id: 3, code: 'WH-003', name: 'Kho Dược phẩm C', type: 'Dược', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Lê Văn C', updatedAt: '2024-12-29 08:12' },
  { id: 4, code: 'WH-004', name: 'Kho Sản xuất D', type: 'Sản xuất', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Phạm Thị D', updatedAt: '2024-12-20 09:45' },
  { id: 5, code: 'WH-005', name: 'Kho Thành phẩm E', type: 'Thành phẩm', status: 'inactive', statusLabel: 'Ngừng hoạt động', manager: 'Đỗ Văn E', updatedAt: '2024-12-18 16:22' },
  { id: 6, code: 'WH-006', name: 'Kho NVL F', type: 'Vật tư', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Bùi Thị F', updatedAt: '2024-12-16 13:08' },
  { id: 7, code: 'WH-007', name: 'Kho NVL G', type: 'Vật tư', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Đinh Văn G', updatedAt: '2024-12-12 07:50' },
  { id: 8, code: 'WH-008', name: 'Kho Đóng gói H', type: 'Đóng gói', status: 'inactive', statusLabel: 'Ngừng hoạt động', manager: 'Vũ Thị H', updatedAt: '2024-12-10 19:25' },
  { id: 9, code: 'WH-009', name: 'Kho NVL I', type: 'Vật tư', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Ngô Văn I', updatedAt: '2024-12-08 12:55' },
  { id: 10, code: 'WH-010', name: 'Kho Phụ liệu J', type: 'Phụ liệu', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Tạ Thị J', updatedAt: '2024-12-06 11:11' },
  { id: 11, code: 'WH-011', name: 'Kho Tổng K', type: 'Tổng', status: 'active', statusLabel: 'Đang hoạt động', manager: 'Hoàng Văn K', updatedAt: '2024-12-03 10:10' },
])

const filteredRows = computed(() => {
  const q = query.value.trim().toLowerCase()
  return allRows.value.filter(r => {
    const matchQ = !q || r.code.toLowerCase().includes(q) || r.name.toLowerCase().includes(q)
    const matchS = !status.value || r.status === status.value
    return matchQ && matchS
  })
})

const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

function rowActions(row) {
  return [
    h('button', {
      class: 'inline-flex items-center justify-center h-10 min-w-10 px-3 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50',
      title: 'Xem chi tiết',
      onClick: () => alert(`Xem: ${row.code}`)
    }, 'Xem'),
    h('button', {
      class: 'inline-flex items-center justify-center h-10 min-w-10 px-3 rounded-md bg-purple-600 text-white hover:bg-purple-700',
      title: 'Sửa',
      onClick: () => alert(`Sửa: ${row.code}`)
    }, 'Sửa')
  ]
}
</script>

<style scoped>
</style>

