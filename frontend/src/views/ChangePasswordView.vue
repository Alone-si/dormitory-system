<template>
  <main class="password-page">
    <section class="password-card">
      <div class="security-icon">
        <ShieldCheck :size="32" />
      </div>

      <header>
        <h1>请先修改初始密码</h1>
        <p>为了保护账号安全，设置新密码后才能继续使用系统。</p>
      </header>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="form.currentPassword"
            type="password"
            show-password
            size="large"
            placeholder="请输入当前密码"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            size="large"
            placeholder="至少8位，不能与当前密码相同"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            size="large"
            placeholder="请再次输入新密码"
            :prefix-icon="Lock"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button class="submit-button" type="primary" size="large" :loading="loading" @click="submit">
          保存新密码
        </el-button>
      </el-form>

      <button v-if="canSkip" class="skip-button" type="button" @click="skipPasswordChange">
        暂时跳过，仅只读浏览
      </button>

      <button class="logout-button" type="button" @click="logout">
        <LogOut :size="16" />
        退出登录
      </button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, LogOut, ShieldCheck } from 'lucide-vue-next'
import { changePassword } from '../api/user'
import { authApi } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const canSkip = computed(() =>
  userStore.role === 'STUDENT' && Boolean(userStore.userInfo?.mustChangePassword)
)
const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const rules: FormRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 128, message: '新密码长度必须为8至128位', trigger: 'blur' }
  ],
  confirmPassword: [{
    validator: (_rule, value, callback) => {
      if (!value) callback(new Error('请再次输入新密码'))
      else if (value !== form.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    },
    trigger: 'blur'
  }]
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const response = await changePassword(form)
    if (response.code !== 200) {
      ElMessage.error(response.message || '密码修改失败')
      return
    }

    userStore.clearAuth()
    await router.replace('/login')
    ElMessage.success('密码已修改，请重新登录')
  } catch (error: any) {
    if (error !== false) ElMessage.error(error?.response?.data?.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}

const skipPasswordChange = async () => {
  await router.replace('/student/home')
  ElMessage.warning('当前为只读访客模式，修改密码后即可操作')
}

const logout = async () => {
  try {
    await authApi.logout()
  } finally {
    userStore.clearAuth()
    await router.replace('/login')
  }
}
</script>

<style scoped src="./ChangePasswordView.css"></style>
