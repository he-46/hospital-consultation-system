<template>
  <div class="disease-detail-page">
    <Header />
    <div class="main-content container">
      <div v-if="disease.id" class="disease-content card">
        <h1 class="title">{{ disease.name }}</h1>
        <div v-if="disease.alias" class="alias">别名: {{ disease.alias }}</div>

        <div class="section">
          <h2>疾病概述</h2>
          <p>{{ disease.description }}</p>
        </div>

        <div class="section">
          <h2>发病部位</h2>
          <p>{{ disease.location || '暂无信息' }}</p>
        </div>

        <div class="section">
          <h2>常见症状</h2>
          <p>{{ disease.symptoms || '暂无信息' }}</p>
        </div>

        <div class="section">
          <h2>治疗方法</h2>
          <p>{{ disease.treatment || '暂无信息' }}</p>
        </div>

        <div class="section">
          <h2>临床检查</h2>
          <p>{{ disease.examinations || '暂无信息' }}</p>
        </div>

        <div class="section">
          <h2>治疗周期</h2>
          <p>{{ disease.treatmentPeriod || '暂无信息' }}</p>
        </div>

        <div class="section">
          <h2>治愈率</h2>
          <p>{{ disease.cureRate || '暂无信息' }}</p>
        </div>

        <div class="action">
          <el-button :type="isFollowed ? 'default' : 'primary'" @click="handleFollow">
            {{ isFollowed ? '已关注' : '关注疾病' }}
          </el-button>
          <el-button v-if="isFollowed" type="danger" @click="handleUnfollow">取消关注</el-button>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDiseaseDetail } from '@/api/disease'
import { addFollow, checkFollow, unfollowByTarget } from '@/api/follow'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'DiseaseDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const disease = ref({})
    const isFollowed = ref(false)

    const loadData = async () => {
      try {
        const res = await getDiseaseDetail(route.params.id)
        disease.value = res.data || {}
      } catch (error) {
        console.error(error)
      }
    }

    const loadFollowStatus = async () => {
      if (!localStorage.getItem('token')) return
      try {
        const res = await checkFollow({ followType: 3, followId: route.params.id })
        isFollowed.value = res.data === true
      } catch (err) {
        console.error(err)
      }
    }

    const handleFollow = async () => {
      if (!localStorage.getItem('token')) {
        router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
        return
      }
      if (isFollowed.value) return // already followed, use unfollow button
      await addFollow({ followType: 3, followId: route.params.id })
      ElMessage.success('关注成功')
      isFollowed.value = true
      disease.value.followCount = (disease.value.followCount || 0) + 1
    }

    const handleUnfollow = async () => {
      await unfollowByTarget(3, route.params.id)
      ElMessage.success('已取消关注')
      isFollowed.value = false
      if (disease.value.followCount > 0) disease.value.followCount--
    }

    onMounted(() => {
      loadData()
      loadFollowStatus()
    })

    return { disease, isFollowed, handleFollow, handleUnfollow }
  }
}
</script>

<style lang="scss" scoped>
.disease-detail-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }

.disease-content {
  .title { font-size: 24px; color: #333; text-align: center; margin-bottom: 10px; }
  .alias { text-align: center; color: #999; font-size: 14px; margin-bottom: 30px; }

  .section {
    margin-bottom: 25px; padding-bottom: 20px; border-bottom: 1px solid #f0f0f0;
    &:last-of-type { border-bottom: none; }
    h2 { font-size: 18px; color: #1e88e5; margin-bottom: 12px; }
    p { color: #666; line-height: 1.8; font-size: 15px; }
  }

  .action { text-align: center; padding-top: 20px; display: flex; gap: 12px; justify-content: center; }
}
</style>
