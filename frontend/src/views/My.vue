<template>
  <div class="my-page">
    <!-- 个人信息头部 -->
    <div class="profile-header">
      <div class="profile-bg">
        <div class="avatar-section">
          <div class="avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <div class="user-info">
            <div class="username">{{ profile.username || '加载中...' }}</div>
            <div class="sub-info">
              <span class="badge" v-if="profile.verifyStatus === 1">已认证</span>
              <span class="campus">{{ profile.campus }}</span>
              <span class="department" v-if="profile.department">{{ profile.department }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 钱包卡片 -->
    <div class="wallet-card" v-if="profile.walletBalance !== undefined">
      <div class="wallet-title">我的钱包</div>
      <div class="wallet-balance">
        <span class="balance-label">可用余额</span>
        <span class="balance-value">¥{{ profile.walletBalance }}</span>
      </div>
      <div class="wallet-detail">
        <div class="detail-item">
          <span class="detail-value">¥{{ profile.frozenAmount || 0 }}</span>
          <span class="detail-label">冻结金额</span>
        </div>
        <div class="detail-item">
          <span class="detail-value">¥{{ profile.totalIncome || 0 }}</span>
          <span class="detail-label">累计收入</span>
        </div>
        <div class="detail-item">
          <span class="detail-value">¥{{ profile.totalExpenditure || 0 }}</span>
          <span class="detail-label">累计支出</span>
        </div>
      </div>
    </div>

    <!-- 订单状态 -->
    <div class="order-section">
      <div class="section-title">我的订单</div>
      <div class="order-grid">
        <div class="order-item">
          <span class="order-count">{{ profile.pendingPaymentCount || 0 }}</span>
          <span class="order-label">待付款</span>
        </div>
        <div class="order-item">
          <span class="order-count">{{ profile.pendingShipCount || 0 }}</span>
          <span class="order-label">待发货</span>
        </div>
        <div class="order-item">
          <span class="order-count">{{ profile.pendingReceiveCount || 0 }}</span>
          <span class="order-label">待收货</span>
        </div>
        <div class="order-item">
          <span class="order-count">{{ profile.completedCount || 0 }}</span>
          <span class="order-label">已完成</span>
        </div>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="menu-section">
      <div class="menu-item">
        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
        </svg>
        <span class="menu-text">我的收藏</span>
        <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
      <div class="menu-item">
        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        <span class="menu-text">浏览历史</span>
        <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
      <div class="menu-item">
        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
        <span class="menu-text">我的评价</span>
        <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
      <div class="menu-item" @click="$router.push('/settings')">
        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.82.33l.06.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0021.68 15a1.65 1.65 0 001.51 1H23a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/>
        </svg>
        <span class="menu-text">账号设置</span>
        <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
      <div class="menu-item" @click="logout">
        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="#ff4d4f" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        <span class="menu-text" style="color: #ff4d4f;">退出登录</span>
        <svg class="menu-arrow" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="$router.push('/')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        <span>首页</span>
      </div>
      <div class="nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7"/>
          <rect x="14" y="3" width="7" height="7"/>
          <rect x="3" y="14" width="7" height="7"/>
          <rect x="14" y="14" width="7" height="7"/>
        </svg>
        <span>分类</span>
      </div>
      <div class="nav-item nav-publish">
        <div class="publish-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </div>
        <span>发布</span>
      </div>
      <div class="nav-item">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/>
          <circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
        </svg>
        <span>购物车</span>
      </div>
      <div class="nav-item active">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'My',
  data() {
    return {
      profile: {}
    }
  },
  methods: {
    async fetchProfile() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const userId = user.userId
        if (!userId) {
          this.$router.push('/login')
          return
        }
        const res = await axios.get(`/api/user/${userId}`)
        this.profile = res.data
      } catch (e) {
        console.error('获取个人信息失败', e)
      }
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  },
  mounted() {
    if (!localStorage.getItem('token')) {
      this.$router.push('/login')
      return
    }
    this.fetchProfile()
  }
}
</script>

<style scoped>
.my-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 80px;
  box-sizing: border-box;
}

/* 个人信息头部 */
.profile-header {
  background: #2b6aff;
  padding: 30px 24px 40px;
  color: #fff;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar svg {
  width: 36px;
  height: 36px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.username {
  font-size: 20px;
  font-weight: 600;
}

.sub-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  opacity: 0.9;
}

.badge {
  background: rgba(255, 255, 255, 0.25);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

/* 钱包卡片 */
.wallet-card {
  margin: -20px 24px 16px;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.wallet-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 12px;
}

.wallet-balance {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.balance-label {
  font-size: 13px;
  color: #999;
}

.balance-value {
  font-size: 32px;
  font-weight: 700;
  color: #2b6aff;
}

.wallet-detail {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.detail-value {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.detail-label {
  font-size: 12px;
  color: #999;
}

/* 订单状态 */
.order-section {
  margin: 0 24px 16px;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.order-count {
  font-size: 20px;
  font-weight: 700;
  color: #2b6aff;
}

.order-label {
  font-size: 13px;
  color: #666;
}

/* 功能列表 */
.menu-section {
  margin: 0 24px 16px;
  background: #fff;
  border-radius: 14px;
  padding: 8px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-item + .menu-item {
  border-top: 1px solid #f5f5f5;
}

.menu-icon {
  width: 22px;
  height: 22px;
  margin-right: 12px;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-arrow {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

/* 底部导航 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #eee;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
  cursor: pointer;
  flex: 1;
}

.nav-item svg {
  width: 26px;
  height: 26px;
}

.nav-item.active {
  color: #2b6aff;
}

.nav-publish {
  position: relative;
  top: -12px;
}

.publish-btn {
  width: 56px;
  height: 56px;
  background: #2b6aff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(43, 106, 255, 0.35);
}

.publish-btn svg {
  width: 28px;
  height: 28px;
}

.nav-publish span {
  margin-top: 4px;
}
</style>
