<template>
  <div class="reviews-page">
    <Header />
    <div class="container main-content">
      <div class="sidebar card">
        <div class="menu-group">
          <div class="menu-title">订单管理</div>
          <div class="menu-item" @click="$router.push('/personal?tab=appointments')">我的挂号</div>
          <div class="menu-item" @click="$router.push('/personal?tab=consults')">我的咨询</div>
        </div>
        <div class="menu-group">
          <div class="menu-title">我的</div>
          <div class="menu-item" @click="$router.push('/personal')">个人资料</div>
          <div class="menu-item" @click="$router.push('/follow/doctor')">关注的医生</div>
          <div class="menu-item" @click="$router.push('/follow/hospital')">关注的医院</div>
          <div class="menu-item" @click="$router.push('/follow/disease')">关注的疾病</div>
          <div class="menu-item" @click="$router.push('/my/review')">我的评价</div>
          <div class="menu-item" @click="$router.push('/feedback')">意见反馈</div>
        </div>
      </div>
      <div class="content-card">
        <div class="page-header">
          <h2 class="page-title">我的评价</h2>
          <el-button v-if="showReviewForm" type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
        </div>

        <!-- 评价表单（从挂号/咨询完成后跳转过来时显示） -->
        <div v-if="showReviewForm" class="review-form card">
          <h4>写评价</h4>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
            <el-form-item label="订单类型">
              <el-tag>{{ form.orderType === 1 ? '挂号' : '咨询' }}</el-tag>
            </el-form-item>
            <el-form-item label="评分" prop="score">
              <el-rate v-model="form.score" :max="5" show-score />
            </el-form-item>
            <el-form-item label="评价内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="3" placeholder="分享您的就医体验..." />
            </el-form-item>
          </el-form>
        </div>

        <!-- 评价列表 -->
        <div v-loading="loading">
          <el-empty v-if="!loading && list.length === 0 && !showReviewForm" description="暂无评价记录" />
          <div v-else-if="list.length > 0" class="review-list">
            <div v-for="item in list" :key="item.id" class="review-item" style="cursor:pointer" @click="$router.push('/doctor/' + item.doctorId)">
              <div class="review-header">
                <span class="order-type-tag">
                  <el-tag :type="item.orderType === 1 ? 'primary' : 'success'" size="small">
                    {{ item.orderType === 1 ? '挂号' : '咨询' }}
                  </el-tag>
                </span>
                <el-rate :model-value="item.rating" disabled show-score size="small" />
                <span class="time">{{ item.createTime }}</span>
              </div>
              <p class="review-content" v-if="item.content">{{ item.content }}</p>
              <p class="review-content empty" v-else>（无文字评价）</p>
            </div>
          </div>
          <div class="pagination-wrap" v-if="total > pageSize">
            <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize"
                           v-model:current-page="currentPage" @current-change="loadList" />
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitReview as submitReviewApi, getReviewList } from '@/api/review'
import { ElMessage } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const router = useRouter()

const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const submitting = ref(false)
const formRef = ref(null)

// 从路由 query 读取预填信息
const showReviewForm = computed(() => {
  return !!(route.query.orderType && route.query.orderId && route.query.doctorId)
})

const form = reactive({
  orderType: Number(route.query.orderType) || 1,
  orderId: route.query.orderId || '',   // 保持字符串，避免大数字精度丢失
  doctorId: route.query.doctorId || '', // 保持字符串
  score: 5,
  content: ''
})

const rules = {
  score: [{ required: true, message: '请打分', trigger: 'change' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getReviewList({ page: currentPage.value, size: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await submitReviewApi({
      orderType: form.orderType,
      orderId: form.orderId,
      doctorId: form.doctorId,
      score: form.score,
      content: form.content
    })
    if (res && res.code === 200) {
      ElMessage.success('评价提交成功')
      // 清除 query 并刷新列表
      router.replace({ query: {} })
      loadList()
    } else {
      ElMessage.error(res?.message || '提交失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || err.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.reviews-page { min-height: 100vh; background: #f5f7fa; }
.main-content { display: flex; gap: 20px; padding: 20px 0 40px; }
.sidebar {
  width: 240px; flex-shrink: 0; background: white; border-radius: 6px; padding: 20px 0;
  .menu-group { margin-bottom: 25px; }
  .menu-title {
    font-size: 16px; font-weight: bold; color: #333; margin-bottom: 12px; padding-left: 10px;
    border-left: 4px solid #1e88e5;
  }
  .menu-item {
    padding: 12px 15px; cursor: pointer; border-radius: 6px; font-size: 14px; color: #666;
    transition: all 0.3s;
    &:hover { background: #f0f7ff; color: #1e88e5; }
  }
}
.content-card { flex: 1; background: white; padding: 20px; border-radius: 6px; min-height: 400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 18px; margin: 0; padding-bottom: 10px; border-bottom: 2px solid #e3f2fd; }
.review-form { padding: 20px; margin-bottom: 24px; }
.review-form h4 { margin: 0 0 16px; font-size: 16px; }
.review-list { display: flex; flex-direction: column; gap: 16px; }
.review-item { padding: 16px; border: 1px solid #eee; border-radius: 8px; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.time { margin-left: auto; font-size: 12px; color: #999; }
.review-content { color: #333; font-size: 14px; margin: 8px 0 0; }
.review-content.empty { color: #ccc; font-style: italic; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
</style>
