<template>
  <div class="personal-page">
    <Header />
    <div class="main-content container">
      <div class="personal-layout">
        <div class="sidebar card">
          <div class="menu-group">
            <div class="menu-title">订单管理</div>
            <div 
              v-for="item in orderMenu" 
              :key="item.key"
              :class="{ active: activeTab === item.key }"
              class="menu-item"
              @click="activeTab = item.key"
            >
              {{ item.name }}
            </div>
          </div>
          <div class="menu-group">
            <div class="menu-title">我的</div>
            <div 
              v-for="item in myMenu" 
              :key="item.key"
              :class="{ active: activeTab === item.key }"
              class="menu-item"
              @click="activeTab = item.key"
            >
              {{ item.name }}
            </div>
          </div>
        </div>
        
        <div class="content-area card">
          <h2 class="page-title">{{ currentTitle }}</h2>
          
          <!-- 个人资料 -->
          <div v-if="activeTab === 'profile'" class="profile-form">
            <el-form :model="userForm" label-width="100px">
              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                >
                  <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
                  <img v-else-if="userForm.avatar" :src="getAvatarUrl(userForm.avatar)" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="userForm.realName" />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="userForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userForm.phone" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" />
              </el-form-item>
              <el-form-item label="出生日期">
                <el-date-picker
                  v-model="userForm.birthday"
                  type="date"
                  placeholder="选择出生日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 我的挂号 -->
          <div v-else-if="activeTab === 'appointments'" class="order-list">
            <div class="status-filter">
              <el-radio-group v-model="apptFilter" size="small" @change="loadAppointments">
                <el-radio-button :label="0">全部</el-radio-button>
                <el-radio-button :label="1">待支付</el-radio-button>
                <el-radio-button :label="2">已支付</el-radio-button>
                <el-radio-button :label="3">已完成</el-radio-button>
                <el-radio-button :label="4">已取消</el-radio-button>
              </el-radio-group>
            </div>
            <div v-if="appointments.length === 0" class="empty">暂无挂号记录</div>
            <div v-else v-for="item in appointments" :key="item.id" class="order-item">
              <div class="order-header">
                <span>订单号: {{ item.orderNo }}</span>
                <span class="status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
              </div>
              <div class="order-content">
                <p>医生: {{ item.doctorName }} <span class="tag">{{ item.doctorTitle }}</span></p>
                <p>医院: {{ item.hospitalName }}</p>
                <p>预约时间: {{ item.appointmentDate }} {{ item.appointmentTime }}</p>
                <p>就诊人: {{ item.patientName }}</p>
                <p>挂号费: ¥{{ item.amount }}</p>
              </div>
              <div class="order-footer" v-if="item.status === 1 || item.status === 2 || item.status === 3">
                <el-button v-if="item.status === 1" type="primary" size="small" @click="$router.push(`/reservation-pay/${item.id}`)">去支付</el-button>
                <el-button v-if="item.status === 1 || item.status === 2" type="danger" size="small" @click="handleCancelAppointment(item)">取消</el-button>
                <el-button v-if="item.status === 3" type="success" size="small" @click="$router.push(`/review/${item.id}`)">评价</el-button>
              </div>
            </div>
            <el-pagination
              v-if="apptTotal > apptSize"
              class="pagination"
              v-model:current-page="apptPage"
              :page-size="apptSize"
              :total="apptTotal"
              layout="prev, pager, next"
              @current-change="loadAppointments"
            />
          </div>
          
          <!-- 我的咨询 -->
          <div v-else-if="activeTab === 'consults'" class="order-list">
            <div v-if="consults.length === 0" class="empty">暂无咨询记录</div>
            <div v-else v-for="item in consults" :key="item.id" class="order-item">
              <div class="order-header">
                <span>订单号: {{ item.orderNo }}</span>
                <span class="status" :class="getConsultStatusClass(item.status)">{{ getConsultStatusText(item.status) }}</span>
              </div>
              <div class="order-content">
                <p>医生: {{ item.doctorName }}</p>
                <p>预约时间: {{ item.appointmentTime }}</p>
                <p>咨询人: {{ item.patientName }}</p>
              </div>
            </div>
          </div>
          
          <!-- 关注的医生 -->
          <div v-else-if="activeTab === 'followDoctors'" class="follow-list">
            <div v-if="followDoctors.length === 0" class="empty">暂无关注的医生</div>
            <div v-else class="grid-list">
              <div v-for="item in followDoctors" :key="item.id" class="follow-item">
                <img :src="item.avatar || '/img/doctor_default.jpg'" />
                <div class="info">
                  <h3>{{ item.name }}</h3>
                  <p>{{ item.hospitalName }} - {{ item.departmentName }}</p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 关注的医院 -->
          <div v-else-if="activeTab === 'followHospitals'" class="follow-list">
            <div v-if="followHospitals.length === 0" class="empty">暂无关注的医院</div>
            <div v-else class="grid-list">
              <div v-for="item in followHospitals" :key="item.id" class="follow-item">
                <img :src="item.image || '/img/hospital_default.jpg'" />
                <div class="info">
                  <h3>{{ item.name }}</h3>
                  <p>{{ item.level }}</p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 关注的疾病 -->
          <div v-else-if="activeTab === 'followDiseases'" class="follow-list">
            <div v-if="followDiseases.length === 0" class="empty">暂无关注的疾病</div>
            <div v-else class="disease-list">
              <div v-for="item in followDiseases" :key="item.id" class="disease-item">
                <h3>{{ item.name }}</h3>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
          
          <!-- 我的评价 -->
          <div v-else-if="activeTab === 'reviews'" class="review-list">
            <div v-if="reviews.length === 0" class="empty">暂无评价记录</div>
            <div v-else v-for="item in reviews" :key="item.id" class="review-item">
              <p>{{ item.content }}</p>
              <div class="meta">
                <span>{{ item.createTime }}</span>
                <el-rate v-model="item.rating" disabled />
              </div>
            </div>
          
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { uploadAvatar } from '@/api/file'
import { getAppointmentList, cancelAppointment } from '@/api/appointment'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Personal',
  components: { Header, Footer, Plus },
  setup() {
    const route = useRoute()
    const activeTab = ref(route.query.tab || 'profile')
    
    const userForm = ref({})
    const avatarUrl = ref('')
    const avatarFile = ref(null)
    const appointments = ref([])
    const apptFilter = ref(0)
    const apptPage = ref(1)
    const apptSize = ref(5)
    const apptTotal = ref(0)
    const consults = ref([])
    const followDoctors = ref([])
    const followHospitals = ref([])
    const followDiseases = ref([])
    const reviews = ref([])
    
    const orderMenu = [
      { key: 'appointments', name: '我的挂号' },
      { key: 'consults', name: '我的咨询' }
    ]
    
    const myMenu = [
      { key: 'profile', name: '个人资料' },
      { key: 'followDoctors', name: '关注的医生' },
      { key: 'followHospitals', name: '关注的医院' },
      { key: 'followDiseases', name: '关注的疾病' },
      { key: 'reviews', name: '我的评价' }
    ]
    
    const menuMap = [...orderMenu, ...myMenu]
    const currentTitle = computed(() => menuMap.find(m => m.key === activeTab.value)?.name || '')
    
    const loadUserInfo = async () => {
      try {
        const res = await getUserInfo()
        userForm.value = res.data || {}
      } catch (error) {
        console.error(error)
      }
    }

    const loadAppointments = async () => {
      try {
        const res = await getAppointmentList({ status: apptFilter.value, page: apptPage.value, size: apptSize.value })
        const data = res.data
        appointments.value = data.records || []
        apptTotal.value = data.total || 0
      } catch (error) {
        console.error(error)
      }
    }

    const handleCancelAppointment = async (item) => {
      try {
        await ElMessageBox.confirm('确定要取消此挂号吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await cancelAppointment(item.id)
        ElMessage.success('取消成功')
        loadAppointments()
      } catch (error) {
        if (error !== 'cancel') console.error(error)
      }
    }

    watch(activeTab, (val) => {
      if (val === 'appointments') {
        apptPage.value = 1
        loadAppointments()
      }
    })

    watch(apptFilter, () => {
      apptPage.value = 1
    })
    
    const getAvatarUrl = (avatar) => {
      if (!avatar) {
        console.log('avatar为空')
        return ''
      }
      console.log('处理avatar:', avatar)
      if (avatar.startsWith('http')) return avatar
      if (avatar.startsWith('/uploads/')) {
        return '/api/file' + avatar  // 通过后端 API 访问
      }
      return avatar
    }
    
    const handleAvatarChange = (file) => {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.raw.size / 1024 / 1024 < 2
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return
      }
      
      avatarUrl.value = URL.createObjectURL(file.raw)
      avatarFile.value = file.raw
    }
    
    const saveProfile = async () => {
      try {
        // 如果有新头像，先上传
        if (avatarFile.value) {
          const uploadRes = await uploadAvatar(avatarFile.value)
          userForm.value.avatar = uploadRes.data.url
          avatarFile.value = null
        }
        
        await updateUserInfo(userForm.value)
        ElMessage.success('保存成功')
        // 同步更新 localStorage 中的用户信息
        localStorage.setItem('userInfo', JSON.stringify(userForm.value))
      } catch (error) {
        console.error(error)
      }
    }
    
    const getStatusText = (status) => {
      const map = { 1: '待支付', 2: '已支付', 3: '已完成', 4: '已取消' }
      return map[status] || '未知'
    }
    
    const getStatusClass = (status) => {
      const map = { 1: 'pending', 2: 'paid', 3: 'completed', 4: 'cancelled' }
      return map[status] || ''
    }
    
    const getConsultStatusText = (status) => {
      const map = { 1: '待支付', 2: '已支付', 3: '咨询中', 4: '已完成', 5: '已取消' }
      return map[status] || '未知'
    }
    
    const getConsultStatusClass = (status) => {
      const map = { 1: 'pending', 2: 'paid', 3: 'consulting', 4: 'completed', 5: 'cancelled' }
      return map[status] || ''
    }
    
    onMounted(() => {
      loadUserInfo()
      if (activeTab.value === 'appointments') {
        loadAppointments()
      }
    })
    
    return {
      activeTab,
      userForm,
      avatarUrl,
      appointments,
      apptFilter,
      apptPage,
      apptSize,
      apptTotal,
      consults,
      followDoctors,
      followHospitals,
      followDiseases,
      reviews,
      orderMenu,
      myMenu,
      currentTitle,
      saveProfile,
      loadAppointments,
      handleCancelAppointment,
      getStatusText,
      getStatusClass,
      getConsultStatusText,
      getConsultStatusClass,
      getAvatarUrl,
      handleAvatarChange
    }
  }
}
</script>

