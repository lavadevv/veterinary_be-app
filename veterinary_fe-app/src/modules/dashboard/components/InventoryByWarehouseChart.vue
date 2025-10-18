<template>
  <div class="h-[320px]">
    <BarChart :chart-data="chartData" :options="options" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { BarChart } from 'vue-chart-3';

const props = defineProps({
  labels: {
    type: Array,
    default: () => [],
  },
  datasets: {
    type: Array,
    default: () => [],
  },
});

const chartData = computed(() => ({
  labels: props.labels,
  datasets: props.datasets,
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
