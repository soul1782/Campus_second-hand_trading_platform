<template>
  <div class="orders-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <h2>{{ isSellerView ? '卖家订单管理' : '我的订单' }}</h2>
      <div class="placeholder"></div>
    </div>

    <!-- Tab切换 -->
    <div class="tabs">
      <div
        v-for="tab in tabs"
        :key="tab.status"
        :class="['tab', { active: activeTab === tab.status }]"
        @click="switchTab(tab.status)"
      >
        {{ tab.name }}
        <span v-if="tab.count > 0" class="badge">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="orders.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
        <circle cx="9" cy="21" r="1"/>
        <circle cx="20" cy="21" r="1"/>
        <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
      </svg>
      <p>暂无订单</p>
      <van-button type="primary" size="small" @click="$router.push('/')">去逛逛</van-button>
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.orderId" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-status" :class="getStatusClass(order.status)">
            {{ order.statusText }}
          </span>
        </div>

        <!-- 商品信息 -->
        <div class="order-product" @click="goToProduct(order.productId)">
          <div class="product-image">
            <img :src="order.productImage || 'https://placehold.co/80x80/eee/999?text=No+Img'" alt="">
          </div>
          <div class="product-info">
            <div class="product-title">{{ order.productTitle || '商品' }}</div>
            <div class="product-price">¥{{ order.unitPrice }} × {{ order.quantity }}</div>
          </div>
          <div class="product-total">¥{{ order.totalAmount }}</div>
        </div>

        <!-- 操作按钮区域 -->
        <div class="order-actions">

          <!-- 待付款 (0) - 买家视角 -->
          <template v-if="order.status === 0">
            <van-button size="small" plain type="danger" @click="cancelOrder(order.orderId)">取消订单</van-button>
            <!-- ✅ 修改：立即支付跳转到确认页面 -->
            <van-button size="small" type="primary" @click="goToPay(order)">立即支付</van-button>
          </template>

          <!-- 待发货 (1) - 卖家视角 -->
          <template v-if="order.status === 1 && isSellerView">
            <van-button size="small" type="primary" @click="shipOrder(order.orderId)">发货</van-button>
          </template>

          <!-- 待发货 (1) - 买家视角 -->
          <template v-if="order.status === 1 && !isSellerView">
            <van-button size="small" plain type="warning" @click="applyRefund(order.orderId)">申请退款</van-button>
            <span class="wait-text">等待卖家发货</span>
          </template>

          <!-- 待收货 (2) - 买家视角 -->
          <template v-if="order.status === 2 && !isSellerView">
            <van-button size="small" plain type="warning" @click="applyRefund(order.orderId)">申请退款</van-button>
            <van-button size="small" type="primary" @click="receiveOrder(order.orderId)">确认收货</van-button>
          </template>

          <!-- 待收货 (2) - 卖家视角 -->
          <template v-if="order.status === 2 && isSellerView">
            <span class="wait-text">买家待收货</span>
          </template>

          <!-- 已完成 (3) - 买家视角 -->
          <template v-if="order.status === 3 && !isSellerView">
            <van-button size="small" plain type="warning" @click="applyRefund(order.orderId)">申请退款</van-button>
            <van-button size="small" plain type="default" @click="buyAgain(order)">再次购买</van-button>
          </template>

          <!-- 已完成 (3) - 卖家视角 -->
          <template v-if="order.status === 3 && isSellerView">
            <span class="wait-text">交易已完成</span>
          </template>

          <!-- 已取消 (4) -->
          <template v-if="order.status === 4">
            <van-button size="small" plain type="default" @click="buyAgain(order)">重新购买</van-button>
          </template>

          <!-- 退款中 (5) - 买家视角 -->
          <template v-if="order.status === 5 && !isSellerView">
            <span class="wait-text refund-status">退款处理中</span>
            <van-button size="small" plain type="danger" @click="cancelRefund(order.orderId)">取消退款</van-button>
          </template>

          <!-- 退款中 (5) - 卖家视角 -->
          <template v-if="order.status === 5 && isSellerView">
            <span class="wait-text refund-status">买家申请退款</span>
          </template>

        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar" v-if="totalPages > 1">
      <button class="page-btn" :disabled="!hasPrev" @click="goToPage(currentPage - 1)">上一页</button>
      <span class="page-info">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
      <button class="page-btn" :disabled="!hasNext" @click="goToPage(currentPage + 1)">下一页</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { showToast } from 'vant'

