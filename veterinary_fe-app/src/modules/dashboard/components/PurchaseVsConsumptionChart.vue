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
  purchase: {
    type: Array,
    default: () => [],
  },
  consumption: {
    type: Array,
    default: () => [],
  },
});

const chartData = computed(() => ({
  labels: props.months,
  datasets: [
    {
      label: 'Purchase',
      data: props.purchase,
    },
    {
      label: 'Consumption',
      data: props.consumption,
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
    y: {
      beginAtZero: true,
    },
  },
}));
</script>
