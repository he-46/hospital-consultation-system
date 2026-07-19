<template>
  <div class="patient-selector">
    <div class="patient-list">
      <div
        v-for="member in members"
        :key="member.id"
        class="patient-card"
        :class="{ selected: selectedId === member.id }"
        @click="selectMember(member)"
      >
        <div class="card-name">{{ member.name }}</div>
        <div class="card-id">{{ maskIdCard(member.idCard) }}</div>
        <div class="card-phone">{{ member.phone }}</div>
        <div v-if="selectedId === member.id" class="card-check">&#10003;</div>
      </div>

      <div class="patient-card add-card" @click="dialogVisible = true">
        <div class="add-icon">+</div>
        <div class="add-text">新增就诊人</div>
      </div>
    </div>

    <div v-if="members.length === 0 && !loading" class="empty-hint">
      暂无就诊人，请点击"新增就诊人"添加
    </div>

    <el-dialog v-model="dialogVisible" title="新增就诊人" width="480px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="dialogFormRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="请选择出生日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="关系" prop="relation">
          <el-select v-model="form.relation" placeholder="请选择关系" style="width: 100%">
            <el-option label="本人" value="本人" />
            <el-option label="配偶" value="配偶" />
            <el-option label="父母" value="父母" />
            <el-option label="子女" value="子女" />
            <el-option label="兄弟姐妹" value="兄弟姐妹" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFamilyMembers, addFamilyMember } from '@/api/family-member'

const emit = defineEmits(['select'])

const members = ref([])
const selectedId = ref(null)
const dialogVisible = ref(false)
const dialogFormRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  name: '',
  gender: 1,
  birthday: '',
  phone: '',
  idCard: '',
  relation: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  relation: [{ required: true, message: '请选择关系', trigger: 'change' }]
}

const maskIdCard = (idCard) => {
  if (!idCard || idCard.length < 10) return idCard || ''
  return idCard.substring(0, 6) + '****' + idCard.substring(idCard.length - 4)
}

const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await getFamilyMembers()
    members.value = res.data || []
  } catch (e) {
    console.error('加载就诊人失败', e)
  } finally {
    loading.value = false
  }
}

const selectMember = (member) => {
  selectedId.value = member.id
  emit('select', member)
}

const handleAdd = async () => {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.name,
      gender: form.gender,
      birthday: form.birthday,
      phone: form.phone,
      idCard: form.idCard,
      relation: form.relation
    }
    const res = await addFamilyMember(payload)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    // reset form
    form.name = ''
    form.gender = 1
    form.birthday = ''
    form.phone = ''
    form.idCard = ''
    form.relation = ''
    await fetchMembers()
    // auto select the new member
    if (res.data) {
      selectedId.value = res.data.id
      emit('select', res.data)
    }
  } catch (e) {
    console.error('添加就诊人失败', e)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchMembers()
})
</script>

<style lang="scss" scoped>
.patient-selector {
  width: 100%;
}

.patient-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.patient-card {
  width: 160px;
  padding: 14px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;

  &:hover { border-color: #1e88e5; }

  &.selected {
    border-color: #1e88e5;
    background: #e3f2fd;
  }

  .card-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
  }

  .card-id {
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
    letter-spacing: 1px;
  }

  .card-phone {
    font-size: 13px;
    color: #666;
  }

  .card-check {
    position: absolute;
    top: 6px;
    right: 10px;
    font-size: 18px;
    color: #1e88e5;
    font-weight: bold;
  }
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-style: dashed;

  .add-icon {
    font-size: 28px;
    color: #1e88e5;
    margin-bottom: 4px;
    line-height: 1;
  }

  .add-text {
    font-size: 13px;
    color: #1e88e5;
  }
}

.empty-hint {
  color: #999;
  font-size: 13px;
  margin-top: 4px;
}
</style>