export default {
  name: 'Orders',
  data() {
    return {
      orders: [],
      loading: false,
      currentPage: 0,
      totalPages: 1,
      hasNext: false,
      hasPrev: false,
      activeTab: null,
      isSellerView: false,
      orderCounts: {
        0: 0,
        1: 0,
        2: 0,
        3: 0,
        4: 0,
        5: 0
      },
      tabs: [
        { name: '全部', status: null, count: 0 },
        { name: '待付款', status: 0, count: 0 },
        { name: '待发货', status: 1, count: 0 },
        { name: '待收货', status: 2, count: 0 },
        { name: '已完成', status: 3, count: 0 }
      ]
    }
  },
  computed: {
    currentUser() {
      const user = localStorage.getItem('user')
      return user ? JSON.parse(user) : null
    },
    userId() {
      return this.currentUser?.userId
    }
  },
  mounted() {
    if (!this.userId) {
      this.$router.push('/login')
      return
    }
    this.isSellerView = this.$route.query.role === 'seller'
    this.fetchOrderCounts()
    this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          size: 10
        }
        if (this.activeTab !== null && this.activeTab !== undefined) {
          params.status = this.activeTab
        }

        let url
        if (this.isSellerView) {
          url = `/api/orders/seller?sellerId=${this.userId}`
        } else {
          url = `/api/orders/buyer?buyerId=${this.userId}`
        }

        const res = await axios.get(url, { params })
        this.orders = res.data.content || []
        this.totalPages = res.data.totalPages || 1
        this.currentPage = res.data.number || 0
        this.hasNext = !res.data.last
        this.hasPrev = !res.data.first
      } catch (e) {
        console.error('获取订单失败', e)
        showToast('获取订单失败')
      } finally {
        this.loading = false
      }
    },

    async fetchOrderCounts() {
      try {
        for (let status = 0; status <= 3; status++) {
          const res = await axios.get(`/api/orders/buyer?buyerId=${this.userId}&status=${status}&size=1`)
          this.orderCounts[status] = res.data.totalElements || 0
        }
        for (let i = 0; i < this.tabs.length; i++) {
          const tab = this.tabs[i]
          if (tab.status !== null) {
            tab.count = this.orderCounts[tab.status]
          } else {
            const allRes = await axios.get(`/api/orders/buyer?buyerId=${this.userId}`)
            tab.count = allRes.data.totalElements || 0
          }
        }
      } catch (e) {
        console.error('获取订单数量失败', e)
      }
    },

    switchTab(status) {
      this.activeTab = status
      this.currentPage = 0
      this.fetchOrders()
    },

    goToPage(page) {
      if (page < 0 || page >= this.totalPages) return
      this.currentPage = page
      this.fetchOrders()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    goToProduct(productId) {
      if (productId) {
        this.$router.push(`/product/${productId}`)
      }
    },

    // ✅ 待付款订单：跳转到确认页面进行支付
    goToPay(order) {
      const payItem = {
        orderId: order.orderId,
        orderNo: order.orderNo,
        productId: order.productId,
        productTitle: order.productTitle,
        productImage: order.productImage,
        price: parseFloat(order.unitPrice),
        quantity: order.quantity,
        totalAmount: order.totalAmount,
        isFromOrder: true  // 标记来自待支付订单
      }
      sessionStorage.setItem('payOrderItem', JSON.stringify(payItem))
      this.$router.push('/confirm-order')
    },

    // 再次购买
    buyAgain(order) {
      const buyAgainItem = {
        cartItemId: null,
        productId: order.productId,
        productTitle: order.productTitle,
        productImage: order.productImage,
        price: parseFloat(order.unitPrice),
        quantity: order.quantity,
        selected: true,
        tradeLocation: order.tradeLocation || '请与卖家协商',
        tradeMethod: order.tradeMethod || 3,
        conditionLevel: order.conditionLevel,
        sellerId: order.sellerId,
        sellerName: order.sellerName
      }
      sessionStorage.setItem('directBuyItem', JSON.stringify(buyAgainItem))
      this.$router.push('/confirm-order')
    },

    async cancelOrder(orderId) {
      if (!confirm('确认取消该订单吗？')) return
      try {
        await axios.post(`/api/orders/${orderId}/cancel?userId=${this.userId}`)
        showToast('订单已取消')
        this.fetchOrders()
        this.fetchOrderCounts()
      } catch (e) {
        showToast(e.response?.data || '取消失败')
      }
    },

    async shipOrder(orderId) {
      if (!confirm('确认已发货吗？')) return
      try {
        await axios.post(`/api/orders/${orderId}/ship?sellerId=${this.userId}`)
        showToast('发货成功')
        this.fetchOrders()
      } catch (e) {
        showToast(e.response?.data || '发货失败')
      }
    },

    async receiveOrder(orderId) {
      if (!confirm('确认已收到商品吗？')) return
      try {
        await axios.post(`/api/orders/${orderId}/receive?buyerId=${this.userId}`)
        showToast('确认收货成功')
        this.fetchOrders()
        this.fetchOrderCounts()
      } catch (e) {
        showToast(e.response?.data || '确认收货失败')
      }
    },

    async applyRefund(orderId) {
      const reason = prompt('请输入退款原因：', '商品与描述不符')
      if (!reason || reason.trim() === '') {
        showToast('请填写退款原因')
        return
      }
      try {
        await axios.post(`/api/orders/${orderId}/refund/apply?userId=${this.userId}&reason=${encodeURIComponent(reason)}`)
        showToast('退款申请已提交，请等待处理')
        this.fetchOrders()
        this.fetchOrderCounts()
      } catch (e) {
        showToast(e.response?.data || '申请失败')
      }
    },

    async cancelRefund(orderId) {
      if (!confirm('确定要取消退款申请吗？')) return
      try {
        await axios.post(`/api/orders/${orderId}/refund/cancel?userId=${this.userId}`)
        showToast('已取消退款申请')
        this.fetchOrders()
        this.fetchOrderCounts()
      } catch (e) {
        showToast(e.response?.data || '操作失败')
      }
    },

    getStatusClass(status) {
      const classes = {
        0: 'status-pending',
        1: 'status-wait-ship',
        2: 'status-wait-receive',
        3: 'status-completed',
        4: 'status-cancelled',
        5: 'status-refund'
      }
      return classes[status] || ''
    }
  }
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
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

.tabs {
  display: flex;
  background: #fff;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 60px;
  z-index: 99;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #666;
  position: relative;
  cursor: pointer;
}

.tab.active {
  color: #2b6aff;
  font-weight: 500;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 2px;
  background: #2b6aff;
  border-radius: 1px;
}

.badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 10px;
  margin-left: 5px;
}

.order-list {
  padding: 12px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 13px;
  font-weight: 500;
}

.status-pending { color: #ff9800; }
.status-wait-ship { color: #2b6aff; }
.status-wait-receive { color: #ff4d4f; }
.status-completed { color: #52c41a; }
.status-cancelled { color: #999; }
.status-refund { color: #ff9800; }

.order-product {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
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
  padding: 0 12px;
}

.product-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 12px;
  color: #999;
}

.product-total {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  flex-shrink: 0;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.wait-text {
  font-size: 13px;
  color: #999;
  line-height: 32px;
  margin-right: 12px;
}

.refund-status {
  color: #ff9800;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state svg {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}
</style>