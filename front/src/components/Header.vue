<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <el-icon><Plus /></el-icon>
        <span>健康之路</span>
      </div>
      
      <nav class="nav">
        <router-link to="/home" class="nav-item" :class="{ active: $route.path === '/home' }">首页</router-link>
        <router-link to="/hospital" class="nav-item" :class="{ active: $route.path.startsWith('/hospital') }">找医院</router-link>
        <router-link to="/doctor" class="nav-item" :class="{ active: $route.path.startsWith('/doctor') }">找医生</router-link>
        <router-link to="/article" class="nav-item" :class="{ active: $route.path.startsWith('/article') }">健康科普</router-link>
        <router-link to="/disease" class="nav-item" :class="{ active: $route.path.startsWith('/disease') }">疾病百科</router-link>
      </nav>
      
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索医院、医生、疾病"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="auth">
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="login-btn">登录</router-link>
          <router-link to="/register" class="register-btn">注册</router-link>
        </template>
        <template v-else>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar">
                {{ userInfo.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="personal">个人中心</el-dropdown-item>
                <el-dropdown-item command="appointments">我的挂号</el-dropdown-item>
                <el-dropdown-item command="consults">我的咨询</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/user'

export default {
  name: 'Header',
  setup() {
    const router = useRouter()
    const searchKeyword = ref('')
    const userInfo = ref({})
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })
    
    onMounted(() => {
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        userInfo.value = JSON.parse(stored)
      }
    })
    
    const handleSearch = () => {
      if (searchKeyword.value.trim()) {
        router.push({ path: '/search', query: { keyword: searchKeyword.value } })
      }
    }
    
    const handleCommand = async (command) => {
      switch (command) {
        case 'personal':
          router.push('/personal')
          break
        case 'appointments':
          router.push('/personal?tab=appointments')
          break
        case 'consults':
          router.push('/personal?tab=consults')
          break
        case 'logout':
          try {
            await logout()
          } catch (e) {
            console.log(e)
          }
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/home')
          break
      }
    }
    
    return {
      searchKeyword,
      userInfo,
      isLoggedIn,
      handleSearch,
      handleCommand
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
  box-shadow: 0 2px 10px rgba(30, 136, 229, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
  
  .header-content {
    width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    padding: 12px 0;
    gap: 30px;
  }
  
  .logo {
    display: flex;
    align-items: center;
    color: white;
    font-size: 22px;
    font-weight: bold;
    gap: 8px;
    
    .el-icon {
      font-size: 28px;
    }
  }
  
  .nav {
    display: flex;
    gap: 5px;
    
    .nav-item {
      color: white;
      text-decoration: none;
      padding: 8px 18px;
      border-radius: 5px;
      font-size: 15px;
      transition: background-color 0.3s;
      
      &:hover, &.active {
        background-color: #1565c0;
      }
    }
  }
  
  .search-box {
    flex: 1;
    max-width: 350px;
    
    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 4px 0 0 4px;
      }
    }
    
    :deep(.el-button) {
      border-radius: 0 4px 4px 0;
    }
  }
  
  .auth {
    display: flex;
    gap: 12px;
    align-items: center;
    
    .login-btn {
      color: white;
      text-decoration: none;
      padding: 6px 16px;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 4px;
      font-size: 14px;
      transition: all 0.3s;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
    
    .register-btn {
      color: #1e88e5;
      background-color: white;
      text-decoration: none;
      padding: 6px 16px;
      border-radius: 4px;
      font-size: 14px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f0f0f0;
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: white;
      padding: 4px 12px;
      border-radius: 4px;
      background-color: rgba(255, 255, 255, 0.1);
      
      .username {
        font-size: 14px;
      }
    }
  }
}
</style>
