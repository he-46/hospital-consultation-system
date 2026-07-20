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
import { ref, onMounted, onUnmounted } from 'vue'
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
    let pollTimer = null
    let payWindow = null
    let navigated = false

    const goSuccess = (id, orderNo, tradeNo) => {
      if (navigated) return
      navigated = true
      if (payWindow && !payWindow.closed) {
        payWindow.close()
      }
      router.push(`/reservation-success/${id}?orderNo=${orderNo || ''}&tradeNo=${tradeNo || ''}`)
    }

    const pollPaymentStatus = () => {
      const apptId = route.params.id
      if (!apptId || apptId === 'undefined') return
      if (pollTimer) clearInterval(pollTimer)
      let count = 0
      const maxCount = 60
      pollTimer = setInterval(async () => {
        count++
        if (count > maxCount) {
          clearInterval(pollTimer)
          pollTimer = null
          return
        }
        try {
          const res = await getAppointmentDetail(apptId)
          if (res.code === 200 && res.data) {
            const appt = res.data.appointment || res.data
            const status = appt.status
            if (status == 2) {
              clearInterval(pollTimer)
              pollTimer = null
              goSuccess(apptId, appt.orderNo || '', '')
            }
          }
        } catch (e) {
          // 忽略轮询错误
        }
      }, 2000)
    }

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
          // 支付宝支付：解析返回的HTML表单，提交到新窗口
          const res = await alipayPay({
            widout_trade_no: order.value.orderNo,
            widtotal_amount: String(order.value.amount || 0),
            widsubject: '挂号预约-' + order.value.doctorName,
            widbody: '就诊人：' + order.value.patientName
          })
          const tempDiv = document.createElement('div')
          tempDiv.innerHTML = res.data
          const form = tempDiv.querySelector('form')
          if (form) {
            payWindow = window.open('', 'alipayPayWindow')
            form.target = 'alipayPayWindow'
            form.style.display = 'none'
            document.body.appendChild(form)
            form.submit()
            document.body.removeChild(form)
            pollPaymentStatus()
          } else {
            payWindow = window.open(URL.createObjectURL(new Blob([res.data], { type: 'text/html' })), '_blank')
          }
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

    const handlePayMessage = (event) => {
      if (event.data && event.data.type === 'PAY_SUCCESS') {
        const id = event.data.orderId || route.params.id
        if (!id || id === 'undefined') return
        goSuccess(id, event.data.orderNo, event.data.tradeNo || '')
      }
    }

    onMounted(() => {
      loadOrder()
      window.addEventListener('message', handlePayMessage)
    })

    onUnmounted(() => {
      window.removeEventListener('message', handlePayMessage)
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
      if (payWindow && !payWindow.closed) {
        payWindow.close()
      }
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
