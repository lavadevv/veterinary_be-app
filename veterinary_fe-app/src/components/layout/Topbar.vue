<template>
  <header class="sticky top-0 z-30 bg-white border-b border-gray-200">
    <div class="h-14 px-4 md:px-6 lg:px-8 flex items-center gap-3">
      <!-- Left: Hamburger (hidden on lg when pinned) -->
      <button
        class="inline-flex items-center justify-center h-10 w-10 rounded-md hover:bg-gray-100 text-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
        :class="pinned ? 'lg:hidden' : ''"
        aria-label="Open sidebar"
        aria-controls="sidebar"
        :aria-expanded="false"
        @click="$emit('toggle-sidebar')"
      >
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
        </svg>
      </button>

      <!-- Breadcrumb -->
      <nav class="flex-1 min-w-0" aria-label="Breadcrumb">
        <ol class="flex items-center gap-2 text-sm text-gray-500">
          <li>
            <RouterLink to="/dashboard" class="hover:text-gray-700">Dashboard</RouterLink>
          </li>
          <li v-for="(bc, idx) in breadcrumbs" :key="idx" class="flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-4 h-4 text-gray-400">
              <path fill-rule="evenodd" d="M7.22 14.78a.75.75 0 01-1.06-1.06L10.44 9.44 6.16 5.16A.75.75 0 117.22 4.1l4.78 4.78a.75.75 0 010 1.06l-4.78 4.78z" clip-rule="evenodd" />
            </svg>
            <RouterLink :to="bc.to" class="truncate hover:text-gray-700">{{ bc.label }}</RouterLink>
          </li>
        </ol>
      </nav>

      <!-- Right: search and avatar menu -->
      <div class="flex items-center gap-3">
        <div class="hidden sm:flex items-center">
          <div class="relative">
            <input
              type="text"
              placeholder="TÃ¬m kiáº¿m"
              class="pl-10 pr-3 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent w-56"
            />
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 10a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>

        <Menu as="div" class="relative">
          <MenuButton class="inline-flex items-center gap-2 h-10 px-2 rounded-lg hover:bg-gray-100">
            <img src="https://avatars.githubusercontent.com/u/9919?s=40&v=4" alt="avatar" class="h-8 w-8 rounded-full" />
            <svg class="w-4 h-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 111.06 1.06l-4.24 4.24a.75.75 0 01-1.06 0L5.21 8.29a.75.75 0 01.02-1.08z" clip-rule="evenodd" />
            </svg>
          </MenuButton>
          <Transition
            enter-active-class="transition ease-out duration-100"
            enter-from-class="transform opacity-0 scale-95"
            enter-to-class="transform opacity-100 scale-100"
            leave-active-class="transition ease-in duration-75"
            leave-from-class="transform opacity-100 scale-100"
            leave-to-class="transform opacity-0 scale-95"
          >
            <MenuItems class="absolute right-0 mt-2 w-48 origin-top-right rounded-lg bg-white shadow-lg ring-1 ring-black/5 focus:outline-none py-1">
              <MenuItem v-slot="{ active }">
                <RouterLink to="/profile" :class="[active ? 'bg-gray-50' : '', 'block px-3 py-2 text-sm text-gray-700']">Há»“ sÆ¡</RouterLink>
              </MenuItem>
              <MenuItem v-slot="{ active }">
                <RouterLink to="/settings" :class="[active ? 'bg-gray-50' : '', 'block px-3 py-2 text-sm text-gray-700']">CÃ i Ä‘áº·t</RouterLink>
              </MenuItem>
              <div class="my-1 h-px bg-gray-100"></div>
              <MenuItem v-slot="{ active }">
                <button :class="[active ? 'bg-gray-50' : '', 'w-full text-left px-3 py-2 text-sm text-gray-700']">ÄÄƒng xuáº¥t</button>
              </MenuItem>
            </MenuItems>
          </Transition>
        </Menu>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/vue'
import { Bars3Icon, ChevronRightIcon, MagnifyingGlassIcon, ChevronDownIcon } from '@heroicons/vue/24/outline'

defineProps({
  pinned: { type: Boolean, default: false }
})

const route = useRoute()

const breadcrumbs = computed(() => {
  // Prefer meta.title; fallback to path segments
  const segments = route.path.split('/').filter(Boolean)
  const acc = []
  let current = ''
  for (const seg of segments) {
    current += `/${seg}`
    const matched = route.matched.find(m => m.path === current)
    const label = matched?.meta?.title || seg.replace(/-/g, ' ')
    acc.push({ label, to: current })
  }
  return acc
})
</script>

<style scoped>
</style>

