<template>
  <div class="article-page">
    <Header />
    <div class="main-content container">
      <div class="article-list">
        <router-link 
          v-for="article in articleList" 
          :key="article.id" 
          :to="`/article/${article.id}`"
          class="article-item"
        >
          <img :src="article.image || '/img/article_default.jpg'" :alt="article.title" />
          <div class="article-content">
            <h3>{{ article.title }}</h3>
            <p>{{ article.summary }}</p>
            <div class="article-meta">
              <span>作者: {{ article.author }}</span>
              <span>{{ article.views }} 阅读</span>
              <span>{{ formatTime(article.publishTime) }}</span>
            </div>
          </div>
        </router-link>
      </div>
      
      <div v-if="articleList.length === 0" class="empty-state">
        <p>暂无文章数据</p>
      </div>
      
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
      />
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getArticleList } from '@/api/article'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Article',
  components: { Header, Footer },
  setup() {
    const pageNum = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const articleList = ref([])
    
    const loadArticles = async () => {
      try {
        const res = await getArticleList({
          pageNum: pageNum.value,
          pageSize: pageSize.value
        })
        articleList.value = res.data.records || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error(error)
      }
    }
    
    const handlePageChange = (page) => {
      pageNum.value = page
      loadArticles()
    }
    
    const formatTime = (time) => {
      if (!time) return ''
      return time.substring(0, 10)
    }
    
    onMounted(() => {
      loadArticles()
    })
    
    return {
      pageNum,
      pageSize,
      total,
      articleList,
      handlePageChange,
      formatTime
    }
  }
}
</script>

<style lang="scss" scoped>
.article-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding: 30px 0;
}

.filter-section {
  margin-bottom: 25px;
  
  .filter-title {
    font-size: 16px;
    color: #333;
    margin-bottom: 15px;
    font-weight: bold;
  }
  
  .primary-departments {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    span {
      padding: 10px 20px;
      background: #f5f7fa;
      border-radius: 20px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
      
      &:hover { background: #e3f2fd; }
      &.active { background: #1e88e5; color: white; }
    }
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  
  .article-item {
    display: flex;
    padding: 20px;
    background: white;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #e8eef3;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(30, 136, 229, 0.15);
      border-color: #1e88e5;
    }
    
    img {
      width: 150px;
      height: 120px;
      object-fit: cover;
      border-radius: 6px;
      margin-right: 20px;
      flex-shrink: 0;
    }
    
    .article-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      h3 {
        color: #333;
        font-size: 18px;
        margin-bottom: 10px;
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
        flex: 1;
      }
      
      .article-meta {
        display: flex;
        gap: 20px;
        color: #999;
        font-size: 13px;
        margin-top: 10px;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 8px;
  color: #999;
}

.pagination {
  justify-content: center;
  margin-top: 30px;
}
</style>
