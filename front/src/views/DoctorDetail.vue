<template>
  <div class="doctor-detail-page">
    <Header />
    <div class="main-content container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/doctor' }">找医生</el-breadcrumb-item>
        <el-breadcrumb-item>医生详情</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div v-if="doctor.id" class="detail-content">
        <div class="doctor-header card">
          <div class="doctor-avatar">
            <img :src="doctor.avatar || '/img/doctor_default.jpg'" :alt="doctor.name" />
          </div>
          <div class="doctor-info">
            <h1>{{ doctor.name }}</h1>
            <div class="title-tag">{{ doctor.title }}</div>
            <p class="hospital-dept">
              {{ doctor.hospitalName }} · {{ doctor.departmentName }}
            </p>
            <div class="stats">
              <span><strong>{{ doctor.consultCount }}</strong> 接诊量</span>
              <span><strong>{{ doctor.followCount }}</strong> 关注</span>
              <span><strong>{{ doctor.rating }}</strong> 评分</span>
            </div>
            <div class="price-info">
              <span class="price-item">
                挂号费: <strong>¥{{ doctor.registrationPrice }}</strong>
              </span>
              <span class="price-item">
                咨询费: <strong>¥{{ doctor.price }}</strong>
              </span>
            </div>
          </div>
          <div class="action-btns">
            <el-button type="primary" @click="$router.push(`/reservation/${doctor.id}`)">
              预约挂号
            </el-button>
            <el-button @click="$router.push(`/phone-consult/${doctor.id}`)">电话咨询</el-button>
          </div>
        </div>
        
        <div class="section card">
          <h2 class="section-title">医生简介</h2>
          <div class="content">
            <p>{{ doctor.intro || '暂无简介' }}</p>
          </div>
        </div>
        
        <div class="section card">
          <h2 class="section-title">擅长领域</h2>
          <div class="expertise-tags">
            <el-tag v-for="(tag, index) in expertiseList" :key="index" type="warning">
              {{ tag }}
            </el-tag>
          </div>
        </div>
        
        <div class="section card">
          <h2 class="section-title">出诊时间</h2>
          <el-table :data="schedules" stripe>
            <el-table-column prop="scheduleDate" label="日期" width="150" />
            <el-table-column prop="timeSlot" label="时间段" />
            <el-table-column prop="remainCount" label="剩余号源">
              <template #default="{ row }">
                {{ row.remainCount > 0 ? row.remainCount + '个' : '已满' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  :disabled="row.remainCount === 0"
                  @click="$router.push(`/reservation/${doctor.id}?scheduleId=${row.id}`)"
                >
                  预约
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getDoctorDetail } from '@/api/doctor'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'DoctorDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const doctor = ref({})
    const schedules = ref([])
    
    const expertiseList = computed(() => {
      if (!doctor.value.expertise) return []
      return doctor.value.expertise.split(/[,，]/).filter(Boolean)
    })
    
    const loadData = async () => {
      try {
        const res = await getDoctorDetail(route.params.id)
        const data = res.data
        doctor.value = data.doctor || {}
        schedules.value = data.schedules || []
      } catch (error) {
        console.error(error)
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    return { doctor, schedules, expertiseList }
  }
}
</script>

<style lang="scss" scoped>
.doctor-detail-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 20px 0 40px; }

.doctor-header {
  display: flex;
  gap: 30px;
  margin: 20px 0;
  align-items: center;
  
  .doctor-avatar {
    width: 150px;
    height: 150px;
    flex-shrink: 0;
    
    img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; border: 4px solid #e3f2fd; }
  }
  
  .doctor-info {
    flex: 1;
    
    h1 { font-size: 24px; color: #333; margin-bottom: 10px; }
    
    .title-tag {
      display: inline-block;
      background: #1e88e5;
      color: white;
      padding: 4px 12px;
      border-radius: 4px;
      margin-bottom: 10px;
    }
    
    .hospital-dept { color: #666; font-size: 14px; margin-bottom: 15px; }
    
    .stats {
      display: flex;
      gap: 30px;
      margin-bottom: 15px;
      font-size: 14px;
      color: #666;
      
      strong { color: #1e88e5; margin-right: 5px; }
    }
    
    .price-info {
      .price-item {
        margin-right: 20px;
        font-size: 14px;
        color: #666;
        
        strong { color: #f57c00; font-size: 18px; }
      }
    }
  }
  
  .action-btns {
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    .el-button { width: 120px; }
  }
}

.section {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 18px;
    color: #333;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 2px solid #e3f2fd;
  }
  
  .content p { color: #666; line-height: 1.8; font-size: 14px; }
  
  .expertise-tags .el-tag { margin: 0 8px 8px 0; }
}
</style>
