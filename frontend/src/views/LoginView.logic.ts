import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Building2, Leaf, ShieldCheck } from 'lucide-vue-next'
import { authApi } from '../api/auth'
import { useUserStore } from '../stores/user'
import type { FormInstance, FormRules } from 'element-plus'

export function useLoginView() {
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await authApi.login(form)

      userStore.setToken(res.token)
      userStore.setUserInfo(res.user)

      ElMessage.success(`登录成功！欢迎 ${res.name}`)

      if (res.role === 'ADMIN') {
        await router.push('/admin/dashboard')
      } else if (res.role === 'STUDENT') {
        await router.push('/student/home')
      } else {
        console.error('未知角色:', res.role)
        ElMessage.error('角色信息异常')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
  return {
  Building2,
  form,
  formRef,
  handleLogin,
  Leaf,
  loading,
  Lock,
  rules,
  ShieldCheck,
  User
  }
}