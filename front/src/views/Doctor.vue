<template>
  <div class="doctor-page">
    <Header />
    <div class="main-content container">
      <div class="filter-section card">
        <h3 class="filter-title">选择科室</h3>
        <div class="primary-departments">
          <span 
            v-for="dept in primaryDepts" 
            :key="dept.id"
            :class="{ active: selectedPrimaryId === dept.id }"
            @click="selectPrimary(dept.id)"
          >
            {{ dept.name }}
          </span>
        </div>
        <div v-if="secondaryDepts.length > 0" class="secondary-departments">
          <span 
            v-for="dept in secondaryDepts" 
            :key="dept.id"
            :class="{ active: selectedSecondaryId === dept.id }"
            @click="selectSecondary(dept.id)"
          >
            {{ dept.name }}
          </span>
        </div>
      </div>
      
      <div class="doctor-list">
        <router-link 
          v-for="doctor in doctorList" 
          :key="doctor.id" 
          :to="`/doctor/${doctor.id}`"
          class="doctor-item"
        >
          <img :src="doctor.avatar || '/img/doctor_default.jpg'" :alt="doctor.name" class="doctor-avatar" />
          <h3>{{ doctor.name }}</h3>
          <p class="title">{{ doctor.title }}</p>
          <p class="dept">{{ doctor.hospitalName }} - {{ doctor.departmentName }}</p>
          <div class="price">挂号费: ¥{{ doctor.registrationPrice }}</div>
        </router-link>
      </div>
      
      <div v-if="doctorList.length === 0" class="empty-state">
        <p>暂无医生数据</p>
      </div>
      
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
      />
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getDoctorList } from '@/api/doctor'
import { getPrimaryDepartments, getSecondaryDepartments } from '@/api/department'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Doctor',
  components: { Header, Footer },
  setup() {
    const pageNum = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const doctorList = ref([])
    const primaryDepts = ref([])
    const secondaryDepts = ref([])
    const selectedPrimaryId = ref(null)
    const selectedSecondaryId = ref(null)
    
    const loadDoctors = async () => {
      try {
        const res = await getDoctorList({
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          departmentId: selectedSecondaryId.value || selectedPrimaryId.value
        })
        doctorList.value = res.data.records || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error(error)
      }
    }
    
    const loadDepartments = async () => {
      try {
        const res = await getPrimaryDepartments()
        primaryDepts.value = res.data || []
      } catch (error) {
        console.error(error)
      }
    }
    
    const selectPrimary = async (id) => {
      selectedPrimaryId.value = id
      selectedSecondaryId.value = null
      pageNum.value = 1
      loadDoctors()
      
      try {
        const res = await getSecondaryDepartments(id)
        secondaryDepts.value = res.data || []
      } catch (error) {
        console.error(error)
      }
    }
    
    const selectSecondary = (id) => {
      selectedSecondaryId.value = id
      pageNum.value = 1
      loadDoctors()
    }
    
    const handlePageChange = (page) => {
      pageNum.value = page
      loadDoctors()
    }
    
    onMounted(() => {
      loadDoctors()
      loadDepartments()
    })
    
    return {
      pageNum,
      pageSize,
      total,
      doctorList,
      primaryDepts,
      secondaryDepts,
      selectedPrimaryId,
      selectedSecondaryId,
      selectPrimary,
      selectSecondary,
      handlePageChange
    }
  }
}
</script>

<style lang="scss" scoped>
.doctor-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding: 30px 0;
}

.filter-section {
  margin-bottom: 25px;
  
  .filter-title {
    font-size: 16px;
    color: #333;
    margin-bottom: 15px;
    font-weight: bold;
  }
  
  .primary-departments {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 20px;
    
    span {
      padding: 10px 20px;
      background: #f5f7fa;
      border-radius: 20px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
      
      &:hover { background: #e3f2fd; }
      &.active { background: #1e88e5; color: white; }
    }
  }
  
  .secondary-departments {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding-left: 20px;
    border-left: 3px solid #1e88e5;
    
    span {
      padding: 8px 16px;
      background: #fafbfc;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 13px;
      border: 1px solid #e8eef3;
      
      &:hover { background: #e3f2fd; border-color: #1e88e5; color: #1e88e5; }
      &.active { background: #1e88e5; color: white; border-color: #1e88e5; }
    }
  }
}

.doctor-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  
  .doctor-item {
    text-align: center;
    padding: 20px 15px;
    background: white;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    .doctor-avatar {
      width: 90px;
      height: 90px;
      border-radius: 50%;
      object-fit: cover;
      margin-bottom: 12px;
      border: 3px solid #e8eef3;
    }
    
    h3 {
      font-size: 16px;
      color: #333;
      margin-bottom: 6px;
    }
    
    .title {
      color: #1e88e5;
      font-size: 13px;
      margin-bottom: 6px;
    }
    
    .dept {
      color: #666;
      font-size: 12px;
      margin-bottom: 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .price {
      color: #f57c00;
      font-size: 14px;
      font-weight: bold;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 8px;
  color: #999;
}

.pagination {
  justify-content: center;
  margin-top: 30px;
}
</style>
