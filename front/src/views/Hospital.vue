<template>
  <div class="hospital-page">
    <Header />
    <div class="main-content container">
      <div class="filter-section card">
        <h3 class="filter-title">选择科室</h3>
        <div class="primary-departments">
          <span
            @click="selectAll"
            :class="{ active: selectedPrimaryId === null }"
          >
            全部
          </span>
          <span
            v-for="dept in primaryDepts"
            :key="dept.id"
            :class="{ active: selectedPrimaryId === dept.id }"
            @click="selectPrimary(dept)"
          >
            {{ dept.name }}
          </span>
        </div>
        <div v-if="secondaryDepts.length > 0" class="secondary-departments">
          <span
            @click="selectSecondary(null)"
            :class="{ active: selectedSecondaryId === null }"
          >
            全部
          </span>
          <span
            v-for="dept in secondaryDepts"
            :key="dept.id"
            :class="{ active: selectedSecondaryId === dept.id }"
            @click="selectSecondary(dept.id)"
          >
            {{ dept.name }}
          </span>
        </div>

        <!-- 等级筛选 -->
        <div class="level-filter">
          <span class="filter-label">医院等级：</span>
          <span
            @click="selectLevel(null)"
            :class="{ active: selectedLevel === null }"
          >
            全部
          </span>
          <span
            v-for="level in levelOptions"
            :key="level"
            :class="{ active: selectedLevel === level }"
            @click="selectLevel(level)"
          >
            {{ level }}
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
              <span class="follow-count">{{ hospital.followCount || 0 }}人关注</span>
            </div>
            <div v-if="hospital.departmentTags && hospital.departmentTags.length > 0" class="dept-tags">
              <span v-for="(tag, idx) in hospital.departmentTags.slice(0, 4)" :key="idx" class="dept-tag">
                {{ tag }}
              </span>
              <span v-if="hospital.departmentTags.length > 4" class="dept-tag more">
                +{{ hospital.departmentTags.length - 4 }}
              </span>
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
import { getHospitals } from '@/api/hospital'
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
    const primaryDepts = ref([])
    const secondaryDepts = ref([])
    const selectedPrimaryId = ref(null)
    const selectedSecondaryId = ref(null)
    const selectedLevel = ref(null)
    const levelOptions = ['三级甲等', '三级乙等', '二级甲等', '二级乙等', '一级甲等', '一级乙等']

    const loadHospitals = async () => {
      try {
        const params = {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          departmentId: selectedSecondaryId.value || undefined,
          level: selectedLevel.value || undefined
        }
        const res = await getHospitals(params)
        hospitalList.value = res.data.records || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error('加载医院列表失败:', error)
      }
    }

    const loadDepartmentTree = async () => {
      try {
        const res = await getDepartmentTree()
        primaryDepts.value = res.data || []
      } catch (error) {
        console.error('加载科室树失败:', error)
      }
    }

    const selectAll = () => {
      selectedPrimaryId.value = null
      selectedSecondaryId.value = null
      secondaryDepts.value = []
      pageNum.value = 1
      loadHospitals()
    }

    const selectPrimary = (dept) => {
      selectedPrimaryId.value = dept.id
      selectedSecondaryId.value = null
      pageNum.value = 1
      loadHospitals()
      secondaryDepts.value = dept.children || []
    }

    const selectSecondary = (id) => {
      selectedSecondaryId.value = id
      pageNum.value = 1
      loadHospitals()
    }

    const selectLevel = (level) => {
      selectedLevel.value = level
      pageNum.value = 1
      loadHospitals()
    }

    const handlePageChange = (page) => {
      pageNum.value = page
      loadHospitals()
    }

    onMounted(() => {
      loadHospitals()
      loadDepartmentTree()
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
      selectedLevel,
      levelOptions,
      selectAll,
      selectPrimary,
      selectSecondary,
      selectLevel,
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
    margin-bottom: 20px;

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

  .level-filter {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    padding-top: 15px;
    border-top: 1px solid #e8eef3;

    .filter-label {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }

    span {
      padding: 6px 14px;
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
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
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
        margin-bottom: 8px;

        .level-tag {
          background: #e3f2fd;
          color: #1e88e5;
          padding: 3px 8px;
          border-radius: 3px;
          font-size: 12px;
        }

        .follow-count {
          color: #999;
          font-size: 12px;
        }
      }

      .dept-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 5px;

        .dept-tag {
          background: #f0f9ff;
          color: #0288d1;
          padding: 2px 8px;
          border-radius: 3px;
          font-size: 11px;
          border: 1px solid #b3e5fc;

          &.more {
            background: #f5f5f5;
            color: #999;
            border: 1px solid #e0e0e0;
          }
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
