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
            <h1>
              {{ doctor.name }}
              <span v-if="doctor.onlineStatus === 1" class="online-badge">在线</span>
              <span v-else class="offline-badge">离线</span>
              <button :class="['follow-btn', isFollowed ? 'followed' : 'follow']" @click="toggleFollow">
                {{ isFollowed ? '已关注(' + followCount + ')' : '关注' }}
              </button>
            </h1>
            <div class="title-tag">{{ doctor.title }}</div>
            <p class="hospital-dept">
              {{ doctor.hospitalName }} - {{ doctor.departmentName }}
            </p>
            <div class="stats">
              <span><strong>{{ doctor.consultCount }}</strong> 接诊量</span>
              <span><strong>{{ doctor.followCount }}</strong> 关注</span>
              <span><strong>{{ doctor.rating }}</strong> 评分</span>
            </div>
            <div class="price-info">
              <span class="price-item">
                挂号费: <strong>{{ doctor.registrationPrice }}</strong>
              </span>
              <span class="price-item">
                咨询费: <strong>{{ doctor.price }}</strong>
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
          <h2 class="section-title">出诊时间（未来7天）</h2>
          <el-table :data="schedules" stripe empty-text="暂无排班信息">
            <el-table-column prop="scheduleDate" label="日期" width="150" />
            <el-table-column prop="timeSlot" label="时间段" />
            <el-table-column prop="remainCount" label="剩余号源">
              <template #default="{ row }">
                <span :class="{ 'no-remain': row.remainCount === 0 }">
                  {{ row.remainCount > 0 ? row.remainCount + '个' : '已满' }}
                </span>
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

        <div class="section card">
          <h2 class="section-title">患者评价</h2>
          <div v-if="reviews.length > 0" class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <span class="review-user">{{ review.userName }}</span>
                <el-rate v-model="review.rating" disabled size="small" />
                <span class="review-time">{{ formatTime(review.createTime) }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <p v-else class="empty-text">暂无评价</p>

          <el-pagination
            v-if="reviewTotal > 0"
            class="pagination"
            background
            layout="prev, pager, next"
            :total="reviewTotal"
            :page-size="reviewPageSize"
            :current-page="reviewPageNum"
            @current-change="handleReviewPageChange"
          />
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getDoctorDetail, getDoctorSchedule, getDoctorReviews } from '@/api/doctor'
import { addFollow, delFollow, checkFollow, getFollowRecordId } from '@/api/follow'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'DoctorDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const doctor = ref({})
    const schedules = ref([])
    const reviews = ref([])
    const reviewTotal = ref(0)
    const reviewPageNum = ref(1)
    const reviewPageSize = ref(5)

    const isFollowed = ref(false)
    const followCount = ref(0)
    let followRecordId = null

    const expertiseList = computed(() => {
      if (!doctor.value.expertise) return []
      return doctor.value.expertise.split(/[,，]/).filter(Boolean)
    })

    const loadData = async () => {
      const doctorId = route.params.id
      try {
        const res = await getDoctorDetail(doctorId)
        doctor.value = res.data.doctor || {}
        followCount.value = doctor.value.followCount || 0
      } catch (error) {
        console.error(error)
      }
    }

    const loadSchedules = async () => {
      try {
        const res = await getDoctorSchedule(route.params.id)
        schedules.value = res.data || []
      } catch (error) {
        console.error(error)
      }
    }

    const loadReviews = async () => {
      try {
        const res = await getDoctorReviews(route.params.id, {
          pageNum: reviewPageNum.value,
          pageSize: reviewPageSize.value
        })
        reviews.value = res.data.records || []
        reviewTotal.value = res.data.total || 0
      } catch (error) {
        console.error(error)
      }
    }

    const formatTime = (timeStr) => {
      if (!timeStr) return ''
      const d = new Date(timeStr)
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }

    const handleReviewPageChange = (page) => {
      reviewPageNum.value = page
      loadReviews()
    }

    const checkFollowStatus = async () => {
      const token = localStorage.getItem('token')
      if (!token) return
      try {
        const res = await checkFollow({ followType: 2, followId: doctor.value.id })
        isFollowed.value = res.data === true || (res.data && res.data.flag === true)
      } catch (e) { /* 未登录或接口异常时保持未关注状态 */ }
    }

    const toggleFollow = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        return
      }
      try {
        if (isFollowed.value) {
          const res = await getFollowRecordId(2, doctor.value.id)
          followRecordId = res.data
          if (followRecordId) {
            await delFollow(followRecordId)
            isFollowed.value = false
            followCount.value = Math.max(0, followCount.value - 1)
            ElMessage.success('已取消关注')
          }
        } else {
          await addFollow({ followType: 2, followId: doctor.value.id })
          isFollowed.value = true
          followCount.value++
          ElMessage.success('关注成功')
        }
      } catch (e) {
        ElMessage.error('操作失败')
      }
    }

    onMounted(async () => {
      await loadData()
      loadSchedules()
      loadReviews()
      checkFollowStatus()
    })

    return {
      doctor, schedules, reviews,
      reviewTotal, reviewPageNum, reviewPageSize,
      expertiseList, formatTime, handleReviewPageChange,
      isFollowed, followCount, toggleFollow
    }
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

    h1 { font-size: 24px; color: #333; margin-bottom: 10px; display: flex; align-items: center; gap: 10px; }
    .online-badge { display: inline-block; background: #4caf50; color: white; padding: 3px 10px; border-radius: 10px; font-size: 12px; }
    .offline-badge { display: inline-block; background: #999; color: white; padding: 3px 10px; border-radius: 10px; font-size: 12px; }

    .follow-btn {
      padding: 8px 20px;
      border-radius: 20px;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      border: none;
      outline: none;
      margin-left: auto;

      &.follow {
        background-color: #1e88e5;
        color: white;
        &:hover { background-color: #1565c0; }
      }
      &.followed {
        background-color: #f5f7fa;
        color: #999;
        border: 1px solid #e8eef3;
      }
    }

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

.no-remain { color: #f56c6c; }
.empty-text { color: #999; text-align: center; padding: 20px 0; }

.review-list {
  .review-item {
    padding: 15px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child { border-bottom: none; }

    .review-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .review-user { color: #333; font-weight: bold; font-size: 14px; }
      .review-time { color: #999; font-size: 12px; }
    }

    .review-content { color: #666; font-size: 14px; line-height: 1.6; }
  }
}

.pagination { justify-content: center; margin-top: 20px; }
</style>
