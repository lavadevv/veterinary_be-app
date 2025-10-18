// src/modules/dashboard/store/dashboardStore.js
import { defineStore } from 'pinia';
import {
  getAgingInventory,
  getAvgPriceByBrandPerMonth,
  getBatchStatusBreakdown,
  getInventoryByCategory,
  getInventoryByWarehouse,
  getLowStockMaterials,
  getMaterialConsumptionByMonth,
  getMonthlyProductionCost,
  getProductionThroughput,
  getPurchaseVsConsumption,
  getQCResultRates,
  getStockAlertsSummary,
  getSupplierLeadTime,
  getSupplierOTD,
  getTopSuppliersByLeadTime,
} from '../services/dashboardService';

const emptyInvByCat = { labels: [], data: [] };
const emptyInvByWh = { labels: [], datasets: [] };
const emptyAgingInv = { labels: [], data: [] };
const emptyStockAlerts = { labels: [], data: [] };
const emptyProdCost = { months: [], materials: [], machines: [], labor: [] };
const emptyThroughput = { months: [], done: [] };
const emptyBatchStatus = { labels: [], data: [] };
const emptyMatConsumption = { months: [], datasets: [] };
const emptyPvsc = { months: [], purchase: [], consumption: [] };
const emptyLeadTime = { labels: [], data: [] };
const emptyOtd = { labels: [], data: [] };
const emptyQcRates = { labels: [], data: [] };
const emptyPriceByBrand = { months: [], datasets: [] };

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    year: new Date().getFullYear(),
    loading: false,
    invByCat: { ...emptyInvByCat },
    invByWh: { ...emptyInvByWh },
    agingInv: { ...emptyAgingInv },
    stockAlerts: { ...emptyStockAlerts },

    prodCost: { ...emptyProdCost },
    throughput: { ...emptyThroughput },
    batchStatus: { ...emptyBatchStatus },
    matConsumption: { ...emptyMatConsumption },
    pvsc: { ...emptyPvsc },

    leadTime: { ...emptyLeadTime },
    otd: { ...emptyOtd },
    qcRates: { ...emptyQcRates },
    priceByBrand: { ...emptyPriceByBrand },

    lowStockRows: [],
    topSuppliersRows: [],
  }),
  actions: {
    async loadAll() {
      this.loading = true;
      try {
        const year = this.year;
        const [
          invByCat,
          invByWh,
          agingInv,
          stockAlerts,
          prodCost,
          throughput,
          batchStatus,
          matConsumption,
          pvsc,
          leadTime,
          otd,
          qcRates,
          priceByBrand,
          lowStockRows,
          topSuppliersRows,
        ] = await Promise.all([
          getInventoryByCategory(),
          getInventoryByWarehouse(),
          getAgingInventory(),
          getStockAlertsSummary(),
          getMonthlyProductionCost(year),
          getProductionThroughput(year),
          getBatchStatusBreakdown(),
          getMaterialConsumptionByMonth(year),
          getPurchaseVsConsumption(year),
          getSupplierLeadTime(),
          getSupplierOTD(),
          getQCResultRates(),
          getAvgPriceByBrandPerMonth(year),
          getLowStockMaterials(),
          getTopSuppliersByLeadTime(),
        ]);

        this.invByCat = invByCat ?? { ...emptyInvByCat };
        this.invByWh = invByWh ?? { ...emptyInvByWh };
        this.agingInv = agingInv ?? { ...emptyAgingInv };
        this.stockAlerts = stockAlerts ?? { ...emptyStockAlerts };

        this.prodCost = prodCost ?? { ...emptyProdCost };
        this.throughput = throughput ?? { ...emptyThroughput };
        this.batchStatus = batchStatus ?? { ...emptyBatchStatus };
        this.matConsumption = matConsumption ?? { ...emptyMatConsumption };
        this.pvsc = pvsc ?? { ...emptyPvsc };

        this.leadTime = leadTime ?? { ...emptyLeadTime };
        this.otd = otd ?? { ...emptyOtd };
        this.qcRates = qcRates ?? { ...emptyQcRates };
        this.priceByBrand = priceByBrand ?? { ...emptyPriceByBrand };

        this.lowStockRows = Array.isArray(lowStockRows) ? lowStockRows : [];
        this.topSuppliersRows = Array.isArray(topSuppliersRows) ? topSuppliersRows : [];
      } catch (error) {
        console.error('Failed to load dashboard data:', error);
      } finally {
        this.loading = false;
      }
    },
    async setYear(yearValue) {
      const parsed = Number.parseInt(yearValue, 10);
      if (!Number.isNaN(parsed) && parsed > 0) {
        this.year = parsed;
      }

      this.loading = true;
      try {
        const year = this.year;
        const [prodCost, throughput, matConsumption, pvsc, priceByBrand] = await Promise.all([
          getMonthlyProductionCost(year),
          getProductionThroughput(year),
          getMaterialConsumptionByMonth(year),
          getPurchaseVsConsumption(year),
          getAvgPriceByBrandPerMonth(year),
        ]);

        this.prodCost = prodCost ?? { ...emptyProdCost };
        this.throughput = throughput ?? { ...emptyThroughput };
        this.matConsumption = matConsumption ?? { ...emptyMatConsumption };
        this.pvsc = pvsc ?? { ...emptyPvsc };
        this.priceByBrand = priceByBrand ?? { ...emptyPriceByBrand };
      } catch (error) {
        console.error('Failed to update year-specific dashboard data:', error);
      } finally {
        this.loading = false;
      }
    },
  },
});
