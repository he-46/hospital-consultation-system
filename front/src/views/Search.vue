<template>
  <div class="search-page">
    <Header />
    <div class="main-content container">
      <div class="search-box card">
        <el-input
          v-model="keyword"
          placeholder="搜索医院、医生、疾病"
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      
      <el-tabs v-model="activeTab" class="result-tabs">
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
          <div v-else class="empty">未找到相关医院</div>
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
          <div v-else class="empty">未找到相关医生</div>
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
              </div>
            </router-link>
          </div>
          <div v-else class="empty">未找到相关疾病</div>
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
          <div v-else class="empty">未找到相关文章</div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchHospitals } from '@/api/hospital'
import { searchDoctors } from '@/api/doctor'
import { searchDiseases } from '@/api/disease'
import { searchArticles } from '@/api/article'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Search',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const keyword = ref('')
    const activeTab = ref('hospital')
    const hospitalList = ref([])
    const doctorList = ref([])
    const diseaseList = ref([])
    const articleList = ref([])
    
    const doSearch = async () => {
      if (!keyword.value.trim()) return
      
      try {
        const promises = [
          searchHospitals({ keyword: keyword.value, pageNum: 1, pageSize: 10 }),
          searchDoctors({ keyword: keyword.value, pageNum: 1, pageSize: 10 }),
          searchDiseases({ keyword: keyword.value, pageNum: 1, pageSize: 10 }),
          searchArticles({ keyword: keyword.value, pageNum: 1, pageSize: 10 })
        ]
        const [hospitals, doctors, diseases, articles] = await Promise.all(promises)
        hospitalList.value = hospitals.data?.records || []
        doctorList.value = doctors.data?.records || []
        diseaseList.value = diseases.data?.records || []
        articleList.value = articles.data?.records || []
      } catch (error) {
        console.error(error)
      }
    }
    
    const handleSearch = () => {
      doSearch()
    }
    
    onMounted(() => {
      keyword.value = route.query.keyword || ''
      if (keyword.value) {
        doSearch()
      }
    })
    
    watch(() => route.query.keyword, (newVal) => {
      keyword.value = newVal || ''
      if (keyword.value) {
        doSearch()
      }
    })
    
    return {
      keyword,
      activeTab,
      hospitalList,
      doctorList,
      diseaseList,
      articleList,
      handleSearch
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
</style>
