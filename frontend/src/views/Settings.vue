<template>
  <div class="settings-page">
    <!-- 顶部导航 -->
    <div class="settings-header">
      <div class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <h2>账号设置</h2>
      <div class="placeholder"></div>
    </div>

    <div class="settings-content">
      <!-- 基本信息 -->
      <div class="settings-section">
        <div class="section-title">基本信息</div>
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="profileForm.username" placeholder="请输入用户名">
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input type="text" v-model="profileForm.phone" placeholder="请输入手机号">
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="profileForm.email" placeholder="请输入邮箱">
        </div>
        <button class="save-btn" @click="saveProfile" :disabled="savingProfile">
          {{ savingProfile ? '保存中...' : '保存资料' }}
        </button>
      </div>

      <!-- 修改密码 -->
      <div class="settings-section">
        <div class="section-title">修改密码</div>
        <div class="form-group">
          <label>旧密码</label>
          <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码">
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码">
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码">
        </div>
        <button class="save-btn password-btn" @click="savePassword" :disabled="savingPassword">
          {{ savingPassword ? '修改中...' : '修改密码' }}
        </button>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" :class="['toast', messageType]">{{ message }}</div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Settings',
  data() {
    return {
      userId: null,
      profileForm: {
        username: '',
        phone: '',
        email: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      savingProfile: false,
      savingPassword: false,
      message: '',
      messageType: 'success'
    }
  },
  methods: {
    async fetchProfile() {
      try {
        const res = await axios.get(`/api/user/${this.userId}`)
        const data = res.data
        this.profileForm.username = data.username || ''
        this.profileForm.phone = data.phone || ''
        this.profileForm.email = data.email || ''
      } catch (e) {
        console.error('获取用户信息失败', e)
        this.showMessage('获取用户信息失败', 'error')
      }
    },
    async saveProfile() {
      if (!this.profileForm.username) {
        this.showMessage('用户名不能为空', 'error')
        return
      }
      if (!this.profileForm.email) {
        this.showMessage('邮箱不能为空', 'error')
        return
      }
      this.savingProfile = true
      try {
        const res = await axios.put(`/api/user/${this.userId}/profile`, this.profileForm)
        this.showMessage(res.data, 'success')
        // 更新本地存储的用户信息
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        user.username = this.profileForm.username
        user.email = this.profileForm.email
        localStorage.setItem('user', JSON.stringify(user))
      } catch (e) {
        const msg = e.response?.data || '保存失败'
        this.showMessage(msg, 'error')
      } finally {
        this.savingProfile = false
      }
    },
    async savePassword() {
      if (!this.passwordForm.oldPassword) {
        this.showMessage('请输入旧密码', 'error')
        return
      }
      if (!this.passwordForm.newPassword) {
        this.showMessage('请输入新密码', 'error')
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.showMessage('两次输入的新密码不一致', 'error')
        return
      }
      if (this.passwordForm.newPassword.length < 6) {
        this.showMessage('新密码长度不能少于6位', 'error')
        return
      }
      this.savingPassword = true
      try {
        const res = await axios.put(`/api/user/${this.userId}/password`, {
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.showMessage(res.data, 'success')
        this.passwordForm.oldPassword = ''
        this.passwordForm.newPassword = ''
        this.passwordForm.confirmPassword = ''
      } catch (e) {
        const msg = e.response?.data || '修改失败'
        this.showMessage(msg, 'error')
      } finally {
        this.savingPassword = false
      }
    },
    showMessage(text, type) {
      this.message = text
      this.messageType = type
      setTimeout(() => {
        this.message = ''
      }, 3000)
    }
  },
  mounted() {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    this.userId = user.userId
    if (!this.userId) {
      this.$router.push('/login')
      return
    }
    this.fetchProfile()
  }
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #f5f5f5;
}

.back-btn svg {
  width: 22px;
  height: 22px;
  color: #333;
}

.settings-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.placeholder {
  width: 36px;
}

.settings-content {
  padding: 20px 24px;
}

.settings-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  color: #333;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #2b6aff;
}

.save-btn {
  width: 100%;
  padding: 12px;
  background: #2b6aff;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 8px;
}

.save-btn:hover {
  background: #1a5af5;
}

.save-btn:disabled {
  background: #a0c0ff;
  cursor: not-allowed;
}

.password-btn {
  background: #ff6b35;
}

.password-btn:hover {
  background: #e55a2b;
}

.password-btn:disabled {
  background: #ffc0a0;
}

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  color: #fff;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.toast.success {
  background: #52c41a;
}

.toast.error {
  background: #ff4d4f;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateX(-50%) translateY(-10px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
