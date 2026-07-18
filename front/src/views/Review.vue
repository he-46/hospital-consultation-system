<template>
  <div class="review-page">
    <Header />
    <div class="main-content container">
      <div class="card">
        <h2 class="title">评价挂号服务</h2>

        <div class="order-info" v-if="appt.appointmentDate">
          <div class="info-row">
            <span class="label">医生</span>
            <span class="value">{{ appt.doctorName }} <span class="tag">{{ appt.doctorTitle }}</span></span>
          </div>
          <div class="info-row">
            <span class="label">医院</span>
            <span class="value">{{ appt.hospitalName }}</span>
          </div>
          <div class="info-row">
            <span class="label">就诊时间</span>
            <span class="value">{{ appt.appointmentDate }} {{ appt.appointmentTime }}</span>
          </div>
        </div>

        <div class="review-form" v-if="appt.appointmentDate">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
            <el-form-item label="评分" prop="score">
              <el-rate v-model="form.score" show-score />
            </el-form-item>
            <el-form-item label="评价内容" prop="content">
              <el-input v-model="form.content" type="textarea" rows="5" placeholder="请分享您的就医体验..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
                提交评价
              </el-button>
              <el-button size="large" @click="$router.back()">返回</el-button>
            </el-form-item>
          </el-form>
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
import { getAppointmentDetail } from '@/api/appointment'
import { submitReview } from '@/api/review'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Review',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const appt = ref({})

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
        const res = await getAppointmentDetail(route.params.id)
        const data = res.data
        appt.value = {
          ...data.appointment,
          doctorName: data.doctorName,
          doctorTitle: data.doctorTitle,
          hospitalName: data.hospitalName
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('加载订单信息失败')
        router.replace('/personal?tab=appointments')
      }
    }

    const handleSubmit = async () => {
      if (!formRef.value) return
      await formRef.value.validate(async (valid) => {
        if (!valid) return
        loading.value = true
        try {
          await submitReview({
            orderType: 1,
            orderId: route.params.id,
            doctorId: appt.value.doctorId,
            score: form.value.score,
            content: form.value.content
          })
          ElMessage.success('评价提交成功')
          router.push('/personal?tab=appointments')
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

    return { appt, form, rules, formRef, loading, handleSubmit }
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
}

.review-form {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #e3f2fd;
}
</style>
