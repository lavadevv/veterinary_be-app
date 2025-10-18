<template>
  <div class="bg-white border border-gray-200 rounded-2xl shadow-sm p-3 md:p-4">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
      <div class="flex-1 min-w-0 flex items-center gap-2">
        <!-- Search -->
        <div class="relative w-full md:w-80">
          <input
            :value="localSearch"
            @input="onInput($event.target.value)"
            type="text"
            placeholder="Tìm kiếm vị trí kho"
            aria-label="Tìm kiếm"
            class="w-full pl-10 pr-9 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 10a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <button
            v-if="localSearch"
            @click="onResetSearch"
            class="absolute right-1.5 top-1/2 -translate-y-1/2 inline-flex items-center justify-center h-7 w-7 rounded-md text-gray-500 hover:bg-gray-100"
            aria-label="Xóa tìm kiếm"
          >
            <svg class="w-4 h-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Warehouse select -->
        <select
          :value="warehouseId"
          @change="$emit('update:warehouseId', $event.target.value)"
          class="py-2 px-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
        >
          <option value="">Tất cả kho</option>
          <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.warehouseName || w.name }}</option>
        </select>
      </div>

      <div class="flex items-center gap-2">
        <button @click="onResetAll" class="inline-flex items-center justify-center px-3 py-2 rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50">Reset</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  search: { type: String, default: '' },
  warehouses: { type: Array, default: () => [] },
  warehouseId: { type: [String, Number], default: '' }
})
const emit = defineEmits(['update:search', 'update:warehouseId', 'reset'])

const localSearch = ref(props.search)
watch(() => props.search, v => { if (v !== localSearch.value) localSearch.value = v })

let t = null
function onInput(val) {
  localSearch.value = val
  if (t) clearTimeout(t)
  t = setTimeout(() => emit('update:search', localSearch.value), 400)
}

function onResetSearch() {
  localSearch.value = ''
  emit('update:search', '')
}

function onResetAll() {
  localSearch.value = ''
  emit('update:search', '')
  emit('update:warehouseId', '')
  emit('reset')
}
</script>

<style scoped>
</style>

