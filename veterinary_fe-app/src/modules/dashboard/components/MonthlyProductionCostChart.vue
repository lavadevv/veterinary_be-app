<template>
  <div class="h-[320px]">
    <BarChart :chart-data="chartData" :options="options" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { BarChart } from 'vue-chart-3';

const props = defineProps({
  months: {
    type: Array,
    default: () => [],
  },
  materials: {
    type: Array,
    default: () => [],
  },
  machines: {
    type: Array,
    default: () => [],
  },
  labor: {
    type: Array,
    default: () => [],
  },
});

const chartData = computed(() => ({
  labels: props.months,
  datasets: [
    {
      label: 'Materials',
      data: props.materials,
      stack: 'productionCost',
    },
    {
      label: 'Machines',
      data: props.machines,
      stack: 'productionCost',
    },
    {
      label: 'Labor',
      data: props.labor,
      stack: 'productionCost',
    },
  ],
}));

const options = computed(() => ({
  plugins: {
    legend: {
      display: true,
    },
  },
  scales: {
    x: {
      stacked: true,
    },
    y: {
      beginAtZero: true,
      stacked: true,
    },
  },
}));
</script>
