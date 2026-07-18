<template>
  <div class="consult-page">
    <Header />
    <div class="main-content container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item to="/home">首页</el-breadcrumb-item>
        <el-breadcrumb-item to="/doctor">找医生</el-breadcrumb-item>
        <el-breadcrumb-item>电话咨询</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="doctor.id" class="card consult-card">
        <div class="doctor-header">
          <div class="doctor-avatar">
            <el-avatar :size="120" :src="doctor.avatar">
              <el-icon :size="48"><User /></el-icon>
            </el-avatar>
          </div>
          <div class="doctor-info">
            <h1>
              {{ doctor.name }}
              <span v-if="doctor.onlineStatus === 1" class="online-badge">在线</span>
              <span v-else class="offline-badge">离线</span>
            </h1>
            <div class="title-tag">{{ doctor.title }}</div>
            <p class="hospital-dept">{{ doctor.hospitalName }} - {{ doctor.departmentName }}</p>
            <div class="stats">
              <span><strong>{{ doctor.followCount || 0 }}</strong> 关注</span>
              <span><strong>{{ doctor.rating || '5.00' }}</strong> 评分</span>
            </div>
            <div class="price-info">
              咨询费：<strong class="price">{{ doctor.price }}</strong> 元/次
            </div>
          </div>
        </div>

        <el-alert class="tip-alert" type="info" title="咨询说明"
                   description="本次电话咨询时长30分钟，提交后需在30分钟内完成支付，逾期自动取消。"
                   :closable="false"/>

        <!-- 咨询表单 -->
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="consult-form">
          <el-form-item label="咨询人姓名" prop="patientName">
            <el-input v-model="form.patientName" placeholder="请输入您的姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="patientPhone">
            <el-input v-model="form.patientPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="病情描述" prop="diseaseDesc">
            <el-input v-model="form.diseaseDesc" type="textarea" :rows="4"
                      placeholder="请描述您的症状、持续时间和疑问" />
          </el-form-item>
          <el-form-item label="期望时间" prop="appointmentTime">
            <el-date-picker v-model="form.appointmentTime" type="datetime"
                            placeholder="选择期望的咨询时间"
                            :disabled-date="disabledDate"
                            value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-form>

        <div class="submit-block">
          <el-button type="primary" size="large" @click="handleCreateConsult" :loading="submitting">
            立即发起电话咨询
          </el-button>
        </div>
      </div>

      <el-empty v-else description="未查询到该医生信息" />
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDoctorDetail } from '@/api/doctor'
import { createConsult } from '@/api/consult'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const router = useRouter()
const doctor = ref({})
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  patientName: '',
  patientPhone: '',
  diseaseDesc: '',
  appointmentTime: ''
})

const rules = {
  patientName: [{ required: true, message: '请输入咨询人姓名', trigger: 'blur' }],
  patientPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  diseaseDesc: [{ required: true, message: '请描述病情', trigger: 'blur' }],
  appointmentTime: [{ required: true, message: '请选择期望时间', trigger: 'change' }]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选过去的时间
}

// 加载医生详情
const loadDoctor = async () => {
  try {
    const res = await getDoctorDetail(route.params.doctorId)
    doctor.value = res.data.doctor || {}
  } catch (e) {
    console.error('加载医生失败', e)
  }
}

// 发起咨询
const handleCreateConsult = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      doctorId: Number(route.params.doctorId),
      patientName: form.patientName,
      patientPhone: form.patientPhone,
      diseaseDesc: form.diseaseDesc,
      appointmentTime: form.appointmentTime
    }
    console.log('创建咨询请求参数:', payload)
    const res = await createConsult(payload)
    console.log('创建咨询响应:', JSON.stringify(res))
    if (res.code !== 200) {
      ElMessage.error(res.message || '创建咨询失败')
      return
    }
    console.log('订单ID:', res.data.id)
    ElMessage.success('咨询订单创建成功')
    router.push('/consult-pay/' + res.data.id)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || err.message || '创建咨询失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDoctor()
})
</script>

<style lang="scss" scoped>
.consult-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.main-content {
  padding: 20px 0 40px;
}
.consult-card {
  margin: 20px auto;
  max-width: 700px;
  padding: 30px;
}
.doctor-header {
  display: flex;
  gap: 30px;
  align-items: center;
  margin-bottom: 20px;
}
.online-badge {
  background: #4caf50;
  color: white;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  margin-left: 8px;
}
.offline-badge {
  background: #999;
  color: white;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  margin-left: 8px;
}
.title-tag {
  background: #1e88e5;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  display: inline-block;
  margin: 8px 0;
  font-size: 13px;
}
.hospital-dept {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}
.stats {
  display: flex;
  gap: 30px;
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}
.stats strong {
  color: #1e88e5;
}
.price-info .price {
  color: #f57c00;
  font-size: 20px;
}
.tip-alert {
  margin: 20px 0;
}
.consult-form {
  margin-top: 24px;
}
.submit-block {
  text-align: center;
  margin-top: 30px;
}
</style>
