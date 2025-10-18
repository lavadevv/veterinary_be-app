<template>
  <div class="h-screen w-full bg-gray-50 flex overflow-hidden">
    <!-- Sidebar -->
    <Sidebar
      :pinned="pinned"
      :mobile-open="mobileOpen"
      @update:pinned="onUpdatePinned"
      @close-mobile="mobileOpen = false"
    />

    <!-- Main content -->
    <div class="flex-1 min-w-0 flex flex-col">
      <Topbar
        :pinned="pinned"
        @toggle-sidebar="toggleMobileSidebar"
      />

      <main class="flex-1 min-h-0 overflow-auto px-4 md:px-6 lg:px-8 py-4 md:py-6 lg:py-8">
        <router-view />
      </main>
    </div>
  </div>
  <!-- Mobile/Tablet overlay close by ESC -->
  <span v-if="mobileOpen" class="sr-only" @keydown.esc.window="mobileOpen = false"></span>
  <!-- Resize observer: keep rail experience smooth -->
  <span class="sr-only" aria-hidden="true"></span>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import Topbar from '@/components/layout/Topbar.vue'

const LS_KEY = 'ui.sidebarPinned'

const pinned = ref(false)
const mobileOpen = ref(false)

function loadPinned() {
  try {
    const raw = localStorage.getItem(LS_KEY)
    if (raw != null) pinned.value = JSON.parse(raw)
  } catch (e) {
    pinned.value = false
  }
}

function onUpdatePinned(val) {
  pinned.value = !!val
  try {
    localStorage.setItem(LS_KEY, JSON.stringify(pinned.value))
  } catch {}
}

function toggleMobileSidebar() {
  mobileOpen.value = !mobileOpen.value
}

onMounted(() => {
  loadPinned()
})
</script>

<style scoped>
</style>

