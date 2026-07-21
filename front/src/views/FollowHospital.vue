<template>
  <div class="follow-page">
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
        <h2 class="page-title">关注的医院</h2>
        <div v-loading="loading">
          <el-empty v-if="!loading && list.length === 0" description="暂未关注任何医院" />
          <div v-else class="follow-grid">
            <div v-for="item in list" :key="item.id" class="follow-card" @click="$router.push('/hospital/' + item.followId)">
              <el-image :src="item.hospitalImage" fit="cover" class="card-img" style="height:150px">
                <template #error><div class="img-placeholder">暂无图片</div></template>
              </el-image>
              <div class="card-body">
                <h4>{{ item.hospitalName }} <el-tag size="small">{{ item.hospitalLevel }}</el-tag></h4>
                <p class="intro">{{ item.hospitalIntro || '暂无介绍' }}</p>
                <div class="card-footer">
                  <span class="time">{{ item.createTime }}</span>
                  <el-button type="danger" size="small" link @click.stop="handleUnfollow(item.id)">取消关注</el-button>
                </div>
              </div>
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
import { ref, onMounted } from 'vue'
import { getFollows, delFollow } from '@/api/follow'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'


const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getFollows({ page: currentPage.value, size: pageSize.value, followType: 1 })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleUnfollow = async (id) => {
  try {
    await ElMessageBox.confirm('确定取消关注吗？', '提示', { type: 'warning' })
    await delFollow(id)
    ElMessage.success('已取消关注')
    loadList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.follow-page { min-height: 100vh; background: #f5f7fa; }
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
.page-title { font-size: 18px; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #e3f2fd; }
.follow-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.follow-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; transition: box-shadow .2s; }
.follow-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.1); }
.card-body { padding: 14px; }
.card-body h4 { margin: 0 0 8px; font-size: 16px; }
.intro { color: #666; font-size: 13px; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.time { font-size: 12px; color: #999; }
.img-placeholder { width: 100%; height: 150px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; color: #999; font-size: 14px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
</style>
