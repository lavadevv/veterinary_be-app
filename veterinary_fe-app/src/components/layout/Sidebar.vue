<template>
  <!-- Desktop rail/expanded sidebar -->
  <aside
    class="relative hidden md:flex flex-col border-r border-gray-200 bg-white transition-[width] duration-200 ease-in-out"
    :class="[
      isExpanded ? 'lg:w-[260px] w-[72px]' : 'w-[72px]',
    ]"
    @mouseenter="isHovering = true"
    @mouseleave="isHovering = false"
  >
    <!-- Pin / logo area -->
    <div class="h-14 shrink-0 flex items-center justify-between px-2">
      <div class="flex items-center gap-2 overflow-hidden">
        <div class="h-8 w-8 rounded-full bg-emerald-600 flex items-center justify-center">
          <SparklesIcon class="w-5 h-5 text-white" />
        </div>
        <span v-if="isExpanded" class="ml-1 truncate font-semibold text-gray-800">The Pro</span>
      </div>
      <button
        v-if="isExpanded"
        @click="togglePin"
        class="ml-auto inline-flex items-center justify-center h-9 w-9 rounded-md hover:bg-gray-100"
        :title="pinned ? 'Bỏ ghim sidebar' : 'Ghim sidebar'"
      >
        <MapPinIcon class="w-5 h-5" :class="pinned ? 'text-emerald-600' : 'text-gray-600'" />
      </button>
    </div>

    <!-- Menu items -->
    <nav class="flex-1 overflow-y-auto px-2 py-2 space-y-1">
      <SidebarItem
        v-for="(item, idx) in menuItems"
        :key="idx"
        :item="item"
        :expanded="isExpanded"
        :pinned="pinned"
        @navigate="onNavigate"
      />
    </nav>
  </aside>

  <!-- Mobile/Tablet overlay drawer -->
  <div
    v-if="mobileOpen"
    class="fixed inset-0 z-50 md:z-40"
    role="dialog"
    aria-modal="true"
  >
    <div class="absolute inset-0 bg-black/40" @click="emit('close-mobile')"></div>
    <div
      class="absolute inset-y-0 left-0 w-72 max-w-full bg-white border-r border-gray-200 shadow-xl flex flex-col"
    >
      <div class="h-14 shrink-0 flex items-center justify-between px-3">
        <div class="flex items-center gap-2">
          <div class="h-8 w-8 rounded-full bg-emerald-600 flex items-center justify-center">
            <SparklesIcon class="w-5 h-5 text-white" />
          </div>
          <span class="font-semibold text-gray-800">Menu</span>
        </div>
        <button @click="emit('close-mobile')" class="inline-flex items-center justify-center h-9 w-9 rounded-md hover:bg-gray-100">
          <XMarkIcon class="w-5 h-5 text-gray-600" />
        </button>
      </div>
      <nav class="flex-1 overflow-y-auto px-2 py-2 space-y-1">
        <SidebarItem
          v-for="(item, idx) in menuItems"
          :key="'m-'+idx"
          :item="item"
          :expanded="true"
          :pinned="true"
          @navigate="onNavigate"
        />
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { XMarkIcon, MapPinIcon, SparklesIcon } from '@heroicons/vue/24/outline'
import SidebarItem from './SidebarItem.vue'
import { menuItems } from '@/config/menu.config'

const props = defineProps({
  pinned: { type: Boolean, default: false },
  mobileOpen: { type: Boolean, default: false }
})
const emit = defineEmits(['update:pinned', 'close-mobile'])

const route = useRoute()
const router = useRouter()

const isHovering = ref(false)
const isExpanded = computed(() => props.pinned || isHovering.value)

function togglePin() {
  emit('update:pinned', !props.pinned)
}

function onNavigate() {
  // Close mobile drawer after navigation
  if (props.mobileOpen) emit('close-mobile')
}
</script>

<style scoped>
</style>

