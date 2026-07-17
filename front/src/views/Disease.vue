<template>
  <div class="disease-page">
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
      </div>
      
      <div class="disease-list">
        <router-link 
          v-for="disease in diseaseList" 
          :key="disease.id" 
          :to="`/disease/${disease.id}`"
          class="disease-item"
        >
          <div class="disease-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="disease-info">
            <h3>{{ disease.name }}</h3>
            <p>{{ disease.description }}</p>
            <div class="disease-tags">
              <span v-if="disease.alias">别名: {{ disease.alias }}</span>
              <span>关注: {{ disease.followCount }}</span>
            </div>
          </div>
        </router-link>
      </div>
      
      <div v-if="diseaseList.length === 0" class="empty-state">
        <p>暂无疾病数据</p>
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
import { getDiseaseList } from '@/api/disease'
import { getPrimaryDepartments } from '@/api/department'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Disease',
  components: { Header, Footer },
  setup() {
    const pageNum = ref(1)
    const pageSize = ref(12)
    const total = ref(0)
    const diseaseList = ref([])
    const primaryDepts = ref([])
    const selectedPrimaryId = ref(null)
    
    const loadDiseases = async () => {
      try {
        const res = await getDiseaseList({
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          departmentId: selectedPrimaryId.value
        })
        diseaseList.value = res.data.records || []
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
    
    const selectPrimary = (id) => {
      selectedPrimaryId.value = id
      pageNum.value = 1
      loadDiseases()
    }
    
    const handlePageChange = (page) => {
      pageNum.value = page
      loadDiseases()
    }
    
    onMounted(() => {
      loadDiseases()
      loadDepartments()
    })
    
    return {
      pageNum,
      pageSize,
      total,
      diseaseList,
      primaryDepts,
      selectedPrimaryId,
      selectPrimary,
      handlePageChange
    }
  }
}
</script>

<style lang="scss" scoped>
.disease-page {
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
}

.disease-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  
  .disease-item {
    display: flex;
    padding: 20px;
    background: white;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    .disease-icon {
      width: 60px;
      height: 60px;
      background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 28px;
      margin-right: 15px;
      flex-shrink: 0;
    }
    
    .disease-info {
      flex: 1;
      
      h3 {
        color: #333;
        font-size: 16px;
        margin-bottom: 8px;
        font-weight: 600;
      }
      
      p {
        color: #666;
        font-size: 13px;
        line-height: 1.6;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        min-height: 42px;
        margin-bottom: 10px;
      }
      
      .disease-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        
        span {
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
