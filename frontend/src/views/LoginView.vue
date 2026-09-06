<template>
  <div class="login-page">
    <div class="soft-blob blob-a"></div>
    <div class="soft-blob blob-b"></div>
    <div class="soft-blob blob-c"></div>

    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-top">
          <div class="brand-logo">
            <Building2 :size="26" />
          </div>
          <div>
            <div class="brand-title">智慧宿舍</div>
            <div class="brand-subtitle">Smart Dorm System</div>
          </div>
        </div>

        <div class="brand-copy">
          <h2>让住宿管理更简单</h2>
          <p>报修、请假、通知、住宿状态，一个入口集中处理。</p>
        </div>

        <div class="brand-points">
          <div class="brand-point">
            <Leaf :size="18" />
            <span>清爽高效的宿舍服务</span>
          </div>
          <div class="brand-point">
            <ShieldCheck :size="18" />
            <span>学生与管理员统一入口</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-wrap">
          <div class="mobile-brand">
            <div class="brand-logo">
              <Building2 :size="24" />
            </div>
            <span>智慧宿舍</span>
          </div>

          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>登录智能宿舍管理系统</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form>

          <div class="login-hint">
            初始账号请使用管理员提供的用户名和密码登录
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLoginView } from './LoginView.logic'

const {
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
} = useLoginView()
</script>
