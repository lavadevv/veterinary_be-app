<template>
  <div class="h-[320px]">
    <DoughnutChart :chart-data="chartData" :options="options" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { DoughnutChart } from 'vue-chart-3';

const props = defineProps({
  labels: {
    type: Array,
    default: () => [],
  },
  data: {
    type: Array,
    default: () => [],
  },
});

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [
    {
      data: props.data,
    },
  ],
}));

const options = computed(() => ({
  plugins: {
    legend: {
      display: true,
    },
  },
}));
</script>
