<template>
  <div class="reservation-success-page">
    <Header />
    <div class="main-content container">
      <div class="card result-card">
        <div class="success-icon">
          <el-icon :size="80" color="#4caf50"><CircleCheckFilled /></el-icon>
        </div>
        <h2 class="result-title">支付成功</h2>
        <p class="result-desc">您的挂号已预约成功，请按时就诊</p>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ orderNo }}</span>
          </div>
          <div class="info-row" v-if="tradeNo">
            <span class="label">流水号</span>
            <span class="value">{{ tradeNo }}</span>
          </div>
          <div class="info-row" v-if="order.patientName">
            <span class="label">就诊人</span>
            <span class="value">{{ order.patientName }}</span>
          </div>
          <div class="info-row" v-if="order.patientPhone">
            <span class="label">联系电话</span>
            <span class="value">{{ order.patientPhone }}</span>
          </div>
          <div class="info-row" v-if="order.appointmentDate">
            <span class="label">就诊时间</span>
            <span class="value highlight">{{ order.appointmentDate }} {{ order.appointmentTime }}</span>
          </div>
          <div class="info-row" v-if="order.hospitalName">
            <span class="label">就诊医院</span>
            <span class="value">{{ order.hospitalName }}（{{ order.hospitalLevel }}）</span>
          </div>
          <div class="info-row" v-if="order.doctorName">
            <span class="label">就诊医生</span>
            <span class="value">{{ order.doctorName }}（{{ order.doctorTitle }}）</span>
          </div>
          <div class="info-row" v-if="order.diseaseDesc">
            <span class="label">病情描述</span>
            <span class="value">{{ order.diseaseDesc }}</span>
          </div>
          <div class="info-row" v-if="order.amount">
            <span class="label">挂号费用</span>
            <span class="value price">¥{{ order.amount }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" size="large" @click="$router.push('/personal?tab=appointments')">
            查看我的挂号
          </el-button>
          <el-button size="large" @click="$router.push('/home')">返回首页</el-button>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAppointmentDetail } from '@/api/appointment'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'ReservationSuccess',
  components: { Header, Footer, CircleCheckFilled },
  setup() {
    const route = useRoute()
    const orderNo = ref(route.query.orderNo || '')
    const tradeNo = ref(route.query.tradeNo || '')
    const order = ref({})

    const loadOrder = async () => {
      const id = route.params.id
      if (!id || id === 'undefined') return
      try {
        const res = await getAppointmentDetail(id)
        const data = res.data
        const appt = data.appointment || {}
        order.value = {
          ...appt,
          doctorName: data.doctorName,
          doctorTitle: data.doctorTitle,
          hospitalName: data.hospitalName,
          hospitalLevel: data.hospitalLevel
        }
        if (!orderNo.value) {
          orderNo.value = appt.orderNo || ''
        }
      } catch (error) {
        console.error(error)
      }
    }

    onMounted(() => {
      loadOrder()
    })

    return { orderNo, tradeNo, order }
  }
}
</script>

<style lang="scss" scoped>
.reservation-success-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 40px 0; }

.result-card {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
  padding: 50px 40px;

  .success-icon { margin-bottom: 20px; }
  .result-title { font-size: 24px; color: #333; margin-bottom: 10px; }
  .result-desc { font-size: 14px; color: #999; margin-bottom: 30px; }

  .order-info {
    text-align: left;
    background: #fafbfc;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 30px;

    .info-row {
      display: flex;
      padding: 10px 0;
      border-bottom: 1px solid #eee;

      &:last-child { border-bottom: none; }
      .label { width: 80px; color: #999; font-size: 14px; flex-shrink: 0; }
      .value { font-size: 14px; color: #333; word-break: break-all; }
      .highlight { color: #1e88e5; font-weight: 500; }
      .price { color: #f57c00; font-weight: bold; }
    }
  }

  .actions {
    display: flex;
    gap: 15px;
    justify-content: center;
  }
}
</style>
