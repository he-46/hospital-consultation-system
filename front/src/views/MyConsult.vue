<template>
  <div class="personal-page">
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
          <h2 class="page-title">我的咨询</h2>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px" @change="loadConsultList">
            <el-option label="全部" :value="null" />
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="咨询中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </div>

        <el-table :data="consultList" stripe empty-text="暂无咨询记录" v-loading="loading">
          <el-table-column label="订单号" prop="orderNo" width="200" show-overflow-tooltip />
          <el-table-column label="医生" width="120">
            <template #default="{row}">{{ row.doctorName || '医生ID:' + row.doctorId }}</template>
          </el-table-column>
          <el-table-column label="医院" width="140" show-overflow-tooltip>
            <template #default="{row}">{{ row.hospitalName || '-' }}</template>
          </el-table-column>
          <el-table-column label="咨询人" prop="patientName" width="100" />
          <el-table-column label="金额(元)" width="90">
            <template #default="{row}">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <el-tag v-if="row.status === 1" type="warning">待支付</el-tag>
              <el-tag v-else-if="row.status === 2" type="success">已支付</el-tag>
              <el-tag v-else-if="row.status === 3" type="primary">咨询中</el-tag>
              <el-tag v-else-if="row.status === 4">已完成</el-tag>
              <el-tag v-else type="info">已取消</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === 1" type="primary" size="small" @click="handlePay(row)">支付</el-button>
              <el-button v-if="row.status === 1 || row.status === 2" type="warning" size="small" @click="handleCancel(row)">取消</el-button>
              <el-button v-if="row.status === 2" type="primary" size="small" @click="handleConfirm(row)">确认完成</el-button>
              <el-button v-if="row.status === 4" type="success" size="small" @click="handleReview(row)">评价</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap" v-if="total > 0">
          <el-pagination background layout="prev, pager, next"
                         :total="total" :page-size="pageSize"
                         v-model:current-page="currentPage"
                         @current-change="loadConsultList" />
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getConsultList, cancelConsult, payConsult, confirmConsult } from '@/api/consult'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const router = useRouter()

const consultList = ref([])
const loading = ref(false)
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadConsultList = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (filterStatus.value != null) params.status = filterStatus.value
    const res = await getConsultList(params)
    consultList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (err) {
    console.error('加载咨询列表失败', err)
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消这笔咨询订单吗？', '提示', { type: 'warning' })
    await cancelConsult(row.id)
    ElMessage.success('取消成功')
    loadConsultList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.message || '取消失败')
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认咨询已完成？确认后将可评价。', '提示', { confirmButtonText: '确认完成', cancelButtonText: '取消', type: 'info' })
    await confirmConsult(row.id)
    ElMessage.success('已确认完成')
    loadConsultList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.message || '操作失败')
  }
}

const handlePay = async (row) => {
  try {
    await payConsult(row.id, { payType: 1 })
    ElMessage.success('支付发起成功')
    router.push('/consult-pay/' + row.id)
  } catch (err) {
    ElMessage.error(err.message || '支付失败')
  }
}

const handleReview = (row) => {
  router.push({ path: '/my/review', query: { orderType: 2, orderId: row.id, doctorId: row.doctorId } })
}

onMounted(() => {
  loadConsultList()
})
</script>

<style lang="scss" scoped>
.personal-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.main-content {
  display: flex;
  gap: 20px;
  padding: 20px 0 40px;
}
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
.content-card {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 6px;
  min-height: 400px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  font-size: 18px;
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e3f2fd;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
