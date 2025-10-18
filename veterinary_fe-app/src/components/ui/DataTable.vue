<template>
  <div class="w-full">
    <!-- Small (<= sm): Card list -->
    <div class="block sm:hidden space-y-3">
      <template v-if="loading && (!rows || rows.length === 0)">
        <div v-for="i in 3" :key="'s'+i" class="bg-white border border-gray-200 rounded-2xl shadow-sm p-4 animate-pulse">
          <div class="h-4 w-2/3 bg-gray-200 rounded"></div>
          <div class="mt-3 grid grid-cols-2 gap-x-4 gap-y-2">
            <div v-for="k in 4" :key="'sk'+k" class="h-3 bg-gray-100 rounded col-span-1"></div>
          </div>
        </div>
      </template>
      <template v-else>
        <div v-for="row in rows" :key="row[rowKey]" class="bg-white border border-gray-200 rounded-2xl shadow-sm p-4">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <div class="font-semibold text-gray-900 truncate" :title="titleCell(row)">{{ titleCell(row) }}</div>
              <div class="text-xs text-gray-500 mt-0.5">#{{ row[rowKey] }}</div>
            </div>
            <div class="shrink-0 flex items-center gap-2">
              <template v-if="actions">
                <component
                  v-for="(node, idx) in actions(row)"
                  :key="idx"
                  :is="node"
                />
              </template>
            </div>
          </div>
          <div class="mt-3 grid grid-cols-2 gap-x-4 gap-y-1">
            <template v-for="col in bodyColumns">
              <div :key="col.key + '-label'" class="text-xs font-medium text-gray-500">{{ col.label }}</div>
              <div :key="col.key + '-value'" class="text-sm text-gray-800 truncate" :title="formatCell(col, row)">{{ formatCell(col, row) }}</div>
            </template>
          </div>
        </div>
        <div v-if="!loading && (!rows || rows.length === 0)" class="text-center text-sm text-gray-500 py-4">Không có dữ liệu</div>
      </template>
    </div>

    <!-- Medium and Large: Table -->
    <div class="hidden sm:block overflow-x-auto">
      <table class="min-w-[900px] w-full table-fixed border-separate border-spacing-0" v-bind="$attrs">
        <thead class="bg-white sticky top-0 z-10">
          <tr>
            <th
              v-for="col in columns"
              :key="col.key"
              scope="col"
              class="px-3 py-3 text-left text-xs font-semibold text-gray-600 border-b border-gray-200 bg-white"
              :class="[alignClass(col.align), visibilityClass(col.priority)]"
              :style="col.width ? { width: col.width } : undefined"
            >
              <div class="truncate" :title="col.label">{{ col.label }}</div>
            </th>
            <th v-if="showActions" scope="col" class="px-3 py-3 text-right text-xs font-semibold text-gray-600 border-b border-gray-200 bg-white"
                :class="stickyActions ? 'sticky right-0 bg-white' : ''">
              Hành động
            </th>
          </tr>
        </thead>
        <tbody class="bg-white">
          <template v-if="loading && (!rows || rows.length === 0)">
            <tr v-for="i in 5" :key="'skl'+i" class="animate-pulse">
              <td v-for="col in columns" :key="col.key" class="px-3 py-3 border-b border-gray-100">
                <div class="h-3 bg-gray-100 rounded"></div>
              </td>
              <td v-if="showActions" class="px-3 py-3 border-b border-gray-100" :class="stickyActions ? 'sticky right-0 bg-white' : ''">
                <div class="h-3 bg-gray-100 rounded"></div>
              </td>
            </tr>
          </template>
          <template v-else>
            <tr v-for="row in rows" :key="row[rowKey]" class="even:bg-gray-50">
              <td
                v-for="col in columns"
                :key="col.key"
                class="px-3 py-3 text-sm text-gray-900 align-middle border-b border-gray-100"
                :class="[alignClass(col.align), visibilityClass(col.priority)]"
                :title="formatCell(col, row)"
              >
                <div class="truncate">{{ formatCell(col, row) }}</div>
              </td>
              <td v-if="showActions" class="px-3 py-3 text-sm text-right align-middle border-b border-gray-100"
                  :class="stickyActions ? 'sticky right-0 bg-white' : ''">
                <div class="inline-flex items-center gap-2">
                  <template v-if="actions">
                    <component
                      v-for="(node, idx) in actions(row)"
                      :key="idx"
                      :is="node"
                    />
                  </template>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
      <div v-if="!loading && (!rows || rows.length === 0)" class="text-center text-sm text-gray-500 py-6">Không có dữ liệu</div>
    </div>
  </div>
  
</template>

<script setup>
import { computed } from 'vue'

// Accepts attrs for accessibility like aria-label passed from parent
defineOptions({ inheritAttrs: false })

/**
 * Column = {
 *   key: string,
 *   label: string,
 *   priority?: 1|2|3|4|5,
 *   align?: 'left'|'center'|'right',
 *   width?: string,
 *   formatter?: (row:any)=>string
 * }
 */

const props = defineProps({
  columns: { type: Array, required: true },
  rows: { type: Array, required: true },
  rowKey: { type: String, required: true },
  actions: { type: Function },
  loading: { type: Boolean, default: false },
  stickyActions: { type: Boolean, default: true }
})

const showActions = computed(() => typeof props.actions === 'function')

const bodyColumns = computed(() => {
  if (!props.columns || props.columns.length === 0) return []
  // First column is card title on small
  return props.columns.slice(1)
})

function alignClass(align) {
  if (align === 'center') return 'text-center'
  if (align === 'right') return 'text-right'
  return 'text-left'
}

function visibilityClass(priority) {
  if (priority >= 4) return 'hidden lg:table-cell'
  if (priority >= 3) return 'hidden md:table-cell'
  return 'table-cell'
}

function formatCell(col, row) {
  if (typeof col.formatter === 'function') {
    try { return String(col.formatter(row) ?? '') } catch { return '' }
  }
  const v = row?.[col.key]
  return v == null ? '' : String(v)
}

function titleCell(row) {
  const first = props.columns?.[0]
  if (!first) return ''
  return formatCell(first, row)
}
</script>

<style scoped>
th.sticky, td.sticky { z-index: 5; }
</style>
