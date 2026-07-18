<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <Header />
    
    <!-- 轮播图 -->
    <div class="banner">
      <div class="banner-container">
        <el-carousel height="400px" indicator-position="outside">
          <el-carousel-item v-for="(img, index) in bannerImages" :key="index">
            <img :src="img" alt="banner" />
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content container">
      <!-- 快捷入口 -->
      <div class="quick-entry card">
        <div class="entry-grid">
          <router-link to="/hospital" class="entry-item">
            <div class="entry-icon blue">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <span>找医院</span>
          </router-link>
          <router-link to="/doctor" class="entry-item">
            <div class="entry-icon green">
              <el-icon><User /></el-icon>
            </div>
            <span>找医生</span>
          </router-link>
          <router-link to="/doctor" class="entry-item">
            <div class="entry-icon orange">
              <el-icon><Calendar /></el-icon>
            </div>
            <span>预约挂号</span>
          </router-link>
          <router-link to="/article" class="entry-item">
            <div class="entry-icon purple">
              <el-icon><Document /></el-icon>
            </div>
            <span>健康科普</span>
          </router-link>
          <router-link to="/disease" class="entry-item">
            <div class="entry-icon red">
              <el-icon><FirstAidKit /></el-icon>
            </div>
            <span>疾病百科</span>
          </router-link>
        </div>
      </div>
      
      <!-- 热门医院 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">热门医院</h2>
          <router-link to="/hospital" class="more-link">查看更多</router-link>
        </div>
        <div class="hospital-grid">
          <router-link 
            v-for="hospital in hotHospitals" 
            :key="hospital.id" 
            :to="`/hospital/${hospital.id}`"
            class="hospital-item"
          >
            <div class="hospital-img">
              <img :src="hospital.image || '/img/hospital_default.jpg'" :alt="hospital.name" />
            </div>
            <div class="hospital-info">
              <h3>{{ hospital.name }}</h3>
              <p class="hospital-address">{{ hospital.address }}</p>
              <span class="level-tag">{{ hospital.level }}</span>
            </div>
          </router-link>
        </div>
      </section>
      
      <!-- 热门医生 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">推荐医生</h2>
          <router-link to="/doctor" class="more-link">查看更多</router-link>
        </div>
        <div class="doctor-grid">
          <router-link 
            v-for="doctor in hotDoctors" 
            :key="doctor.id" 
            :to="`/doctor/${doctor.id}`"
            class="doctor-item"
          >
            <img :src="doctor.avatar || '/img/doctor_default.jpg'" :alt="doctor.name" class="doctor-avatar" />
            <h3>{{ doctor.name }}</h3>
            <p class="doctor-title">{{ doctor.title }}</p>
            <p class="doctor-dept">{{ doctor.hospitalName }} - {{ doctor.departmentName }}</p>
          </router-link>
        </div>
      </section>
      
      <!-- 热门疾病 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">常见疾病</h2>
          <router-link to="/disease" class="more-link">查看更多</router-link>
        </div>
        <div class="disease-grid">
          <router-link 
            v-for="disease in hotDiseases" 
            :key="disease.id" 
            :to="`/disease/${disease.id}`"
            class="disease-item"
          >
            <div class="disease-icon">
              <el-icon><FirstAidKit /></el-icon>
            </div>
            <div class="disease-info">
              <h4>{{ disease.name }}</h4>
              <p>{{ disease.description }}</p>
            </div>
          </router-link>
        </div>
      </section>
      
      <!-- 健康科普 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">健康科普</h2>
          <router-link to="/article" class="more-link">查看更多</router-link>
        </div>
        <div class="article-list">
          <router-link 
            v-for="article in hotArticles" 
            :key="article.id" 
            :to="`/article/${article.id}`"
            class="article-item"
          >
            <img :src="article.image || '/img/article_default.jpg'" :alt="article.title" />
            <div class="article-content">
              <h3>{{ article.title }}</h3>
              <p>{{ article.summary }}</p>
              <div class="article-meta">
                <span>{{ article.author }}</span>
                <span>{{ article.views }} 阅读</span>
              </div>
            </div>
          </router-link>
        </div>
      </section>
    </div>
    
    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getIndexData } from '@/api/home'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { OfficeBuilding, User, Calendar, Document, FirstAidKit } from '@element-plus/icons-vue'

