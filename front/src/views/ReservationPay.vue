<template>
  <div class="reservation-pay-page">
    <Header />
    <div class="main-content container">
      <div class="card">
        <h2 class="title">确认支付</h2>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">医生</span>
            <span class="value">{{ order.doctorName }} <span class="tag">{{ order.doctorTitle }}</span></span>
          </div>
          <div class="info-row">
            <span class="label">医院</span>
            <span class="value">{{ order.hospitalName }}</span>
          </div>
          <div class="info-row">
            <span class="label">预约时间</span>
            <span class="value">{{ order.appointmentDate }} {{ order.appointmentTime }}</span>
          </div>
          <div class="info-row">
            <span class="label">就诊人</span>
            <span class="value">{{ order.patientName }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话</span>
            <span class="value">{{ order.patientPhone }}</span>
          </div>
          <div class="info-row price-row">
            <span class="label">挂号费</span>
            <span class="value price">¥{{ order.amount }}</span>
          </div>
        </div>

        <div class="pay-section">
          <el-radio-group v-model="payMethod" class="pay-method">
            <el-radio :label="1">支付宝</el-radio>
            <el-radio :label="2">微信支付</el-radio>
          </el-radio-group>
          <div class="pay-actions">
            <el-button type="primary" size="large" :loading="loading" @click="handlePay">
              立即支付 ¥{{ order.amount }}
            </el-button>
            <el-button size="large" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAppointmentDetail, payAppointment, paymentCallback } from '@/api/appointment'
import { alipayPay } from '@/api/alipay'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'ReservationPay',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const payMethod = ref(1)
    const order = ref({})

    const loadOrder = async () => {
      try {
        const res = await getAppointmentDetail(route.params.id)
        const data = res.data
        order.value = {
          ...data.appointment,
          doctorName: data.doctorName,
          doctorTitle: data.doctorTitle,
          hospitalName: data.hospitalName
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('加载订单信息失败')
      }
    }

    const handlePay = async () => {
      loading.value = true
      try {
        if (payMethod.value === 1) {
          // 支付宝支付
          const res = await alipayPay({
            widout_trade_no: order.value.orderNo,
            widtotal_amount: String(order.value.amount || 0),
            widsubject: '挂号预约-' + order.value.doctorName,
            widbody: '就诊人：' + order.value.patientName
          })
          document.write(res.data)
          document.close()
        } else {
          // 微信支付（模拟）
          await payAppointment(route.params.id, { payMethod: payMethod.value })
          const cbRes = await paymentCallback(order.value.orderNo)
          const cbData = cbRes.data
          router.push(`/reservation-success/${route.params.id}?orderNo=${order.value.orderNo}&tradeNo=${cbData.thirdPartyTradeNo || ''}`)
        }
      } catch (error) {
        ElMessage.error(error?.response?.data?.message || error?.message || '支付失败，请重试')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadOrder()
    })

    return { order, payMethod, loading, handlePay }
  }
}
</script>

<style lang="scss" scoped>
.reservation-pay-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }

.title {
  font-size: 20px;
  color: #333;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e3f2fd;
}

.order-info {
  .info-row {
    display: flex;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;

    .label { width: 100px; color: #999; font-size: 14px; }
    .value { flex: 1; font-size: 14px; color: #333; }

    .tag {
      background: #1e88e5;
      color: white;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      margin-left: 8px;
    }
  }

  .price-row {
    .price { color: #f57c00; font-size: 22px; font-weight: bold; }
  }
}

.pay-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #e3f2fd;

  .pay-method {
    display: flex;
    gap: 30px;
    margin-bottom: 25px;
  }

  .pay-actions {
    display: flex;
    gap: 15px;
  }
}
</style>
