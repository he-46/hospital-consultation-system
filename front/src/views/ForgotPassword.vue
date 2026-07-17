<template>
  <div class="forgot-page">
    <div class="forgot-container">
      <div class="forgot-header">
        <h1>健康之路在线医疗</h1>
        <p>忘记密码</p>
      </div>
      
      <el-steps :active="step" align-center class="steps">
        <el-step title="验证手机号" />
        <el-step title="设置新密码" />
        <el-step title="完成" />
      </el-steps>
      
      <!-- 步骤1: 验证手机号 -->
      <div v-if="step === 0" class="step-content">
        <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules">
          <el-form-item prop="phone">
            <el-input 
              v-model="phoneForm.phone" 
              placeholder="请输入注册的手机号"
              prefix-icon="Phone"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              class="submit-btn"
              @click="handleVerifyPhone"
            >
              下一步
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤2: 设置新密码 -->
      <div v-if="step === 1" class="step-content">
        <div class="verified-tip">
          <el-icon><CircleCheck /></el-icon>
          手机号 <span>{{ phoneForm.phone }}</span> 验证成功
        </div>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
          <el-form-item prop="password">
            <el-input 
              v-model="passwordForm.password" 
              type="password" 
              placeholder="请输入新密码"
              prefix-icon="Lock"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请确认新密码"
              prefix-icon="Lock"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              class="submit-btn"
              @click="handleResetPassword"
            >
              确认重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤3: 完成 -->
      <div v-if="step === 2" class="step-content">
        <div class="success-tip">
          <el-icon class="success-icon"><CircleCheck /></el-icon>
          <h3>密码重置成功</h3>
          <p>请使用新密码登录</p>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          class="submit-btn"
          @click="goLogin"
        >
          去登录
        </el-button>
      </div>
      
      <div class="back-link">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { verifyPhone, resetPassword } from '@/api/user'

export default {
  name: 'ForgotPassword',
  setup() {
    const router = useRouter()
    const step = ref(0)
    const loading = ref(false)
    const phoneFormRef = ref(null)
    const passwordFormRef = ref(null)
    
    const phoneForm = reactive({
      phone: ''
    })
    
    const passwordForm = reactive({
      password: '',
      confirmPassword: ''
    })
    
    const phoneRules = {
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ]
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== passwordForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const passwordRules = {
      password: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }
    
    const handleVerifyPhone = async () => {
      if (!phoneFormRef.value) return
      
      await phoneFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await verifyPhone(phoneForm.phone)
            step.value = 1
          } catch (error) {
            console.error(error)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const handleResetPassword = async () => {
      if (!passwordFormRef.value) return
      
      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await resetPassword({
              phone: phoneForm.phone,
              password: passwordForm.password
            })
            step.value = 2
          } catch (error) {
            console.error(error)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const goLogin = () => {
      router.push('/login')
    }
    
    return {
      step,
      loading,
      phoneFormRef,
      passwordFormRef,
      phoneForm,
      passwordForm,
      phoneRules,
      passwordRules,
      handleVerifyPhone,
      handleResetPassword,
      goLogin
    }
  }
}
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
  padding: 40px 0;
}

.forgot-container {
  width: 450px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    color: #1e88e5;
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 10px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 150px;
}

.verified-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 15px;
  background: #f0f9ff;
  border-radius: 6px;
  color: #67c23a;
  margin-bottom: 20px;
  font-size: 14px;
  
  span {
    font-weight: bold;
  }
}

.success-tip {
  text-align: center;
  padding: 20px 0;
  
  .success-icon {
    font-size: 60px;
    color: #67c23a;
    margin-bottom: 15px;
  }
  
  h3 {
    color: #333;
    font-size: 18px;
    margin-bottom: 10px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.back-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e8eef3;
  
  a {
    color: #1e88e5;
    font-size: 14px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
