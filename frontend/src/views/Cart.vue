<template>
  <div class="cart-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <h2>购物车</h2>
      <div class="edit-btn" @click="toggleEditMode">{{ isEditMode ? '完成' : '编辑' }}</div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 空状态 -->
    <div v-else-if="cartList.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
        <circle cx="9" cy="21" r="1"/>
        <circle cx="20" cy="21" r="1"/>
        <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
      </svg>
      <p>购物车空空如也～</p>
      <van-button type="primary" size="small" @click="$router.push('/')">去逛逛</van-button>
    </div>

    <!-- 购物车列表 -->
    <div v-else class="cart-list">
      <div v-for="item in cartList" :key="item.cartItemId" class="cart-item">
        <!-- 选择框 -->
        <div class="checkbox" @click="toggleSelect(item.cartItemId, !item.selected)">
          <svg v-if="item.selected" viewBox="0 0 24 24" fill="#2b6aff" stroke="none">
            <circle cx="12" cy="12" r="10" fill="#2b6aff"/>
            <path d="M8 12l3 3 6-6" stroke="white" stroke-width="2" fill="none"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
          </svg>
        </div>

        <!-- 商品图片 -->
        <div class="product-image" @click="goToProduct(item.productId)">
          <img :src="item.productImage || 'https://placehold.co/80x80/eee/999?text=Img'" alt="">
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <div class="product-title">{{ item.productTitle }}</div>
          <div class="product-price">¥{{ item.price }}</div>

          <!-- 数量选择器（编辑模式） -->
          <div v-if="isEditMode" class="quantity-control">
            <button @click="updateQuantity(item.cartItemId, item.quantity - 1)">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="updateQuantity(item.cartItemId, item.quantity + 1)">+</button>
          </div>
        </div>

        <!-- 非编辑模式显示总价 -->
        <div v-if="!isEditMode" class="item-total">
          ¥{{ (item.price * item.quantity).toFixed(2) }}
        </div>

        <!-- 编辑模式显示删除按钮 -->
        <div v-if="isEditMode" class="delete-btn" @click="removeItem(item.cartItemId)">
          <svg viewBox="0 0 24 24" fill="none" stroke="#ff4d4f" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 底部结算栏（非编辑模式） -->
    <div v-if="!isEditMode && cartList.length > 0" class="footer-bar">
      <div class="select-all" @click="selectAll(!allSelected)">
        <svg v-if="allSelected" viewBox="0 0 24 24" fill="#2b6aff" stroke="none">
          <circle cx="12" cy="12" r="10" fill="#2b6aff"/>
          <path d="M8 12l3 3 6-6" stroke="white" stroke-width="2" fill="none"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
        </svg>
        <span>全选</span>
      </div>
      <div class="total-info">
        <span class="total-label">合计：</span>
        <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <van-button type="danger" size="small" @click="checkout" :disabled="selectedCount === 0">
        结算({{ selectedCount }})
      </van-button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { showToast } from 'vant'

export default {
  name: 'Cart',
  data() {
    return {
      cartList: [],
      loading: false,
      isEditMode: false
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
    selectedItems() {
      return this.cartList.filter(item => item.selected)
    },
    selectedCount() {
      return this.selectedItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice() {
      return this.selectedItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    },
    allSelected() {
      return this.cartList.length > 0 && this.cartList.every(item => item.selected)
    }
  },
  mounted() {
    if (!this.userId) {
      this.$router.push('/login')
      return
    }
    this.fetchCartList()
  },
  methods: {
    async fetchCartList() {
      this.loading = true
      try {
        const res = await axios.get(`/api/cart/list?userId=${this.userId}`)
        this.cartList = res.data || []
      } catch (e) {
        console.error('获取购物车失败', e)
        showToast('获取购物车失败')
      } finally {
        this.loading = false
      }
    },

    async updateQuantity(cartItemId, quantity) {
      if (quantity < 1) {
        this.removeItem(cartItemId)
        return
      }
      try {
        await axios.put(`/api/cart/quantity?cartItemId=${cartItemId}&userId=${this.userId}&quantity=${quantity}`)
        const item = this.cartList.find(i => i.cartItemId === cartItemId)
        if (item) item.quantity = quantity
      } catch (e) {
        showToast(e.response?.data || '更新失败')
      }
    },

    async toggleSelect(cartItemId, selected) {
      try {
        await axios.put(`/api/cart/selected?cartItemId=${cartItemId}&userId=${this.userId}&selected=${selected}`)
        const item = this.cartList.find(i => i.cartItemId === cartItemId)
        if (item) item.selected = selected
      } catch (e) {
        showToast(e.response?.data || '操作失败')
      }
    },

    async selectAll(selected) {
      try {
        await axios.put(`/api/cart/select-all?userId=${this.userId}&selected=${selected}`)
        this.cartList.forEach(item => item.selected = selected)
      } catch (e) {
        showToast(e.response?.data || '操作失败')
      }
    },

    async removeItem(cartItemId) {
      if (!confirm('确定要删除该商品吗？')) return
      try {
        await axios.delete(`/api/cart/remove?cartItemId=${cartItemId}&userId=${this.userId}`)
        this.cartList = this.cartList.filter(item => item.cartItemId !== cartItemId)
        showToast('删除成功')
      } catch (e) {
        showToast(e.response?.data || '删除失败')
      }
    },

    toggleEditMode() {
      this.isEditMode = !this.isEditMode
    },

    // ✅ 修改：跳转到订单确认页面
    async checkout() {
      if (this.selectedCount === 0) {
        showToast('请选择要结算的商品')
        return
      }

      const selectedItems = this.selectedItems

      if (selectedItems.length === 0) {
        showToast('请选择要结算的商品')
        return
      }

      // 保存选中的商品到 sessionStorage
      sessionStorage.setItem('selectedCartItems', JSON.stringify(selectedItems))

      // 跳转到订单确认页
      this.$router.push('/confirm-order')
    },

    goToProduct(productId) {
      this.$router.push(`/product/${productId}`)
    }
  }
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 70px;
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

.edit-btn {
  font-size: 14px;
  color: #2b6aff;
  cursor: pointer;
}

.cart-list {
  padding: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  gap: 12px;
}

.checkbox {
  cursor: pointer;
  flex-shrink: 0;
}

.checkbox svg {
  width: 20px;
  height: 20px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  cursor: pointer;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.quantity-control button {
  width: 28px;
  height: 28px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.quantity-control span {
  font-size: 14px;
  min-width: 30px;
  text-align: center;
}

.item-total {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  flex-shrink: 0;
}

.delete-btn {
  cursor: pointer;
  flex-shrink: 0;
}

.delete-btn svg {
  width: 20px;
  height: 20px;
}

.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.select-all svg {
  width: 20px;
  height: 20px;
}

.total-info {
  flex: 1;
  text-align: right;
  margin-right: 12px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
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
</style>