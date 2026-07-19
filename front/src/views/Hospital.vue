<template>
  <div class="hospital-page">
    <Header />
    <div class="main-content container">
      <!-- 科室选择 -->
      <div class="section filter-section">
        <div class="section-title">选择科室</div>
        <div class="primary-departments">
          <span v-for="dept in primaryDepartments" :key="dept.id"
                :class="{active: selectedPrimary === dept.id}"
                @click="selectPrimary(dept.id)">
            {{ dept.name }}
          </span>
        </div>
        <div v-if="selectedPrimary && selectedPrimary !== 0 && secondaryDepartments.length > 0" class="secondary-departments">
          <span v-for="sub in secondaryDepartments" :key="sub.id"
                :class="{active: selectedSecondary === sub.id}"
                @click="selectSecondary(sub.id)">
            {{ sub.name }}
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
import { ref, computed, onMounted } from 'vue'
import { getHospitalList } from '@/api/hospital'
import { getDepartmentTree } from '@/api/department'
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
    const deptTree = ref([])
    const selectedPrimary = ref(0)
    const selectedSecondary = ref(null)

    // 一级科室：全部 + 从树中取顶层节点
    const primaryDepartments = computed(() => {
      const list = [{ id: 0, name: '全部' }]
      deptTree.value.forEach(d => list.push({ id: d.id, name: d.name }))
      return list
    })

    // 当前选中的一级科室下的二级科室
    const secondaryDepartments = computed(() => {
      if (!selectedPrimary.value || selectedPrimary.value === 0) return []
      const dept = deptTree.value.find(d => d.id === selectedPrimary.value)
      return dept ? (dept.children || []) : []
    })

    const selectPrimary = (id) => {
      selectedPrimary.value = id
      selectedSecondary.value = null
      pageNum.value = 1
      loadHospitals()
    }

    const selectSecondary = (id) => {
      selectedSecondary.value = id
      pageNum.value = 1
      loadHospitals()
    }

    const loadHospitals = async () => {
      try {
        const params = {
          pageNum: pageNum.value,
          pageSize: pageSize.value
        }
        // 选中科室时传 departmentId（后端自动展开一级科室的子科室）
        if (selectedSecondary.value) {
          params.departmentId = selectedSecondary.value
        } else if (selectedPrimary.value && selectedPrimary.value !== 0) {
          params.departmentId = selectedPrimary.value
        }
        const res = await getHospitalList(params)
        hospitalList.value = res.data.records || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error(error)
      }
    }

    const handlePageChange = (page) => {
      pageNum.value = page
      loadHospitals()
    }

    const loadDeptTree = async () => {
      try {
        const res = await getDepartmentTree()
        deptTree.value = res.data || []
      } catch (error) {
        console.error('加载科室树失败', error)
      }
    }

    onMounted(() => {
      loadDeptTree()
      loadHospitals()
    })

    return {
      pageNum,
      pageSize,
      total,
      hospitalList,
      primaryDepartments,
      secondaryDepartments,
      selectedPrimary,
      selectedSecondary,
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

.section {
  background: white;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e3f2fd;
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
