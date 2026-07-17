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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDoctorDetail } from '@/api/doctor'
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
      diseaseDesc: ''
    })
    
    const rules = {
      scheduleId: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
      patientName: [{ required: true, message: '请输入就诊人姓名', trigger: 'blur' }],
      patientPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
      patientIdCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }]
    }
    
    const loadData = async () => {
      try {
        const res = await getDoctorDetail(route.params.doctorId)
        doctor.value = res.data.doctor || {}
        schedules.value = res.data.schedules || []
      } catch (error) {
        console.error(error)
      }
    }
    
    const selectSchedule = (id) => {
      console.log('Selected schedule:', id)
    }
    
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
            // 实际调用预约接口
            ElMessage.success('预约成功！')
            router.push('/personal?tab=appointments')
          } catch (error) {
            console.error(error)
            ElMessage.error('预约失败')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    onMounted(() => {
      loadData()
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
