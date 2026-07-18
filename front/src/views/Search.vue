<template>
  <div class="search-page">
    <Header />
    <div class="main-content container">
      <div class="search-box card">
        <el-input
          v-model="keyword"
          placeholder="搜索医院、医生、疾病、文章"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <el-tabs v-model="activeTab" class="result-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="医院" name="hospital">
          <div v-if="hospitalList.length > 0" class="result-list">
            <router-link
              v-for="item in hospitalList"
              :key="item.id"
              :to="`/hospital/${item.id}`"
              class="result-item hospital"
            >
              <img :src="item.image || '/img/hospital_default.jpg'" />
              <div class="result-info">
                <h3>{{ item.name }}</h3>
                <p>{{ item.address }}</p>
                <span class="level-tag">{{ item.level }}</span>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">
            <el-empty description="未找到相关医院" />
          </div>
          <el-pagination
            v-if="hospitalTotal > 0"
            background
            layout="prev, pager, next"
            :total="hospitalTotal"
            :page-size="pageSize"
            :current-page="hospitalPage"
            @current-change="(p) => loadTabData('hospital', p)"
          />
        </el-tab-pane>

        <el-tab-pane label="医生" name="doctor">
          <div v-if="doctorList.length > 0" class="result-list">
            <router-link
              v-for="item in doctorList"
              :key="item.id"
              :to="`/doctor/${item.id}`"
              class="result-item doctor"
            >
              <img :src="item.avatar || '/img/doctor_default.jpg'" />
              <div class="result-info">
                <h3>{{ item.name }}</h3>
                <p>{{ item.hospitalName }} - {{ item.departmentName }}</p>
                <span class="title-tag">{{ item.title }}</span>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">
            <el-empty description="未找到相关医生" />
          </div>
          <el-pagination
            v-if="doctorTotal > 0"
            background
            layout="prev, pager, next"
            :total="doctorTotal"
            :page-size="pageSize"
            :current-page="doctorPage"
            @current-change="(p) => loadTabData('doctor', p)"
          />
        </el-tab-pane>

        <el-tab-pane label="疾病" name="disease">
          <div v-if="diseaseList.length > 0" class="result-list">
            <router-link
              v-for="item in diseaseList"
              :key="item.id"
              :to="`/disease/${item.id}`"
              class="result-item disease"
            >
              <div class="disease-icon">
                <el-icon><FirstAidKit /></el-icon>
              </div>
              <div class="result-info">
                <h3>{{ item.name }}</h3>
                <p>{{ item.description }}</p>
                <span>{{ item.departmentName || '' }}</span>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">
            <el-empty description="未找到相关疾病" />
          </div>
          <el-pagination
            v-if="diseaseTotal > 0"
            background
            layout="prev, pager, next"
            :total="diseaseTotal"
            :page-size="pageSize"
            :current-page="diseasePage"
            @current-change="(p) => loadTabData('disease', p)"
          />
        </el-tab-pane>

        <el-tab-pane label="文章" name="article">
          <div v-if="articleList.length > 0" class="result-list">
            <router-link
              v-for="item in articleList"
              :key="item.id"
              :to="`/article/${item.id}`"
              class="result-item article"
            >
              <img :src="item.image || '/img/article_default.jpg'" />
              <div class="result-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
                <span>{{ item.views }} 阅读</span>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">
            <el-empty description="未找到相关文章" />
          </div>
          <el-pagination
            v-if="articleTotal > 0"
            background
            layout="prev, pager, next"
            :total="articleTotal"
            :page-size="pageSize"
            :current-page="articlePage"
            @current-change="(p) => loadTabData('article', p)"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { unifiedSearch } from '@/api/search'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Search',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const keyword = ref('')
    const activeTab = ref('hospital')
    const pageSize = 10

    // 各 tab 的分页和数据
    const hospitalList = ref([])
    const hospitalPage = ref(1)
    const hospitalTotal = ref(0)

    const doctorList = ref([])
    const doctorPage = ref(1)
    const doctorTotal = ref(0)

    const diseaseList = ref([])
    const diseasePage = ref(1)
    const diseaseTotal = ref(0)

    const articleList = ref([])
    const articlePage = ref(1)
    const articleTotal = ref(0)

    const loadTabData = async (type, page = 1) => {
      if (!keyword.value.trim()) return

      try {
        const res = await unifiedSearch({
          keyword: keyword.value.trim(),
          type: type,
          pageNum: page,
          pageSize: pageSize
        })
        const data = res.data

        if (type === 'hospital') {
          const pageData = data.hospitals || {}
          hospitalList.value = pageData.records || []
          hospitalTotal.value = pageData.total || 0
          hospitalPage.value = page
        } else if (type === 'doctor') {
          const pageData = data.doctors || {}
          doctorList.value = pageData.records || []
          doctorTotal.value = pageData.total || 0
          doctorPage.value = page
        } else if (type === 'disease') {
          const pageData = data.diseases || {}
          diseaseList.value = pageData.records || []
          diseaseTotal.value = pageData.total || 0
          diseasePage.value = page
        } else if (type === 'article') {
          const pageData = data.articles || {}
          articleList.value = pageData.records || []
          articleTotal.value = pageData.total || 0
          articlePage.value = page
        }
      } catch (error) {
        console.error(`搜索${type}失败:`, error)
      }
    }

    // 全量搜索（用于初始加载或切换关键词）
    const doSearchAll = async () => {
      if (!keyword.value.trim()) return

      try {
        const res = await unifiedSearch({
          keyword: keyword.value.trim(),
          type: 'all',
          pageNum: 1,
          pageSize: pageSize
        })
        const data = res.data

        const hospitals = data.hospitals || {}
        hospitalList.value = hospitals.records || []
        hospitalTotal.value = hospitals.total || 0
        hospitalPage.value = 1

        const doctors = data.doctors || {}
        doctorList.value = doctors.records || []
        doctorTotal.value = doctors.total || 0
        doctorPage.value = 1

        const diseases = data.diseases || {}
        diseaseList.value = diseases.records || []
        diseaseTotal.value = diseases.total || 0
        diseasePage.value = 1

        const articles = data.articles || {}
        articleList.value = articles.records || []
        articleTotal.value = articles.total || 0
        articlePage.value = 1
      } catch (error) {
        console.error('搜索失败:', error)
      }
    }

    const handleSearch = () => {
      doSearchAll()
    }

    const handleTabClick = () => {
      // 切换 tab 时如果该 tab 还没有数据，加载它
      const tabDataMap = {
        hospital: hospitalList,
        doctor: doctorList,
        disease: diseaseList,
        article: articleList
      }
      if (tabDataMap[activeTab.value]?.value?.length === 0 && keyword.value.trim()) {
        loadTabData(activeTab.value, 1)
      }
    }

    onMounted(() => {
      keyword.value = route.query.keyword || ''
      if (keyword.value) {
        doSearchAll()
      }
    })

    watch(() => route.query.keyword, (newVal) => {
      keyword.value = newVal || ''
      if (keyword.value) {
        doSearchAll()
      }
    })

    return {
      keyword,
      activeTab,
      hospitalList, hospitalPage, hospitalTotal,
      doctorList, doctorPage, doctorTotal,
      diseaseList, diseasePage, diseaseTotal,
      articleList, articlePage, articleTotal,
      handleSearch,
      handleTabClick,
      loadTabData
    }
  }
}
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding: 30px 0;
}

.search-box {
  margin-bottom: 25px;
}

.result-tabs {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 15px;

  .result-item {
    display: flex;
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;

    &:hover {
      background: #e3f2fd;
      border-color: #1e88e5;
    }

    img {
      width: 100px;
      height: 80px;
      object-fit: cover;
      border-radius: 6px;
      margin-right: 15px;
      flex-shrink: 0;
    }

    .disease-icon {
      width: 100px;
      height: 80px;
      background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 36px;
      margin-right: 15px;
      flex-shrink: 0;
    }

    .result-info {
      flex: 1;

      h3 {
        color: #333;
        font-size: 16px;
        margin-bottom: 8px;
      }

      p {
        color: #666;
        font-size: 13px;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      span {
        color: #999;
        font-size: 12px;
      }

      .level-tag, .title-tag {
        display: inline-block;
        background: #e3f2fd;
        color: #1e88e5;
        padding: 2px 8px;
        border-radius: 3px;
        font-size: 12px;
      }
    }
  }
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

:deep(.el-pagination) {
  justify-content: center;
  margin-top: 20px;
}
</style>
