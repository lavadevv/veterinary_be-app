<template>
  <div class="h-[320px]">
    <LineChart :chart-data="chartData" :options="options" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { LineChart } from 'vue-chart-3';

const props = defineProps({
  months: {
    type: Array,
    default: () => [],
  },
  done: {
    type: Array,
    default: () => [],
  },
});

const chartData = computed(() => ({
  labels: props.months,
  datasets: [
    {
      label: 'Done',
      data: props.done,
      fill: false,
      tension: 0.3,
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
