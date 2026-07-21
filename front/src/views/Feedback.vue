<template>
  <div class="feedback-page">
    <Header />
    <div class="container">
      <div class="page-title">我的反馈</div>

      <!-- 提交反馈 -->
      <div class="card submit-section">
        <h3>提交反馈</h3>
        <el-form :model="form" label-width="80px">
          <el-form-item label="反馈类型">
            <el-select v-model="form.feedbackType" placeholder="请选择">
              <el-option label="系统问题" :value="1" />
              <el-option label="服务问题" :value="2" />
              <el-option label="医生问题" :value="3" />
              <el-option label="其他" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="反馈内容">
            <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入反馈内容" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 反馈列表 -->
      <div class="card list-section">
        <h3>反馈记录</h3>
        <div v-if="list.length === 0" class="empty">暂无反馈</div>
        <div v-for="item in list" :key="item.id" class="feedback-item">
          <div class="feedback-header">
            <el-tag :type="typeTag(item.feedbackType)">{{ typeLabel(item.feedbackType) }}</el-tag>
            <span class="time">{{ item.createTime }}</span>
            <el-tag v-if="item.status === 2" type="success" size="small">已处理</el-tag>
            <el-tag v-else type="warning" size="small">待处理</el-tag>
          </div>
          <div class="feedback-content">{{ item.content }}</div>
          <div v-if="item.replyContent" class="feedback-reply">
            <strong>管理员回复：</strong>{{ item.replyContent }}
          </div>
        </div>
        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/index'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'Feedback',
  components: { Header, Footer },
  setup() {
    const form = reactive({ feedbackType: null, content: '' })
    const list = ref([])
    const pageNum = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const typeLabel = (type) => {
      const map = { 1: '系统问题', 2: '服务问题', 3: '医生问题', 4: '其他' }
      return map[type] || '未知'
    }
    const typeTag = (type) => {
      const map = { 1: 'danger', 2: 'warning', 3: 'info', 4: '' }
      return map[type] || ''
    }

    const submitFeedback = async () => {
      if (!form.feedbackType) { ElMessage.warning('请选择反馈类型'); return }
      if (!form.content) { ElMessage.warning('请输入内容'); return }
      await request.post('/feedback/submit', {
        feedbackType: form.feedbackType,
        content: form.content
      })
      ElMessage.success('提交成功')
      form.content = ''
      form.feedbackType = null
      loadList()
    }

    const loadList = async () => {
      const res = await request.get('/feedback/myList', {
        params: { pageNum: pageNum.value, pageSize: pageSize.value }
      })
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }

    onMounted(() => loadList())

    return { form, list, pageNum, pageSize, total, typeLabel, typeTag, submitFeedback, loadList }
  }
}
</script>

<style scoped>
.feedback-page { min-height: 100vh; background: #f5f7fa; }
.container { width: 900px; margin: 0 auto; padding: 30px 0; }
.page-title { font-size: 24px; font-weight: bold; margin-bottom: 20px; }
.card { background: #fff; border-radius: 8px; padding: 25px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.card h3 { margin-bottom: 15px; font-size: 18px; }
.submit-section { max-width: 600px; }
.feedback-item { padding: 15px; border-bottom: 1px solid #eee; }
.feedback-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.feedback-header .time { color: #999; font-size: 13px; margin-left: auto; }
.feedback-content { color: #333; margin-bottom: 8px; }
.feedback-reply { background: #f0f9eb; padding: 10px; border-radius: 4px; color: #67c23a; font-size: 14px; }
.empty { text-align: center; color: #999; padding: 40px; }
</style>
