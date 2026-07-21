<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>健康之路在线医疗</h1>
        <p>用户注册</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <!-- 头像上传 -->
        <div class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <p class="avatar-tip">点击上传头像</p>
        </div>
        
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
        
        <el-form-item prop="verifyCode">
          <div class="code-input">
            <el-input 
              v-model="form.verifyCode" 
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              maxlength="6"
            />
            <el-button 
              size="large" 
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="gender">
          <el-radio-group v-model="form.gender" size="large">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择生日"
            size="large"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
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
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { register, sendRegisterCode } from '@/api/user'
import { uploadAvatar } from '@/api/file'

export default {
  name: 'Register',
  components: { Plus },
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const countdown = ref(0)
    let countdownTimer = null
    const avatarUrl = ref('')
    const avatarFile = ref(null)
    
    const form = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      verifyCode: '',
      realName: '',
      gender: 1,
      birthday: '',
      email: '',
      avatar: ''
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
      ],
      verifyCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码为6位数字', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ]
    }
    
    const handleAvatarChange = (file) => {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.raw.size / 1024 / 1024 < 2
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return
      }
      
      avatarUrl.value = URL.createObjectURL(file.raw)
      avatarFile.value = file.raw
    }
    
    const handleSendCode = async () => {
      if (!form.phone) {
        ElMessage.warning('请先输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(form.phone)) {
        ElMessage.warning('请输入正确的手机号')
        return
      }
      
      try {
        await sendRegisterCode(form.phone)
        ElMessage.success('验证码已发送')
        countdown.value = 60
        countdownTimer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(countdownTimer)
          }
        }, 1000)
      } catch (error) {
        console.error(error)
      }
    }
    
    const handleRegister = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            // 如果有头像文件，先上传
            if (avatarFile.value) {
              const uploadRes = await uploadAvatar(avatarFile.value)
              form.avatar = uploadRes.data.url
            }
            
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
    
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })
    
    return {
      form,
      rules,
      loading,
      countdown,
      formRef,
      avatarUrl,
      handleAvatarChange,
      handleSendCode,
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
  width: 480px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 25px;
  
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

.avatar-upload {
  text-align: center;
  margin-bottom: 25px;
  
  .avatar-uploader {
    display: inline-block;
    
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 50%;
      cursor: pointer;
      overflow: hidden;
      transition: border-color 0.3s;
      
      &:hover {
        border-color: #1e88e5;
      }
    }
    
    .avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
    }
    
    .avatar-uploader-icon {
      width: 80px;
      height: 80px;
      font-size: 28px;
      color: #8c939d;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .avatar-tip {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
  }
}

.code-input {
  display: flex;
  gap: 10px;
  
  .el-input {
    flex: 1;
  }
  
  .el-button {
    width: 120px;
  }
}

.register-btn {
  width: 100%;
}

.register-footer {
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
