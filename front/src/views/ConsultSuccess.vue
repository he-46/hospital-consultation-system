<template>
  <div class="consult-success-page">
    <Header />
    <div class="main-content container">
      <div class="card result-card">
        <div class="success-icon">
          <el-icon :size="80" color="#4caf50"><CircleCheckFilled /></el-icon>
        </div>
        <h2 class="result-title">支付成功</h2>
        <p class="result-desc">您的电话咨询已预约成功，请保持电话畅通</p>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ orderNo }}</span>
          </div>
          <div class="info-row" v-if="order.doctorName">
            <span class="label">咨询医生</span>
            <span class="value">{{ order.doctorName }}（{{ order.doctorTitle }}）</span>
          </div>
          <div class="info-row" v-if="order.hospitalName">
            <span class="label">所属医院</span>
            <span class="value">{{ order.hospitalName }}</span>
          </div>
          <div class="info-row" v-if="order.appointmentTime">
            <span class="label">咨询时间</span>
            <span class="value highlight">{{ order.appointmentTime }}</span>
          </div>
          <div class="info-row" v-if="order.amount">
            <span class="label">咨询费用</span>
            <span class="value price">¥{{ order.amount }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" size="large" @click="$router.push('/personal?tab=consults')">
            查看我的咨询
          </el-button>
          <el-button size="large" @click="$router.push('/home')">返回首页</el-button>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getConsultDetail } from '@/api/consult'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const orderNo = ref(route.query.orderNo || '')
const order = ref({})

onMounted(async () => {
  try {
    const res = await getConsultDetail(route.params.id)
    order.value = res.data || {}
  } catch (error) {
    console.error(error)
  }
})
</script>

<style lang="scss" scoped>
.consult-success-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 40px 0; }

.result-card {
  max-width: 600px; margin: 0 auto; text-align: center; padding: 50px 40px;
  .success-icon { margin-bottom: 20px; }
  .result-title { font-size: 24px; color: #333; margin-bottom: 10px; }
  .result-desc { font-size: 14px; color: #999; margin-bottom: 30px; }

  .order-info {
    text-align: left; background: #fafbfc; border-radius: 8px; padding: 20px; margin-bottom: 30px;
    .info-row {
      display: flex; padding: 10px 0; border-bottom: 1px solid #eee;
      &:last-child { border-bottom: none; }
      .label { width: 80px; color: #999; font-size: 14px; }
      .value { font-size: 14px; color: #333; }
      .highlight { color: #1e88e5; font-weight: 500; }
      .price { color: #f57c00; font-weight: bold; }
    }
  }

  .actions { display: flex; gap: 15px; justify-content: center; }
}
</style>