<style lang="scss" scoped>
.personal-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }

.personal-layout {
  display: flex;
  gap: 25px;
}

.sidebar {
  width: 240px;
  padding: 20px;
  
  .menu-group { margin-bottom: 25px; }
  .menu-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 4px solid #1e88e5;
  }
  
  .menu-item {
    padding: 12px 15px;
    cursor: pointer;
    border-radius: 6px;
    font-size: 14px;
    color: #666;
    transition: all 0.3s;
    
    &:hover { background: #f0f7ff; color: #1e88e5; }
    &.active { background: #e3f2fd; color: #1e88e5; }
  }
}

.content-area {
  flex: 1;
  
  .page-title {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #e3f2fd;
  }
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}

.status-filter {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

.order-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 15px;
  
  .order-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    font-size: 14px;
    
    .status {
      font-weight: 500;
      &.pending { color: #f57c00; }
      &.paid, &.consulting { color: #1e88e5; }
      &.completed { color: #4caf50; }
      &.cancelled { color: #999; }
    }
  }
  
  .order-content {
    p { font-size: 14px; color: #666; margin-bottom: 5px; }
    .tag {
      background: #1e88e5;
      color: white;
      padding: 1px 6px;
      border-radius: 3px;
      font-size: 11px;
      margin-left: 6px;
    }
  }

  .order-footer {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
}

.grid-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  
  .follow-item {
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    text-align: center;
    
    img {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
      margin-bottom: 10px;
    }
    
    h3 { font-size: 14px; color: #333; margin-bottom: 5px; }
    p { font-size: 12px; color: #999; }
  }
}

.disease-list {
  .disease-item {
    padding: 15px;
    border-bottom: 1px solid #eee;
    
    h3 { font-size: 15px; color: #333; margin-bottom: 5px; }
    p { font-size: 13px; color: #666; }
  }
}

.review-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  
  p { font-size: 14px; color: #666; margin-bottom: 10px; }
  
  .meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    
    &:hover { border-color: #1e88e5; }
  }
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
  }
  
  .avatar-uploader-icon {
    width: 100px;
    height: 100px;
    font-size: 28px;
    color: #8c939d;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
