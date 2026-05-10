<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <div class="form-group">
        <label for="username">用户名:</label>
        <input type="text" id="username" v-model="registerForm.username" required>
      </div>
      <div class="form-group">
        <label for="email">邮箱:</label>
        <input type="email" id="email" v-model="registerForm.email" required>
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input type="password" id="password" v-model="registerForm.password" required>
      </div>
      <div class="form-group">
        <label for="studentId">学号:</label>
        <input type="text" id="studentId" v-model="registerForm.studentId" required>
      </div>
      <div class="form-group">
        <label for="realName">真实姓名:</label>
        <input type="text" id="realName" v-model="registerForm.realName" required>
      </div>
      <div class="form-group">
        <label for="identityType">身份类型:</label>
        <select id="identityType" v-model="registerForm.identityType">
          <option value="1">学生</option>
          <option value="2">教职工</option>
        </select>
      </div>
      <div class="form-group">
        <label for="campus">校区:</label>
        <input type="text" id="campus" v-model="registerForm.campus" required>
      </div>
      <div class="form-group">
        <label for="department">院系/部门:</label>
        <input type="text" id="department" v-model="registerForm.department">
      </div>
      <div class="form-group">
        <label for="phone">电话:</label>
        <input type="text" id="phone" v-model="registerForm.phone">
      </div>
      <button type="submit" :disabled="loading">注册</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
    <p>已有账号？ <router-link to="/login">登录</router-link></p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        username: '',
        email: '',
        password: '',
        studentId: '',
        realName: '',
        identityType: 1,
        campus: '',
        department: '',
        phone: ''
      },
      loading: false,
      error: null
    }
  },
  methods: {
    async register() {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post('/api/auth/register', this.registerForm)
        alert(response.data)
        this.$router.push('/login')
      } catch (error) {
        this.error = error.response?.data || '注册失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
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
input, select {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
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