export default {
  name: 'Home',
  components: { Header, Footer, OfficeBuilding, User, Calendar, Document, FirstAidKit },
  setup() {
    const bannerImages = ref([
      '/img/banner_01.jpeg',
      '/img/banner_02.jpeg',
      '/img/banner_03.jpeg',
      '/img/banner_04.png',
      '/img/banner_05.png'
    ])
    
    const hotHospitals = ref([])
    const hotDoctors = ref([])
    const hotDiseases = ref([])
    const hotArticles = ref([])
    
    const loadData = async () => {
      try {
        const res = await getIndexData()
        hotHospitals.value = res.data.hospitals || []
        hotDoctors.value = res.data.doctors || []
        hotDiseases.value = res.data.diseases || []
        hotArticles.value = res.data.articles || []
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      bannerImages,
      hotHospitals,
      hotDoctors,
      hotDiseases,
      hotArticles
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.banner {
  background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
  padding: 20px 0;
  
  .banner-container {
    width: 1200px;
    margin: 0 auto;
    border-radius: 8px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.main-content {
  padding: 30px 0;
  
  .section {
    background: white;
    border-radius: 8px;
    padding: 25px;
    margin-bottom: 25px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #e3f2fd;
  }
  
  .section-title {
    font-size: 20px;
    font-weight: bold;
    color: #1e88e5;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      width: 4px;
      height: 20px;
      background-color: #1e88e5;
      margin-right: 10px;
      border-radius: 2px;
    }
  }
  
  .more-link {
    color: #1e88e5;
    font-size: 14px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 快捷入口
.quick-entry {
  .entry-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20px;
  }
  
  .entry-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    border-radius: 8px;
    transition: all 0.3s;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
    }
    
    .entry-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      font-size: 28px;
      color: white;
      
      &.blue { background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%); }
      &.green { background: linear-gradient(135deg, #4caf50 0%, #81c784 100%); }
      &.orange { background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%); }
      &.purple { background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%); }
      &.red { background: linear-gradient(135deg, #f44336 0%, #e57373 100%); }
    }
    
    span {
      font-size: 16px;
      color: #333;
      font-weight: 500;
    }
  }
}

// 医院网格
.hospital-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  
  .hospital-item {
    background: #fafbfc;
    border-radius: 8px;
    padding: 15px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    .hospital-img {
      width: 100%;
      height: 120px;
      border-radius: 6px;
      overflow: hidden;
      margin-bottom: 12px;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .hospital-info {
      text-align: center;
      
      h3 {
        font-size: 16px;
        color: #333;
        margin-bottom: 6px;
      }
      
      .hospital-address {
        font-size: 13px;
        color: #666;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .level-tag {
        display: inline-block;
        background: #e3f2fd;
        color: #1e88e5;
        padding: 3px 8px;
        border-radius: 3px;
        font-size: 12px;
      }
    }
  }
}

// 医生网格
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  
  .doctor-item {
    text-align: center;
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    .doctor-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
      margin-bottom: 10px;
      border: 3px solid #e8eef3;
    }
    
    h3 {
      font-size: 15px;
      color: #333;
      margin-bottom: 4px;
    }
    
    .doctor-title {
      color: #1e88e5;
      font-size: 13px;
      margin-bottom: 4px;
    }
    
    .doctor-dept {
      color: #666;
      font-size: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 疾病网格
.disease-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  
  .disease-item {
    display: flex;
    align-items: center;
    padding: 15px;
    background: #fafbfc;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    min-width: 0; // 防止flex子元素溢出
    
    &:hover {
      background: #e3f2fd;
      border-color: #1e88e5;
    }
    
    .disease-icon {
      width: 45px;
      height: 45px;
      background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      margin-right: 12px;
      flex-shrink: 0;
    }
    
    .disease-info {
      flex: 1;
      min-width: 0; // 防止flex子元素溢出
      
      h4 {
        color: #333;
        font-size: 14px;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      p {
        color: #999;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 100%;
      }
    }
  }
}

// 文章列表
.article-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  
  .article-item {
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
      width: 120px;
      height: 100px;
      object-fit: cover;
      border-radius: 6px;
      margin-right: 15px;
      flex-shrink: 0;
    }
    
    .article-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      h3 {
        color: #333;
        font-size: 17px;
        margin-bottom: 8px;
      }
      
      p {
        color: #666;
        font-size: 14px;
        line-height: 1.6;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .article-meta {
        display: flex;
        gap: 20px;
        color: #999;
        font-size: 13px;
        margin-top: 8px;
      }
    }
  }
}
</style>
