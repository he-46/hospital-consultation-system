<template>
  <div class="article-detail-page">
    <Header />
    <div class="main-content container">
      <div v-if="article.id" class="article-content card">
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <span>作者: {{ article.author }}</span>
          <span>阅读: {{ article.views }}</span>
          <span>{{ formatTime(article.publishTime) }}</span>
        </div>
        <div class="article-body" v-html="article.content"></div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'ArticleDetail',
  components: { Header, Footer },
  setup() {
    const route = useRoute()
    const article = ref({})
    
    const loadData = async () => {
      try {
        const res = await getArticleDetail(route.params.id)
        article.value = res.data || {}
      } catch (error) {
        console.error(error)
      }
    }
    
    const formatTime = (time) => {
      if (!time) return ''
      return time.substring(0, 10)
    }
    
    onMounted(() => {
      loadData()
    })
    
    return { article, formatTime }
  }
}
</script>

<style lang="scss" scoped>
.article-detail-page { min-height: 100vh; background: #f5f7fa; }
.main-content { padding: 30px 0; }

.article-content {
  .title {
    font-size: 24px;
    color: #333;
    text-align: center;
    margin-bottom: 20px;
  }
  
  .meta {
    text-align: center;
    color: #999;
    font-size: 14px;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    
    span { margin: 0 15px; }
  }
  
  .article-body {
    line-height: 1.8;
    color: #666;
    font-size: 15px;
    
    :deep(h2) { font-size: 20px; color: #333; margin: 20px 0 10px; }
    :deep(h3) { font-size: 17px; color: #333; margin: 15px 0 10px; }
    :deep(p) { margin-bottom: 15px; }
    :deep(ul), :deep(ol) { padding-left: 30px; margin-bottom: 15px; }
    :deep(li) { margin-bottom: 8px; }
  }
}
</style>
