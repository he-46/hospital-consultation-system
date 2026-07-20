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
              <span>{{ hospital.departmentCount }}个科室</span>
              <span>{{ hospital.doctorCount }}名医生</span>
              <span>{{ hospital.followCount }}人关注</span>
            </div>
            <p class="address">
              <el-icon><Location /></el-icon>
              {{ hospital.address }}
            </p>
            <p class="phone">
              <el-icon><Phone /></el-icon>
              {{ hospital.phone }}
            </p>
            <p class="intro">{{ hospital.intro }}</p>
          </div>
          <div class="action-btns">
            <el-button :type="isFollow ? 'default' : 'primary'" @click="handleFollow">
              {{ isFollow ? '已关注' : '+ 关注' }}
            </el-button>
          </div>
        </div>

        <div class="section card">
          <h2 class="section-title">医院科室</h2>
          <div class="departments">
            <div v-for="dept in departments" :key="dept.id" class="dept-card">
              <h3>{{ dept.name }}</h3>
              <p>{{ dept.description }}</p>
            </div>
          </div>
        </div>

        <div class="section card">
          <h2 class="section-title">推荐医生</h2>
          <div class="doctor-list">
            <router-link v-for="doctor in doctors" :key="doctor.id" :to="`/doctor/${doctor.id}`" class="doctor-item">
              <img :src="doctor.avatar || '/img/doctor_default.jpg'" />
              <h3>{{ doctor.name }}</h3>
              <p>{{ doctor.title }}</p>
              <p class="dept">{{ doctor.departmentName }}</p>
            </router-link>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getHospitalDetail } from '@/api/hospital'
import { addFollow, delFollow, getFollows, checkFollow } from '@/api/follow'
import { ElMessage } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'HospitalDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const hospital = ref({})
    const departments = ref([])
    const doctors = ref([])
    const isFollow = ref(false)
    const myFollowId = ref(null)

    const loadData = async () => {
      try {
        const res = await getHospitalDetail(route.params.id)
        const data = res.data
        hospital.value = data.hospital || {}
        departments.value = data.departments || []
        doctors.value = data.doctors || []
      } catch (error) {
        console.error(error)
      }
    }

    const loadFollowStatus = async () => {
      if (!localStorage.getItem('token')) return
      try {
        const res = await checkFollow({ followType: 1, followId: route.params.id })
        isFollow.value = res.data === true
      } catch (err) {
        console.error(err)
      }
    }

    const loadMyFollowId = async () => {
      try {
        const res = await getFollows({ page: 1, size: 100, followType: 1 })
        const records = res.data.records || []
        const match = records.find(r => String(r.followId) === String(route.params.id))
        myFollowId.value = match ? match.id : null
      } catch (err) {
        console.error(err)
      }
    }

    const handleFollow = async () => {
      if (isFollow.value) {
        // 取消关注
        if (!myFollowId.value) {
          ElMessage.error('未找到关注记录')
          return
        }
        await delFollow(myFollowId.value)
        ElMessage.success('已取消关注')
        isFollow.value = false
        myFollowId.value = null
        if (hospital.value.followCount > 0) hospital.value.followCount--
      } else {
        // 关注
        await addFollow({ followType: 1, followId: route.params.id })
        ElMessage.success('关注成功')
        isFollow.value = true
        hospital.value.followCount = (hospital.value.followCount || 0) + 1
        loadMyFollowId()
      }
    }

    onMounted(() => {
      loadData()
      loadFollowStatus()
      loadMyFollowId()
    })

    return { hospital, departments, doctors, isFollow, handleFollow }
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
      display: flex; gap: 15px; margin-bottom: 15px; font-size: 14px; color: #666;
      .level-tag { background: #1e88e5; color: white; padding: 4px 12px; border-radius: 4px; }
    }
    .address, .phone {
      display: flex; align-items: center; gap: 8px; color: #666; font-size: 14px; margin-bottom: 8px;
    }
    .intro { color: #999; font-size: 14px; line-height: 1.8; margin-top: 15px; }
  }

  .action-btns {
    display: flex; flex-direction: column; gap: 10px; padding-top: 10px;
    .el-button { min-width: 100px; }
  }
}

.section {
  margin-bottom: 20px;
  .section-title { font-size: 18px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #e3f2fd; }
}

.departments {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;
  .dept-card { padding: 15px; background: #fafbfc; border-radius: 8px; border: 1px solid #e8eef3;
    h3 { font-size: 15px; color: #333; margin-bottom: 8px; }
    p { font-size: 13px; color: #999; }
  }
}

.doctor-list {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px;
  .doctor-item {
    text-align: center; padding: 15px; background: #fafbfc; border-radius: 8px; border: 1px solid #e8eef3; transition: all 0.3s;
    &:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15); border-color: #1e88e5; }
    img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; margin-bottom: 10px; }
    h3 { font-size: 15px; color: #333; margin-bottom: 5px; }
    p { font-size: 13px; color: #1e88e5; }
    .dept { color: #999; }
  }
}
</style>
