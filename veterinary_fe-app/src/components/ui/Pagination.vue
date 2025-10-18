<template>
  <div class="flex items-center justify-between">
    <!-- Mobile simple pager -->
    <div class="sm:hidden flex items-center justify-between w-full">
      <button
        class="inline-flex items-center justify-center px-3 py-2 text-sm rounded-md border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
        :disabled="page <= 1"
        @click="$emit('update:page', page - 1)"
      >Trước</button>
      <div class="text-sm text-gray-600">Trang {{ page }} / {{ totalPages }}</div>
      <button
        class="inline-flex items-center justify-center px-3 py-2 text-sm rounded-md border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
        :disabled="page >= totalPages"
        @click="$emit('update:page', page + 1)"
      >Sau</button>
    </div>

    <!-- Desktop pager -->
    <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
      <div class="text-sm text-gray-600">
        Hiển thị
        <span class="font-medium">{{ startItem }}</span>
        -
        <span class="font-medium">{{ endItem }}</span>
        trong tổng
        <span class="font-medium">{{ total }}</span>
        bản ghi
      </div>

      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2">
          <label class="text-sm text-gray-700">Mỗi trang</label>
          <select
            :value="pageSize"
            @change="$emit('update:pageSize', parseInt($event.target.value))"
            class="block w-auto py-1 pl-2 pr-8 text-sm border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
          >
            <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
          </select>
        </div>

        <div class="inline-flex items-center">
          <button
            class="inline-flex items-center justify-center px-2 py-2 text-sm rounded-l-md border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
            :disabled="page <= 1"
            @click="$emit('update:page', page - 1)"
            aria-label="Trang trước"
          >
            <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 010 1.06L9.06 10l3.73 3.71a.75.75 0 11-1.06 1.06l-4.24-4.24a.75.75 0 010-1.06l4.24-4.24a.75.75 0 011.06 0z" clip-rule="evenodd"/></svg>
          </button>
          <template v-for="p in visiblePages" :key="p">
            <button
              v-if="p !== '...'"
              class="px-3 py-2 text-sm border-t border-b border-gray-300"
              :class="p === page ? 'bg-purple-50 text-purple-700 border-x border-gray-300' : 'bg-white text-gray-700 hover:bg-gray-50 border-x border-gray-300'"
              @click="$emit('update:page', p)"
            >{{ p }}</button>
            <span v-else class="px-3 py-2 text-sm bg-white border-t border-b border-gray-300 text-gray-500">...</span>
          </template>
          <button
            class="inline-flex items-center justify-center px-2 py-2 text-sm rounded-r-md border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
            :disabled="page >= totalPages"
            @click="$emit('update:page', page + 1)"
            aria-label="Trang sau"
          >
            <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 001.06 0L12 11.06a.75.75 0 000-1.06L8.27 6.29A.75.75 0 107.21 7.35L10.06 10 7.21 12.65a.75.75 0 000 1.12z" clip-rule="evenodd"/></svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true },
  pageSizeOptions: { type: Array, default: () => [10, 20, 50] }
})

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const startItem = computed(() => {
  if (props.total === 0) return 0
  return (props.page - 1) * props.pageSize + 1
})

const endItem = computed(() => {
  const end = props.page * props.pageSize
  return Math.min(end, props.total)
})

const visiblePages = computed(() => {
  const delta = 2
  const range = []
  const rangeWithDots = []
  for (let i = Math.max(2, props.page - delta); i <= Math.min(totalPages.value - 1, props.page + delta); i++) {
    range.push(i)
  }
  if (props.page - delta > 2) {
    rangeWithDots.push(1, '...')
  } else {
    rangeWithDots.push(1)
  }
  rangeWithDots.push(...range)
  if (props.page + delta < totalPages.value - 1) {
    rangeWithDots.push('...', totalPages.value)
  } else if (totalPages.value > 1) {
    rangeWithDots.push(totalPages.value)
  }
  return rangeWithDots.filter((item, index, arr) => arr.indexOf(item) === index)
})
</script>

<style scoped>
</style>

