<template>
  <div class="hospital-detail-page">
    <Header />
    <div class="main-content container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/hospital' }">找医院</el-breadcrumb-item>
        <el-breadcrumb-item>{{ hospital.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="hospital.id" class="detail-content">
        <div class="hospital-header card">
          <div class="hospital-img">
            <img :src="hospital.image || '/img/hospital_default.jpg'" :alt="hospital.name" />
          </div>
          <div class="hospital-info">
            <h1>{{ hospital.name }}</h1>
            <div class="tags">
              <span class="level-tag">{{ hospital.level }}</span>
              <span>{{ departmentList.length }}个科室</span>
              <span>{{ hospital.doctorCount || doctorList.length }}名医生</span>
              <span>{{ hospital.followCount || 0 }}人关注</span>
            </div>
            <p class="address">
              <el-icon><Location /></el-icon>
              {{ hospital.address }}
            </p>
            <p class="phone" v-if="hospital.phone">
              <el-icon><Phone /></el-icon>
              {{ hospital.phone }}
            </p>
            <p class="intro">{{ hospital.intro }}</p>
            <el-button
              type="primary"
              :icon="isFollowed ? 'el-icon-check' : 'el-icon-plus'"
              round
              @click="toggleFollow"
            >
              {{ isFollowed ? '已关注' : '关注医院' }}
            </el-button>
          </div>
        </div>

        <div class="section card">
          <h2 class="section-title">医院科室</h2>
          <div class="departments">
            <div
              v-for="dept in departmentList"
              :key="dept.id"
              class="dept-card"
            >
              <h3>{{ dept.name }}</h3>
              <p>{{ dept.description || '暂无介绍' }}</p>
            </div>
          </div>
          <div v-if="departmentList.length === 0" class="empty-section">
            暂无科室数据
          </div>
        </div>

        <div class="section card">
          <h2 class="section-title">推荐医生（{{ doctorList.length }}人）</h2>
          <div class="doctor-list">
            <router-link
              v-for="doctor in doctorList"
              :key="doctor.id"
              :to="`/doctor/${doctor.id}`"
              class="doctor-item"
            >
              <img :src="doctor.avatar || '/img/doctor_default.jpg'" />
              <h3>{{ doctor.name }}</h3>
              <p>{{ doctor.title }}</p>
              <p class="dept">{{ doctor.departmentName }}</p>
            </router-link>
          </div>
          <div v-if="doctorList.length === 0" class="empty-section">
            暂无医生数据
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <p>医院信息不存在</p>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHospitalById, getHospitalDetail } from '@/api/hospital'
import { addFollow, delFollow, getFollowRecordId } from '@/api/follow'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'HospitalDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const hospital = ref({})
    const departmentList = ref([])
    const doctorList = ref([])
    const isFollowed = ref(false)
    const followRecordId = ref(0)
    const loading = ref(true)

    const loadData = async () => {
      loading.value = true
      try {
        const hospitalId = route.params.id

        // Use the new RESTful API
        const res = await getHospitalById(hospitalId)
        const data = res.data
        hospital.value = data.hospital || {}
        departmentList.value = data.departments || []
        doctorList.value = data.doctors || []

        // 检查关注状态
        checkFollowStatus(hospitalId)
      } catch (error) {
        console.error('加载医院详情失败:', error)
        // Fallback to old API
        try {
          const res = await getHospitalDetail(route.params.id)
          const data = res.data
          hospital.value = data.hospital || {}
          departmentList.value = data.departments || []
          doctorList.value = data.doctors || []
        } catch (e) {
          console.error('Fallback also failed:', e)
        }
      } finally {
        loading.value = false
      }
    }

    const checkFollowStatus = async (hospitalId) => {
      try {
        const res = await getFollowRecordId(1, hospitalId)
        const id = res.data || 0
        isFollowed.value = id > 0
        followRecordId.value = id
      } catch (e) {
        // 未登录或接口错误，默认为未关注
        isFollowed.value = false
        followRecordId.value = 0
      }
    }

    const toggleFollow = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        return
      }

      try {
        if (isFollowed.value && followRecordId.value > 0) {
          await delFollow(followRecordId.value)
          isFollowed.value = false
          followRecordId.value = 0
          hospital.value.followCount = Math.max(0, (hospital.value.followCount || 1) - 1)
          ElMessage.success('已取消关注')
        } else {
          await addFollow({ followType: 1, followId: hospital.value.id })
          isFollowed.value = true
          // 重新获取关注记录ID
          const res = await getFollowRecordId(1, hospital.value.id)
          followRecordId.value = res.data || 0
          hospital.value.followCount = (hospital.value.followCount || 0) + 1
          ElMessage.success('关注成功')
        }
      } catch (error) {
        console.error('操作失败:', error)
        if (error.response?.data?.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('操作失败，请重试')
        }
      }
    }

    onMounted(() => {
      loadData()
    })

    return { hospital, departmentList, doctorList, isFollowed, followRecordId, loading, toggleFollow }
  }
}
</script>

<style lang="scss" scoped>
.hospital-detail-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 20px 0 40px; }

.hospital-header {
  display: flex;
  gap: 30px;
  margin: 20px 0;

  .hospital-img {
    width: 300px;
    height: 200px;
    flex-shrink: 0;

    img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
  }

  .hospital-info {
    flex: 1;

    h1 { font-size: 24px; color: #333; margin-bottom: 15px; }

    .tags {
      display: flex;
      gap: 15px;
      margin-bottom: 15px;
      font-size: 14px;
      color: #666;

      .level-tag {
        background: #1e88e5;
        color: white;
        padding: 4px 12px;
        border-radius: 4px;
      }
    }

    .address, .phone {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
      font-size: 14px;
      margin-bottom: 8px;
    }

    .intro {
      color: #999;
      font-size: 14px;
      line-height: 1.8;
      margin-top: 15px;
      margin-bottom: 15px;
    }
  }
}

.section {
  margin-bottom: 20px;

  .section-title {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #e3f2fd;
  }
}

.departments {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;

  .dept-card {
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    border: 1px solid #e8eef3;

    h3 { font-size: 15px; color: #333; margin-bottom: 8px; }
    p { font-size: 13px; color: #999; }
  }
}

.doctor-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;

  .doctor-item {
    text-align: center;
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    border: 1px solid #e8eef3;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }

    img {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
      margin-bottom: 10px;
    }

    h3 { font-size: 15px; color: #333; margin-bottom: 5px; }
    p { font-size: 13px; color: #1e88e5; }
    .dept { color: #999; }
  }
}

.empty-section {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 8px;
  color: #999;
}
</style>
