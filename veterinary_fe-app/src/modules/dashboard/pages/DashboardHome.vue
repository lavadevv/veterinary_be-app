<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <h1 class="text-2xl font-semibold text-gray-900">B&aacute;o c&aacute;o t&#7893;ng quan</h1>
      <div class="flex items-center gap-3">
        <label class="text-sm font-medium text-gray-600" for="dashboard-year">N&#259;m</label>
        <input
          id="dashboard-year"
          v-model.number="year"
          class="w-28 rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          type="number"
          min="2000"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">T&#7891;n kho theo nh&oacute;m v&#7853;t t&#432;</h2>
        <InventoryByCategoryChart :labels="invByCat.labels" :data="invByCat.data" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">T&#7891;n kho theo kho</h2>
        <InventoryByWarehouseChart :labels="invByWh.labels" :datasets="invByWh.datasets" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Aging inventory</h2>
        <AgingInventoryChart :labels="agingInv.labels" :data="agingInv.data" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Stock alerts</h2>
        <StockAlertsPie :labels="stockAlerts.labels" :data="stockAlerts.data" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Chi ph&iacute; s&#7843;n xu&#7845;t theo th&aacute;ng</h2>
        <MonthlyProductionCostChart
          :months="prodCost.months"
          :materials="prodCost.materials"
          :machines="prodCost.machines"
          :labor="prodCost.labor"
        />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Throughput (batch DONE / th&aacute;ng)</h2>
        <ProductionThroughputChart :months="throughput.months" :done="throughput.done" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Ti&ecirc;u th&#7909; v&#7853;t t&#432; theo th&aacute;ng</h2>
        <MaterialConsumptionChart :months="matConsumption.months" :datasets="matConsumption.datasets" />
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Purchase vs Consumption</h2>
        <PurchaseVsConsumptionChart
          :months="pvsc.months"
          :purchase="pvsc.purchase"
          :consumption="pvsc.consumption"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Low stock materials</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 text-sm">
            <thead class="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th class="px-4 py-3">Code</th>
                <th class="px-4 py-3">Name</th>
                <th class="px-4 py-3">Category</th>
                <th class="px-4 py-3">Min stock</th>
                <th class="px-4 py-3">On hand</th>
                <th class="px-4 py-3">Gap</th>
              </tr>
            </thead>
            <tbody v-if="lowStockRows.length" class="divide-y divide-gray-100 bg-white text-gray-700">
              <tr v-for="row in lowStockRows" :key="row.code" class="hover:bg-indigo-50/40">
                <td class="px-4 py-3 font-medium text-gray-900">{{ row.code }}</td>
                <td class="px-4 py-3">{{ row.name }}</td>
                <td class="px-4 py-3">{{ row.category }}</td>
                <td class="px-4 py-3">{{ row.minStock }}</td>
                <td class="px-4 py-3">{{ row.onHand }}</td>
                <td class="px-4 py-3 text-red-600">{{ row.gap }}</td>
              </tr>
            </tbody>
            <tbody v-else class="bg-white">
              <tr>
                <td class="px-4 py-4 text-center text-sm text-gray-500" colspan="6">
                  Kh&ocirc;ng c&oacute; d&#7919; li&#7879;u
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="rounded-2xl border bg-white p-4 shadow-sm md:p-5">
        <h2 class="mb-4 text-lg font-medium text-gray-800">Top suppliers by lead time</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 text-sm">
            <thead class="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th class="px-4 py-3">Supplier</th>
                <th class="px-4 py-3">Avg days</th>
              </tr>
            </thead>
            <tbody v-if="topSuppliersRows.length" class="divide-y divide-gray-100 bg-white text-gray-700">
              <tr v-for="row in topSuppliersRows" :key="row.supplier" class="hover:bg-indigo-50/40">
                <td class="px-4 py-3 font-medium text-gray-900">{{ row.supplier }}</td>
                <td class="px-4 py-3">{{ row.avgDays }}</td>
              </tr>
            </tbody>
            <tbody v-else class="bg-white">
              <tr>
                <td class="px-4 py-4 text-center text-sm text-gray-500" colspan="2">
                  Kh&ocirc;ng c&oacute; d&#7919; li&#7879;u
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import AgingInventoryChart from '../components/AgingInventoryChart.vue';
import InventoryByCategoryChart from '../components/InventoryByCategoryChart.vue';
import InventoryByWarehouseChart from '../components/InventoryByWarehouseChart.vue';
import MaterialConsumptionChart from '../components/MaterialConsumptionChart.vue';
import MonthlyProductionCostChart from '../components/MonthlyProductionCostChart.vue';
import ProductionThroughputChart from '../components/ProductionThroughputChart.vue';
import PurchaseVsConsumptionChart from '../components/PurchaseVsConsumptionChart.vue';
import StockAlertsPie from '../components/StockAlertsPie.vue';
import { useDashboardStore } from '../store/dashboardStore';

const dashboardStore = useDashboardStore();

const {
  invByCat,
  invByWh,
  agingInv,
  stockAlerts,
  prodCost,
  throughput,
  matConsumption,
  pvsc,
  lowStockRows,
  topSuppliersRows,
} = storeToRefs(dashboardStore);

const year = computed({
  get: () => dashboardStore.year,
  set: (value) => {
    if (!Number.isFinite(value) || value === dashboardStore.year) {
      return;
    }

    dashboardStore.setYear(value);
  },
});

onMounted(() => {
  dashboardStore.loadAll();
});
</script>
