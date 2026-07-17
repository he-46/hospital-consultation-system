<template>
  <div class="hospital-page">
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
      
      <div class="hospital-list">
        <router-link 
          v-for="hospital in hospitalList" 
          :key="hospital.id" 
          :to="`/hospital/${hospital.id}`"
          class="hospital-item"
        >
          <div class="hospital-img">
            <img :src="hospital.image || '/img/hospital_default.jpg'" :alt="hospital.name" />
          </div>
          <div class="hospital-info">
            <h3>{{ hospital.name }}</h3>
            <p class="address">{{ hospital.address }}</p>
            <div class="tags">
              <span class="level-tag">{{ hospital.level }}</span>
              <span class="dept-count">{{ hospital.departmentCount }}个科室</span>
              <span class="doctor-count">{{ hospital.doctorCount }}名医生</span>
            </div>
          </div>
        </router-link>
      </div>
      
      <div v-if="hospitalList.length === 0" class="empty-state">
        <p>暂无医院数据</p>
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
import { getHospitalList } from '@/api/hospital'
import { getPrimaryDepartments, getSecondaryDepartments } from '@/api/department'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Hospital',
  components: { Header, Footer },
  setup() {
    const pageNum = ref(1)
    const pageSize = ref(12)
    const total = ref(0)
    const hospitalList = ref([])
    const primaryDepts = ref([])
    const secondaryDepts = ref([])
    const selectedPrimaryId = ref(null)
    const selectedSecondaryId = ref(null)
    
    const loadHospitals = async () => {
      try {
        const res = await getHospitalList({
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          departmentId: selectedSecondaryId.value || selectedPrimaryId.value
        })
        hospitalList.value = res.data.records || []
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
      loadHospitals()
      
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
      loadHospitals()
    }
    
    const handlePageChange = (page) => {
      pageNum.value = page
      loadHospitals()
    }
    
    onMounted(() => {
      loadHospitals()
      loadDepartments()
    })
    
    return {
      pageNum,
      pageSize,
      total,
      hospitalList,
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
.hospital-page {
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
      
      &:hover {
        background: #e3f2fd;
      }
      
      &.active {
        background: #1e88e5;
        color: white;
      }
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
      
      &:hover {
        background: #e3f2fd;
        border-color: #1e88e5;
        color: #1e88e5;
      }
      
      &.active {
        background: #1e88e5;
        color: white;
        border-color: #1e88e5;
      }
    }
  }
}

.hospital-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  
  .hospital-item {
    background: white;
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    .hospital-img {
      width: 100%;
      height: 150px;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .hospital-info {
      padding: 15px;
      
      h3 {
        font-size: 16px;
        color: #333;
        margin-bottom: 8px;
      }
      
      .address {
        font-size: 13px;
        color: #666;
        margin-bottom: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .level-tag {
          background: #e3f2fd;
          color: #1e88e5;
          padding: 3px 8px;
          border-radius: 3px;
          font-size: 12px;
        }
        
        .dept-count, .doctor-count {
          color: #999;
          font-size: 12px;
        }
      }
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
