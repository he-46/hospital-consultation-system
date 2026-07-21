<template>
  <div class="forgot-page">
    <div class="forgot-container">
      <div class="forgot-header">
        <h1>健康之路在线医疗</h1>
        <p>忘记密码</p>
      </div>
      
      <el-steps :active="step" align-center class="steps">
        <el-step title="验证手机号" />
        <el-step title="输入验证码" />
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
      
      <!-- 步骤2: 输入验证码 -->
      <div v-if="step === 1" class="step-content">
        <div class="verified-tip">
          <el-icon><CircleCheck /></el-icon>
          手机号 <span>{{ phoneForm.phone }}</span> 已验证
        </div>
        <el-form ref="codeFormRef" :model="codeForm" :rules="codeRules">
          <el-form-item prop="code">
            <el-input 
              v-model="codeForm.code" 
              placeholder="请输入短信验证码"
              prefix-icon="Key"
              size="large"
              maxlength="6"
            />
          </el-form-item>
          <el-form-item>
            <div class="code-actions">
              <el-button 
                size="large" 
                :disabled="countdown > 0 || sendingCode"
                :loading="sendingCode"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}秒后可重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              class="submit-btn"
              @click="handleCheckCode"
            >
              下一步
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤3: 设置新密码 -->
      <div v-if="step === 2" class="step-content">
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
      
      <!-- 步骤4: 完成 -->
      <div v-if="step === 3" class="step-content">
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
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { verifyPhone, sendCode, checkCode, resetPassword } from '@/api/user'

export default {
  name: 'ForgotPassword',
  components: { CircleCheck },
  setup() {
    const router = useRouter()
    const step = ref(0)
    const loading = ref(false)
    const countdown = ref(0)
    let countdownTimer = null
    
    const phoneFormRef = ref(null)
    const codeFormRef = ref(null)
    const passwordFormRef = ref(null)
    
    const phoneForm = reactive({
      phone: ''
    })
    
    const codeForm = reactive({
      code: ''
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
    
    const codeRules = {
      code: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码为6位数字', trigger: 'blur' }
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
    
    // 发送验证码中
    const sendingCode = ref(false)
    
    // 验证手机号
    const handleVerifyPhone = async () => {
      if (!phoneFormRef.value) return
      
      await phoneFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await verifyPhone(phoneForm.phone)
            step.value = 1
            // 自动发送验证码（等待完成）
            await handleSendCode()
          } catch (error) {
            console.error(error)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    // 发送验证码
    const handleSendCode = async () => {
      // 防止重复点击
      if (sendingCode.value || countdown.value > 0) return
      
      sendingCode.value = true
      try {
        await sendCode(phoneForm.phone)
        ElMessage.success('验证码已发送，请注意查收')
        // 开始60秒倒计时
        countdown.value = 60
        countdownTimer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(countdownTimer)
          }
        }, 1000)
      } catch (error) {
        console.error(error)
        ElMessage.error(error.message || '验证码发送失败')
      } finally {
        sendingCode.value = false
      }
    }
    
    // 验证验证码
    const handleCheckCode = async () => {
      if (!codeFormRef.value) return
      
      await codeFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await checkCode({
              phone: phoneForm.phone,
              code: codeForm.code
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
    
    // 重置密码
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
            step.value = 3
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
    
    // 组件卸载时清除定时器
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })
    
    return {
      step,
      loading,
      countdown,
      phoneFormRef,
      codeFormRef,
      passwordFormRef,
      phoneForm,
      codeForm,
      passwordForm,
      phoneRules,
      codeRules,
      passwordRules,
      handleVerifyPhone,
      handleSendCode,
      handleCheckCode,
      handleResetPassword,
      goLogin
    }
  }
}
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.forgot-container {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 450px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 24px;
    color: #333;
    margin-bottom: 8px;
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
  min-height: 200px;
}

.verified-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  margin-bottom: 20px;
  font-size: 14px;
  
  span {
    font-weight: bold;
  }
}

.code-actions {
  display: flex;
  gap: 10px;
  
  .el-button {
    flex: 1;
  }
}

.success-tip {
  text-align: center;
  padding: 40px 0;
  
  .success-icon {
    font-size: 60px;
    color: #67c23a;
    margin-bottom: 20px;
  }
  
  h3 {
    font-size: 20px;
    color: #333;
    margin-bottom: 10px;
  }
  
  p {
    color: #666;
  }
}

.submit-btn {
  width: 100%;
}

.back-link {
  text-align: center;
  margin-top: 20px;
  
  a {
    color: #409eff;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
