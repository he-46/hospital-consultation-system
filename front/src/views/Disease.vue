<template>
  <div class="disease-page">
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
import { ref, computed, onMounted } from 'vue'
import { getDiseaseList } from '@/api/disease'
import { getDepartmentTree } from '@/api/department'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { FirstAidKit } from '@element-plus/icons-vue'

export default {
  name: 'Disease',
  components: { Header, Footer, FirstAidKit },
  setup() {
    const pageNum = ref(1)
    const pageSize = ref(12)
    const total = ref(0)
    const diseaseList = ref([])
    const deptTree = ref([])
    const selectedPrimary = ref(0)
    const selectedSecondary = ref(null)

    const primaryDepartments = computed(() => {
      const list = [{ id: 0, name: '全部' }]
      deptTree.value.forEach(d => list.push({ id: d.id, name: d.name }))
      return list
    })

    const secondaryDepartments = computed(() => {
      if (!selectedPrimary.value || selectedPrimary.value === 0) return []
      const dept = deptTree.value.find(d => d.id === selectedPrimary.value)
      return dept ? (dept.children || []) : []
    })

    const selectPrimary = (id) => {
      selectedPrimary.value = id
      selectedSecondary.value = null
      pageNum.value = 1
      loadDiseases()
    }

    const selectSecondary = (id) => {
      selectedSecondary.value = id
      pageNum.value = 1
      loadDiseases()
    }

    const loadDiseases = async () => {
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
        const res = await getDiseaseList(params)
        diseaseList.value = res.data.records || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error(error)
      }
    }

    const handlePageChange = (page) => {
      pageNum.value = page
      loadDiseases()
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
      loadDiseases()
    })

    return {
      pageNum,
      pageSize,
      total,
      diseaseList,
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
.disease-page {
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

  .primary-departments {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 25px;

    span {
      padding: 10px 20px;
      background: #f5f7fa;
      border-radius: 20px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
      color: #333;
      border: 2px solid transparent;

      &:hover { background: #e3f2fd; }
      &.active { background: #1e88e5; color: white; border-color: #1565c0; }
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
      color: #666;
      border: 1px solid #e8eef3;

      &:hover { background: #e3f2fd; border-color: #1e88e5; color: #1e88e5; }
      &.active { background: #1e88e5; color: white; border-color: #1565c0; }
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
