<template>
  <div class="login-container">
    <h2>登录</h2>
    <form @submit.prevent="login">
      <div class="form-group">
        <label for="usernameOrEmail">用户名或邮箱:</label>
        <input type="text" id="usernameOrEmail" v-model="loginForm.usernameOrEmail" required>
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input type="password" id="password" v-model="loginForm.password" required>
      </div>
      <button type="submit" :disabled="loading">登录</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
    <p>还没有账号？ <router-link to="/register">注册</router-link></p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        usernameOrEmail: '',
        password: ''
      },
      loading: false,
      error: null
    }
  },
  methods: {
    async login() {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post('/api/auth/login', this.loginForm)
        // 保存token
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data))
        // 跳转到首页
        this.$router.push('/')
      } catch (error) {
        this.error = error.response?.data || '登录失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 5px;
}
.form-group {
  margin-bottom: 15px;
}
label {
  display: block;
  margin-bottom: 5px;
}
input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:disabled {
  background-color: #ccc;
}
.error {
  color: red;
  margin-top: 10px;
}
</style>
