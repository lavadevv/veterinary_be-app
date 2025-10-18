// src/plugins/chartjs.js
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from 'chart.js';

Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
);

Chart.defaults.plugins.legend.position = 'bottom';
Chart.defaults.plugins.legend.display = true;
Chart.defaults.responsive = true;
Chart.defaults.maintainAspectRatio = false;
