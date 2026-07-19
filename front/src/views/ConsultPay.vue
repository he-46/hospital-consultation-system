<template>
  <div class="consult-pay-page">
    <Header />
    <div class="main-content container">
      <div class="card pay-card">
        <h2 class="title">确认支付 - 电话咨询</h2>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">咨询医生</span>
            <span class="value">{{ order.doctorName }} <span class="tag">{{ order.doctorTitle }}</span></span>
          </div>
          <div class="info-row">
            <span class="label">所属医院</span>
            <span class="value">{{ order.hospitalName }}</span>
          </div>
          <div class="info-row">
            <span class="label">咨询人</span>
            <span class="value">{{ order.patientName }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话</span>
            <span class="value">{{ order.patientPhone }}</span>
          </div>
          <div class="info-row">
            <span class="label">病情描述</span>
            <span class="value">{{ order.diseaseDesc }}</span>
          </div>
          <div class="info-row price-row">
            <span class="label">咨询费</span>
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

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getConsultDetail, payConsult } from '@/api/consult'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const payMethod = ref(1)
const order = ref({})

const loadOrder = async () => {
  try {
    console.log('ConsultPay 加载订单, id:', route.params.id)
    const res = await getConsultDetail(route.params.id)
    console.log('ConsultPay 订单详情响应:', JSON.stringify(res))
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载订单失败')
      console.error('订单详情接口返回非200:', res)
      return
    }
    order.value = res.data || {}
    console.log('order 赋值后:', JSON.stringify(order.value))
  } catch (error) {
    console.error('加载订单异常:', error)
    ElMessage.error('加载订单信息失败')
  }
}

const handlePay = async () => {
  loading.value = true
  try {
    const res = await payConsult(route.params.id, { payType: payMethod.value })
    if (res.code !== 200) {
      ElMessage.error(res.message || '支付失败')
      return
    }
    router.push(`/consult-success/${route.params.id}?orderNo=${res.data.orderNo}&tradeNo=${res.data.thirdPartyTradeNo || ''}`)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '支付失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style lang="scss" scoped>
.consult-pay-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }
.pay-card { max-width: 650px; margin: 0 auto; padding: 30px; }

.title {
  font-size: 20px; color: #333; margin-bottom: 25px;
  padding-bottom: 15px; border-bottom: 2px solid #e3f2fd;
}

.order-info .info-row {
  display: flex; padding: 14px 0; border-bottom: 1px solid #f0f0f0;
  .label { width: 100px; color: #999; font-size: 14px; }
  .value { flex: 1; font-size: 14px; color: #333; }
  .tag { background: #1e88e5; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-left: 8px; }
}

.price-row .price { color: #f57c00; font-size: 22px; font-weight: bold; }

.pay-section {
  margin-top: 30px; padding-top: 20px; border-top: 2px solid #e3f2fd;
  .pay-method { display: flex; gap: 30px; margin-bottom: 25px; }
  .pay-actions { display: flex; gap: 15px; }
}
</style>
