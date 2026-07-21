<template>
  <div class="message-page">
    <Header />
    <div class="container">
      <div class="page-title">我的消息</div>

      <div class="card">
        <el-radio-group v-model="filter" @change="loadList">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">未读</el-radio-button>
          <el-radio-button label="1">已读</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="list.length === 0" class="empty">暂无消息</div>
      <div v-for="item in list" :key="item.id" class="card message-item"
           :class="{ unread: item.isRead === 0 }"
           @click="handleClick(item)">
        <div class="message-title">
          <span v-if="item.isRead === 0" class="unread-dot"></span>
          <span v-else class="read-dot"></span>
          {{ item.title }}
          <span class="time">{{ item.createTime }}</span>
        </div>
        <div class="message-content">{{ item.content }}</div>
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
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import request from '@/api/index'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Messages',
  components: { Header, Footer },
  setup() {
    const list = ref([])
    const filter = ref('')
    const pageNum = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const loadList = async () => {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (filter.value !== '') params.isRead = filter.value
      const res = await request.get('/message/list', { params })
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }

    const markRead = async (id) => {
      await request.put(`/message/${id}/read`)
    }

    const handleClick = async (item) => {
      if (item.isRead === 0) {
        try {
          await markRead(item.id)
          item.isRead = 1
        } catch (e) {
          // 忽略
        }
      }
    }

    onMounted(() => loadList())

    return { list, filter, pageNum, pageSize, total, loadList, handleClick }
  }
}
</script>

<style scoped>
.message-page { min-height: 100vh; background: #f5f7fa; }
.container { width: 800px; margin: 0 auto; padding: 30px 0; }
.page-title { font-size: 24px; font-weight: bold; margin-bottom: 20px; }
.card { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 15px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.message-item { cursor: pointer; transition: background-color 0.3s; }
.message-item:hover { background: #fafbfc; }
.message-item.unread { border-left: 4px solid #1e88e5; background: #f0f7ff; }
.message-title { font-size: 16px; font-weight: bold; display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.message-title .time { color: #999; font-size: 13px; margin-left: auto; }
.message-content { color: #666; }
.unread-dot { width: 8px; height: 8px; background: #f44336; border-radius: 50%; flex-shrink: 0; }
.read-dot { width: 8px; height: 8px; background: transparent; border-radius: 50%; flex-shrink: 0; }
.empty { text-align: center; color: #999; padding: 60px; }
</style>
