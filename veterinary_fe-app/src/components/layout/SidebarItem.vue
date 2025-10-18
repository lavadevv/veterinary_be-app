<template>
  <div>
    <!-- Item without children -->
    <component
      v-if="!hasChildren"
      :is="linkComponent"
      :to="item.route"
      class="group flex items-center gap-3 rounded-lg px-2 py-2 text-sm transition-colors"
      :class="[
        isActive ? 'bg-emerald-50 text-emerald-700' : 'text-gray-700 hover:bg-gray-50',
        expanded ? 'justify-start' : 'justify-center'
      ]"
      :title="!expanded ? item.title : undefined"
      @click="emit('navigate')"
    >
      <component :is="item.icon" class="h-5 w-5 shrink-0" />
      <span v-if="expanded" class="truncate">{{ item.title }}</span>
      <span v-if="isActive" class="ml-auto inline-block w-1.5 h-1.5 rounded-full bg-emerald-600"/>
    </component>

    <!-- Item with children -->
    <Disclosure v-else v-slot="{ open }" :default-open="isParentActive">
      <DisclosureButton
        class="w-full flex items-center gap-3 rounded-lg px-2 py-2 text-sm transition-colors"
        :class="[
          isParentActive ? 'bg-emerald-50 text-emerald-700' : 'text-gray-700 hover:bg-gray-50',
          expanded ? 'justify-start' : 'justify-center'
        ]"
        :title="!expanded ? item.title : undefined"
      >
        <component :is="item.icon" class="h-5 w-5 shrink-0" />
        <span v-if="expanded" class="truncate">{{ item.title }}</span>
        <ChevronDownIcon v-if="expanded" class="ml-auto h-4 w-4 text-gray-500 transition-transform" :class="open ? 'rotate-180' : ''" />
      </DisclosureButton>
      <DisclosurePanel v-if="expanded" class="mt-1 space-y-1 pl-9">
        <component
          v-for="(child, idx) in item.children"
          :key="idx"
          :is="linkComponent"
          :to="child.route"
          class="flex items-center gap-2 rounded-md px-2 py-1.5 text-sm"
          :class="childActive(child) ? 'text-emerald-700 bg-emerald-50' : 'text-gray-600 hover:bg-gray-50'"
          @click="emit('navigate')"
        >
          <span class="truncate">{{ child.title }}</span>
        </component>
      </DisclosurePanel>
    </Disclosure>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { Disclosure, DisclosureButton, DisclosurePanel } from '@headlessui/vue'
import { ChevronDownIcon } from '@heroicons/vue/24/outline'

const props = defineProps({
  item: { type: Object, required: true },
  expanded: { type: Boolean, default: false },
  pinned: { type: Boolean, default: false }
})
const emit = defineEmits(['navigate'])

const route = useRoute()
const linkComponent = RouterLink

const hasChildren = computed(() => Array.isArray(props.item.children) && props.item.children.length > 0)
const isActive = computed(() => route.path.startsWith(props.item.route))
const isParentActive = computed(() => isActive.value)

function childActive(child) {
  return route.path.startsWith(child.route)
}
</script>

<style scoped>
</style>
