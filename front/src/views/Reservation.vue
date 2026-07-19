<template>
  <div class="reservation-page">
    <Header />
    <div class="main-content container">
      <div class="card">
        <h2 class="title">在线预约挂号</h2>
        
        <!-- 医生信息 -->
        <div class="doctor-info">
          <img :src="doctor.avatar || '/img/doctor_default.jpg'" class="avatar" />
          <div class="info">
            <h3>{{ doctor.name }} <span class="tag">{{ doctor.title }}</span></h3>
            <p>{{ doctor.hospitalName }} · {{ doctor.departmentName }}</p>
            <p class="price">挂号费: ¥{{ doctor.registrationPrice }}</p>
          </div>
        </div>
        
        <!-- 预约表单 -->
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="选择日期" prop="scheduleId">
            <el-select v-model="form.scheduleId" placeholder="请选择预约日期" @change="selectSchedule">
              <el-option 
                v-for="s in schedules" 
                :key="s.id" 
                :label="`${s.scheduleDate} ${s.timeSlot} (剩余${s.remainCount}个)`" 
                :value="s.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="就诊人" prop="patientName">
            <el-input v-model="form.patientName" placeholder="请输入就诊人姓名" />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="patientPhone">
            <el-input v-model="form.patientPhone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="身份证号" prop="patientIdCard">
            <el-input v-model="form.patientIdCard" placeholder="请输入身份证号" />
          </el-form-item>

          <el-form-item label="性别" prop="patientGender">
            <el-radio-group v-model="form.patientGender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="年龄" prop="patientAge">
            <el-input-number v-model="form.patientAge" :min="0" :max="150" placeholder="请输入年龄" style="width: 100%" />
          </el-form-item>

          <el-form-item label="病情描述">
            <el-input v-model="form.diseaseDesc" type="textarea" rows="3" placeholder="请描述您的病情" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="submitReservation">
              提交预约
            </el-button>
            <el-button size="large" @click="$router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDoctorDetail, getDoctorSchedule } from '@/api/doctor'
import { createAppointment } from '@/api/appointment'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Reservation',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    
    const doctor = ref({})
    const schedules = ref([])
    
    const form = ref({
      scheduleId: null,
      patientName: '',
      patientPhone: '',
      patientIdCard: '',
      patientGender: 1,
      patientAge: null,
      diseaseDesc: ''
    })

    const rules = {
      scheduleId: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
      patientName: [{ required: true, message: '请输入就诊人姓名', trigger: 'blur' }],
      patientPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
      patientIdCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
      patientGender: [{ required: true, message: '请选择性别', trigger: 'change' }]
    }
    
    const loadData = async () => {
      try {
        const res = await getDoctorDetail(route.params.doctorId)
        doctor.value = res.data.doctor || {}
      } catch (error) {
        console.error(error)
      }
    }

    const loadSchedules = async () => {
      try {
        const res = await getDoctorSchedule(route.params.doctorId)
        schedules.value = res.data || []
        // 数据赋值完成后再执行自动选中
        if (route.query.scheduleId) {
          const sid = Number(route.query.scheduleId)
          form.value.scheduleId = sid
          selectSchedule(sid)
        }
      } catch (error) {
        console.error('加载排班失败', error)
      }
    }
    
    const selectSchedule = (id) => {
      const s = schedules.value.find(item => item.id === id)
      if (s) {
        form.value.appointmentDate = s.scheduleDate
        form.value.appointmentTime = s.timeSlot
      }
    }

    watch(() => form.value.patientIdCard, (idCard) => {
      if (idCard && idCard.length === 18) {
        const birthStr = idCard.substring(6, 14)
        const birthYear = parseInt(birthStr.substring(0, 4))
        const birthMonth = parseInt(birthStr.substring(4, 6))
        const birthDay = parseInt(birthStr.substring(6, 8))
        const birthDate = new Date(birthYear, birthMonth - 1, birthDay)
        const today = new Date()
        let age = today.getFullYear() - birthDate.getFullYear()
        const m = today.getMonth() - birthDate.getMonth()
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
          age--
        }
        if (age >= 0 && age <= 150) {
          form.value.patientAge = age
        }
      }
    })

    const submitReservation = async () => {
      if (!formRef.value) return

      await formRef.value.validate(async (valid) => {
        if (valid) {
          if (!localStorage.getItem('token')) {
            ElMessage.warning('请先登录')
            router.push('/login')
            return
          }

          loading.value = true
          try {
            const res = await createAppointment({
              doctorId: doctor.value.id,
              hospitalId: doctor.value.hospitalId,
              scheduleId: form.value.scheduleId,
              patientName: form.value.patientName,
              patientPhone: form.value.patientPhone,
              patientIdCard: form.value.patientIdCard,
              patientGender: form.value.patientGender,
              patientAge: form.value.patientAge,
              appointmentDate: form.value.appointmentDate,
              appointmentTime: form.value.appointmentTime,
              diseaseDesc: form.value.diseaseDesc
            })
            router.push('/reservation-pay/' + res.data.id)
          } catch (error) {
            console.error(error)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    onMounted(() => {
      loadData()
      loadSchedules()
    })
    
    return {
      doctor,
      schedules,
      form,
      rules,
      formRef,
      loading,
      selectSchedule,
      submitReservation
    }
  }
}
</script>

<style lang="scss" scoped>
.reservation-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }

.title {
  font-size: 20px;
  color: #333;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e3f2fd;
}

.doctor-info {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 8px;
  margin-bottom: 30px;
  border-left: 4px solid #1e88e5;
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .info {
    h3 {
      font-size: 20px;
      color: #333;
      margin-bottom: 10px;
      
      .tag {
        background: #1e88e5;
        color: white;
        padding: 3px 10px;
        border-radius: 4px;
        font-size: 12px;
        margin-left: 10px;
      }
    }
    
    p { font-size: 14px; color: #666; margin-bottom: 8px; }
    .price { color: #f57c00; font-weight: bold; }
  }
}
</style>
