<template>
  <div class="statistics-page">
    <TopNav />

    <div class="stats-cards">
      <StatCard 
        :icon-component="DataBoard" 
        value="12,345" 
        label="总数据量" 
        icon-class="blue" 
      />
      <StatCard 
        :icon-component="TrendCharts" 
        value="89%" 
        label="准确率" 
        icon-class="green" 
      />
      <StatCard 
        :icon-component="Clock" 
        value="156" 
        label="今日任务" 
        icon-class="orange" 
      />
      <StatCard 
        :icon-component="User" 
        value="23" 
        label="在线用户" 
        icon-class="purple" 
      />
    </div>
    <div class="charts-section">
      <DataTrendChart title="数据趋势" />
    </div>
  </div>
</template>

<script setup>
import { DataBoard, TrendCharts, Clock, User } from '@element-plus/icons-vue'
import TopNav from '@/components/TopNav.vue'
import StatCard from '@/components/StatCard.vue'
import DataTrendChart from '@/components/DataTrendChart.vue'
</script>

<style lang="scss" scoped>
.statistics-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 80px 20px 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      margin: 0 0 8px 0;
      color: #333;
    }

    p {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
  }

  .charts-section {
    margin-top: 20px;
  }
}
</style>