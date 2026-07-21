<template>
  <div class="review-page">
    <Header />
    <div class="main-content container">
      <div class="card">
        <h2 class="title">{{ orderType === 2 ? '评价咨询医生' : '评价挂号服务' }}</h2>

        <!-- 加载中 -->
        <div v-if="pageLoading" class="loading-wrap">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- 加载失败 -->
        <div v-else-if="loadError" class="error-wrap">
          <p class="error-msg">{{ loadError }}</p>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>

        <!-- 正常内容 -->
        <template v-else>
          <div class="order-info">
            <div class="info-row">
              <span class="label">医生</span>
              <span class="value">{{ orderInfo.doctorName || '--' }}
                <span class="tag" v-if="orderInfo.doctorTitle">{{ orderInfo.doctorTitle }}</span>
              </span>
            </div>
            <div class="info-row">
              <span class="label">医院</span>
              <span class="value">{{ orderInfo.hospitalName || '--' }}</span>
            </div>

            <!-- 挂号特有 -->
            <template v-if="orderType === 1">
              <div class="info-row" v-if="orderInfo.appointmentDate">
                <span class="label">就诊时间</span>
                <span class="value">{{ orderInfo.appointmentDate }} {{ orderInfo.appointmentTime }}</span>
              </div>
            </template>

            <!-- 咨询特有 -->
            <template v-if="orderType === 2">
              <div class="info-row" v-if="orderInfo.appointmentTime">
                <span class="label">咨询时间</span>
                <span class="value">{{ orderInfo.appointmentTime }}</span>
              </div>
              <div class="info-row" v-if="orderInfo.duration">
                <span class="label">咨询时长</span>
                <span class="value">{{ orderInfo.duration }} 分钟</span>
              </div>
              <div class="info-row" v-if="orderInfo.diseaseDesc">
                <span class="label">病情描述</span>
                <span class="value desc-text">{{ orderInfo.diseaseDesc }}</span>
              </div>
            </template>

            <div class="info-row">
              <span class="label">{{ orderType === 2 ? '咨询人' : '就诊人' }}</span>
              <span class="value">{{ orderInfo.patientName || '--' }}</span>
            </div>
            <div class="info-row" v-if="orderInfo.patientPhone">
              <span class="label">联系电话</span>
              <span class="value">{{ orderInfo.patientPhone }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单金额</span>
              <span class="value price">¥{{ orderInfo.amount || '0.00' }}</span>
            </div>
          </div>

          <div class="review-form">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
              <el-form-item label="评分" prop="score">
                <el-rate v-model="form.score" show-score />
              </el-form-item>
              <el-form-item label="评价内容" prop="content">
                <el-input v-model="form.content" type="textarea" rows="5"
                  :placeholder="orderType === 2 ? '请分享您的咨询体验...' : '请分享您的就医体验...'" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
                  提交评价
                </el-button>
                <el-button size="large" @click="goBack">返回</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getAppointmentDetail } from '@/api/appointment'
import { getConsultDetail } from '@/api/consult'
import { submitReview } from '@/api/review'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Review',
  components: { Header, Footer, Loading },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const pageLoading = ref(true)
    const loadError = ref('')
    const orderInfo = ref({})
    const orderType = ref(Number(route.query.orderType) || 1)

    const form = ref({
      score: 5,
      content: ''
    })

    const rules = {
      score: [{ required: true, message: '请选择评分', trigger: 'change' }],
      content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
    }

    const loadOrder = async () => {
      try {
        pageLoading.value = true
        loadError.value = ''

        if (orderType.value === 2) {
          // 咨询订单
          const res = await getConsultDetail(route.params.id)
          const data = res.data
          if (!data) {
            loadError.value = '未找到该咨询订单'
            return
          }
          orderInfo.value = {
            ...data,
            doctorId: data.doctorId
          }
        } else {
          // 挂号订单
          const res = await getAppointmentDetail(route.params.id)
          const data = res.data
          if (!data || !data.appointment) {
            loadError.value = '未找到该挂号订单'
            return
          }
          orderInfo.value = {
            ...data.appointment,
            doctorName: data.doctorName,
            doctorTitle: data.doctorTitle,
            hospitalName: data.hospitalName
          }
        }
      } catch (error) {
        console.error(error)
        const msg = error?.response?.data?.message || '加载订单信息失败'
        loadError.value = msg
      } finally {
        pageLoading.value = false
      }
    }

    const goBack = () => {
      if (orderType.value === 2) {
        router.push('/personal?tab=consults')
      } else {
        router.push('/personal?tab=appointments')
      }
    }

    const handleSubmit = async () => {
      if (!formRef.value) return
      await formRef.value.validate(async (valid) => {
        if (!valid) return
        loading.value = true
        try {
          await submitReview({
            orderType: orderType.value,
            orderId: route.params.id,
            doctorId: orderInfo.value.doctorId,
            score: form.value.score,
            content: form.value.content
          })
          ElMessage.success('评价提交成功')
          goBack()
        } catch (error) {
          ElMessage.error(error?.response?.data?.message || error?.message || '提交失败')
        } finally {
          loading.value = false
        }
      })
    }

    onMounted(() => {
      loadOrder()
    })

    return { orderInfo, orderType, form, rules, formRef, loading, pageLoading, loadError, goBack, handleSubmit }
  }
}
</script>

<style lang="scss" scoped>
.review-page { min-height: 100vh; background: #f5f7fa; }
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

    .label { width: 100px; color: #999; font-size: 14px; flex-shrink: 0; }
    .value { flex: 1; font-size: 14px; color: #333; }

    .tag {
      background: #1e88e5;
      color: white;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      margin-left: 8px;
    }

    .desc-text {
      color: #666;
      line-height: 1.6;
    }

    .price {
      color: #e53935;
      font-weight: 500;
    }
  }
}

.review-form {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #e3f2fd;
}

.loading-wrap {
  text-align: center;
  padding: 60px 0;
  color: #999;

  .loading-icon {
    font-size: 36px;
    animation: spin 1s linear infinite;
  }

  p { margin-top: 12px; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-wrap {
  text-align: center;
  padding: 60px 0;

  .error-msg {
    color: #f56c6c;
    font-size: 16px;
    margin-bottom: 20px;
  }
}
</style>
