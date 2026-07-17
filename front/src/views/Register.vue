<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>健康之路在线医疗</h1>
        <p>用户注册</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱(选填)"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名(选填)"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="register-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      email: '',
      realName: ''
    })
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ]
    }
    
    const handleRegister = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await register(form)
            ElMessage.success('注册成功，请登录')
            router.push('/login')
          } catch (error) {
            console.error(error)
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    return {
      form,
      rules,
      loading,
      formRef,
      handleRegister
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%);
  padding: 40px 0;
}

.register-container {
  width: 420px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
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

.register-form {
  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
  }
}

.register-footer {
  text-align: center;
  margin-top: 25px;
  padding-top: 25px;
  border-top: 1px solid #e8eef3;
  font-size: 14px;
  color: #666;
  
  a {
    color: #1e88e5;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
