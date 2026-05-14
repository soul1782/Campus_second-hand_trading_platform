<template>
  <div class="confirm-order-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <h2>{{ isFromOrder ? '订单支付' : '确认订单' }}</h2>
      <div class="placeholder"></div>
    </div>

    <!-- 交易地址（从商品信息中读取） -->
    <div class="address-card" v-if="!isFromOrder">
      <div class="address-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
      </div>
      <div class="address-info">
        <div class="address-name">交易地点</div>
        <div class="address-detail">{{ tradeLocation }}</div>
      </div>
    </div>

    <!-- 订单信息（待支付订单） -->
    <div class="order-info-card" v-else>
      <div class="order-info-title">订单信息</div>
      <div class="order-info-row">
        <span class="label">订单号：</span>
        <span class="value">{{ payOrder.orderNo }}</span>
      </div>
      <div class="order-info-row">
        <span class="label">商品总额：</span>
        <span class="value">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section">
      <div class="section-header">
        <span class="seller-name">卖家：{{ sellerName }}</span>
      </div>
      <div class="product-card" v-for="(item, index) in selectedItems" :key="index">
        <div class="product-image">
          <img :src="item.productImage || 'https://placehold.co/80x80/eee/999?text=Img'" alt="">
        </div>
        <div class="product-info">
          <div class="product-title">{{ item.productTitle }}</div>
          <div class="product-spec" v-if="item.conditionLevel">成色：{{ getConditionText(item.conditionLevel) }}</div>
          <div class="product-price">¥{{ item.price }}</div>
        </div>
        <div class="product-quantity">x{{ item.quantity }}</div>
      </div>
    </div>

    <!-- 支付方式（仅新订单显示） -->
    <div class="payment-section" v-if="!isFromOrder">
      <div class="section-title">支付方式</div>
      <div class="payment-options">
        <div
          class="payment-option"
          :class="{ active: paymentMethod === 'balance' }"
          @click="paymentMethod = 'balance'"
        >
          <div class="payment-left">
            <svg viewBox="0 0 24 24" fill="none" stroke="#2b6aff" stroke-width="2">
              <rect x="2" y="6" width="20" height="12" rx="2"/>
              <circle cx="12" cy="12" r="2"/>
              <path d="M6 12h.01M18 12h.01"/>
            </svg>
            <span>VISA校园卡支付</span>
          </div>
          <div class="payment-right">
            <span class="balance">余额：¥{{ userBalance }}</span>
            <svg v-if="paymentMethod === 'balance'" viewBox="0 0 24 24" fill="#2b6aff" stroke="none">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 12l3 3 6-6" stroke="white" stroke-width="2"/>
            </svg>
          </div>
        </div>
        <div
          class="payment-option"
          :class="{ active: paymentMethod === 'alipay' }"
          @click="paymentMethod = 'alipay'"
        >
          <div class="payment-left">
            <svg viewBox="0 0 24 24" fill="none" stroke="#1677ff" stroke-width="2">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
              <path d="M8 12h8M12 8v8"/>
            </svg>
            <span>支付宝</span>
          </div>
          <div class="payment-right">
            <svg v-if="paymentMethod === 'alipay'" viewBox="0 0 24 24" fill="#2b6aff" stroke="none">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 12l3 3 6-6" stroke="white" stroke-width="2"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单金额明细 -->
    <div class="amount-section">
      <div class="amount-row">
        <span class="amount-label">商品总额</span>
        <span class="amount-value">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <div class="amount-row" v-if="!isFromOrder">
        <span class="amount-label">交易方式</span>
        <span class="amount-value">{{ tradeMethodText }}</span>
      </div>
      <div class="amount-row" v-if="!isFromOrder">
        <span class="amount-label">校园平台保障费</span>
        <span class="amount-value">¥{{ serviceFee.toFixed(2) }}</span>
      </div>
      <div class="amount-row total">
        <span class="amount-label">合计</span>
        <span class="amount-value">¥{{ finalAmount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 底部提交按钮 -->
    <div class="footer-bar">
      <div class="total-info">
        <span class="total-label">实付款</span>
        <span class="total-price">¥{{ finalAmount.toFixed(2) }}</span>
      </div>
      <van-button type="danger" @click="submitOrder" :loading="submitting">
        {{ isFromOrder ? '立即付款' : '提交订单' }}
      </van-button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { showToast } from 'vant'

export default {
  name: 'ConfirmOrder',
  data() {
    return {
      selectedItems: [],
      payOrder: null,      // 待支付订单信息
      isFromOrder: false,  // 是否来自待支付订单
      submitting: false,
      paymentMethod: 'balance',
      serviceFee: 5.00
    }
  },
  computed: {
    currentUser() {
      const user = localStorage.getItem('user')
      return user ? JSON.parse(user) : null
    },
    userId() {
      return this.currentUser?.userId
    },
    userBalance() {
      return '1250.50'
    },
    tradeLocation() {
      if (this.selectedItems.length > 0 && this.selectedItems[0].tradeLocation) {
        return this.selectedItems[0].tradeLocation
      }
      return '请与卖家协商交易地点'
    },
    tradeMethodText() {
      if (this.selectedItems.length > 0) {
        const method = this.selectedItems[0].tradeMethod
        const map = { 1: '仅支持自提', 2: '仅支持快递', 3: '自提/快递均可' }
        return map[method] || '面议'
      }
      return '面议'
    },
    sellerName() {
      if (this.isFromOrder && this.payOrder) {
        return this.payOrder.sellerName || '卖家'
      }
      if (this.selectedItems.length > 0) {
        return this.selectedItems[0].sellerName || '卖家'
      }
      return '卖家'
    },
    totalAmount() {
      if (this.isFromOrder && this.payOrder) {
        return this.payOrder.totalAmount
      }
      return this.selectedItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    },
    finalAmount() {
      if (this.isFromOrder) {
        return this.totalAmount
      }
      return this.totalAmount + this.serviceFee
    }
  },
  mounted() {
    // 1. 检查是否来自待支付订单
    const payOrderStr = sessionStorage.getItem('payOrderItem')
    if (payOrderStr) {
      this.isFromOrder = true
      this.payOrder = JSON.parse(payOrderStr)
      sessionStorage.removeItem('payOrderItem')

      // 构造商品列表
      this.selectedItems = [{
        productId: this.payOrder.productId,
        productTitle: this.payOrder.productTitle,
        productImage: this.payOrder.productImage,
        price: this.payOrder.price,
        quantity: this.payOrder.quantity
      }]
      return
    }

    // 2. 检查购物车结算
    let selectedStr = sessionStorage.getItem('selectedCartItems')
    if (selectedStr) {
      this.selectedItems = JSON.parse(selectedStr)
      sessionStorage.removeItem('selectedCartItems')
    }

    // 3. 检查直接购买
    const directStr = sessionStorage.getItem('directBuyItem')
    if (directStr && !selectedStr) {
      const item = JSON.parse(directStr)
      this.selectedItems = [item]
      sessionStorage.removeItem('directBuyItem')
    }

    if (this.selectedItems.length === 0 && !this.isFromOrder) {
      showToast('请选择要结算的商品')
      this.$router.back()
    }
  },
  methods: {
    getConditionText(level) {
      const map = { 1: '全新', 2: '99新', 3: '95新', 4: '9成新', 5: '8成新', 6: '7成新及以下' }
      return map[level] || '99新'
    },
    async submitOrder() {
      this.submitting = true

      try {
        if (this.isFromOrder && this.payOrder) {
          // 待支付订单：直接调用支付接口
          await axios.post(`/api/orders/${this.payOrder.orderId}/pay?buyerId=${this.userId}`)
          showToast('支付成功')
          setTimeout(() => {
            this.$router.push('/orders')
          }, 1500)
        } else {
          // 新订单：先创建订单，再支付
          if (!this.tradeLocation && this.selectedItems.length > 0) {
            showToast('请确认交易地点')
            this.submitting = false
            return
          }

          for (const item of this.selectedItems) {
            await axios.post('/api/orders', {
              productId: item.productId,
              buyerId: this.userId,
              quantity: item.quantity,
              remark: '',
              receiverName: this.currentUser?.username || '用户',
              receiverPhone: this.currentUser?.phone || '',
              receiverAddress: this.tradeLocation
            })
          }

          showToast('订单创建成功，即将跳转支付')

          // 如果是购物车结算的商品，删除购物车中对应的商品
          for (const item of this.selectedItems) {
            if (item.cartItemId) {
              await axios.delete(`/api/cart/remove?cartItemId=${item.cartItemId}&userId=${this.userId}`)
            }
          }

          setTimeout(() => {
            this.$router.push('/orders')
          }, 1500)
        }
      } catch (e) {
        console.error('操作失败', e)
        showToast(e.response?.data || '操作失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.confirm-order-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.back-btn svg {
  width: 20px;
  height: 20px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.placeholder {
  width: 32px;
}

.address-card {
  display: flex;
  align-items: center;
  background: #fff;
  margin: 10px 12px;
  padding: 16px;
  border-radius: 12px;
  gap: 12px;
}

.address-icon svg {
  width: 24px;
  height: 24px;
}

.address-info {
  flex: 1;
}

.address-name {
  font-size: 14px;
  color: #999;
  margin-bottom: 4px;
}

.address-detail {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.order-info-card {
  background: #fff;
  margin: 10px 12px;
  padding: 16px;
  border-radius: 12px;
}

.order-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info-row {
  display: flex;
  padding: 6px 0;
  font-size: 14px;
}

.order-info-row .label {
  color: #999;
  width: 80px;
}

.order-info-row .value {
  color: #333;
  font-weight: 500;
}

.product-section {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  overflow: hidden;
}

.section-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.seller-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.product-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.product-card:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.product-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
}

.product-quantity {
  font-size: 14px;
  color: #999;
}

.payment-section {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  padding: 0 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.payment-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.payment-option:last-child {
  border-bottom: none;
}

.payment-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.payment-left svg {
  width: 24px;
  height: 24px;
}

.payment-left span {
  font-size: 15px;
  color: #333;
}

.payment-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-right .balance {
  font-size: 13px;
  color: #999;
}

.payment-right svg {
  width: 20px;
  height: 20px;
}

.payment-option.active .payment-left span {
  color: #2b6aff;
}

.amount-section {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  padding: 16px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.amount-row.total {
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 600;
}

.amount-row.total .amount-label {
  color: #333;
}

.amount-row.total .amount-value {
  color: #ff4d4f;
}

.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  z-index: 100;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}

.total-info {
  flex: 1;
}

.total-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff4d4f;
}

.footer-bar .van-button {
  height: 44px;
  padding: 0 32px;
  border-radius: 22px;
}
</style>