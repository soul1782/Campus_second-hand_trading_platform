<template>
  <div class="my-products-page">
    <!-- ✅ 新增：带返回按钮的顶部导航 -->
    <div class="page-header">
      <div class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>
      <h2>我发布的</h2>
      <span class="count">共 {{ products.length }} 件</span>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="products.length === 0" class="empty-state">
      <p>你还没有发布任何商品哦～</p>
      <van-button type="primary" size="small" @click="$router.push('/publish')">去发布</van-button>
    </div>

    <div v-else class="product-list">
      <div v-for="item in products" :key="item.id" class="product-card">
        <div class="card-left">
          <img :src="item.images?.[0] || 'https://placehold.co/100x100?text=No+Img'" alt="cover" />
          <div class="status-tag" :class="item.status === 1 ? 'on-sale' : 'off-sale'">
            {{ item.status === 1 ? '在售' : '已下架' }}
          </div>
        </div>
        
        <div class="card-right">
          <div class="title">{{ item.title }}</div>
          <div class="price">¥{{ item.price }}</div>
          <div class="meta">
            <span>{{ item.condition }}</span>
            <span>|</span>
            <span>{{ item.campus }}</span>
          </div>
          
          <div class="actions">
            <van-button size="mini" plain type="primary" @click="goToEdit(item.id)">编辑</van-button>
            <van-button 
              size="mini" 
              :type="item.status === 1 ? 'default' : 'success'" 
              @click="toggleStatus(item)"
            >
              {{ item.status === 1 ? '下架' : '上架' }}
            </van-button>
            <van-button size="mini" plain type="danger" @click="deleteProduct(item.id)">删除</van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'MyProducts',
  data() {
    return {
      products: [],
      loading: false,
      currentUser: null
    }
  },
  async mounted() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      this.currentUser = JSON.parse(userStr)
      await this.fetchMyProducts()
    } else {
      this.$router.push('/login')
    }
  },
  methods: {
    async fetchMyProducts() {
      if (!this.currentUser?.userId) return
      this.loading = true
      try {
        // ✅ 调用后端接口获取我发布的商品
        const res = await axios.get(`/api/products/my?sellerId=${this.currentUser.userId}`)
        this.products = res.data || []
      } catch (e) {
        console.error('获取我的商品失败', e)
      } finally {
        this.loading = false
      }
    },
    goToEdit(id) {
      this.$router.push(`/edit/${id}`)
    },
    async toggleStatus(item) {
      const newStatus = item.status === 1 ? 3 : 1
      const action = newStatus === 1 ? '上架' : '下架'
      if (!confirm(`确定要${action}该商品吗？`)) return

      try {
        await axios.patch(`/api/products/${item.id}/status?status=${newStatus}`)
        item.status = newStatus
        alert(`${action}成功！`)
      } catch (e) {
        alert('操作失败')
      }
    },
    async deleteProduct(id) {
      if (!confirm('确定要永久删除该商品吗？此操作不可恢复！')) return

      try {
        // ✅ 发送 DELETE 请求
        await axios.delete(`/api/products/${id}`)

        // ✅ 只有后端成功返回后，才从本地列表移除
        this.products = this.products.filter(p => p.id !== id)
        alert('删除成功')
      } catch (e) {
        console.error(e)
        alert('删除失败: ' + (e.response?.data?.message || e.message))
      }
    }
  }
}
</script>

<style scoped>
.my-products-page { padding: 16px; background: #f5f5f5; min-height: 100vh; }

/* ✅ 新增：Header 样式 */
.page-header { 
  display: flex; 
  align-items: center; 
  margin-bottom: 16px; 
  position: relative;
}
.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #333;
  margin-right: 12px;
}
.back-btn svg { width: 20px; height: 20px; }
.page-header h2 { font-size: 18px; margin: 0; flex: 1; }
.count { font-size: 14px; color: #999; }

.product-list { display: flex; flex-direction: column; gap: 12px; }
.product-card { background: #fff; border-radius: 12px; padding: 12px; display: flex; gap: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.card-left { position: relative; width: 100px; height: 100px; flex-shrink: 0; }
.card-left img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
.status-tag { position: absolute; top: 4px; left: 4px; padding: 2px 6px; border-radius: 4px; font-size: 10px; color: #fff; }
.status-tag.on-sale { background: #52c41a; }
.status-tag.off-sale { background: #999; }
.card-right { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.title { font-size: 14px; font-weight: 500; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.price { font-size: 16px; color: #ff4d4f; font-weight: bold; margin-top: 4px; }
.meta { font-size: 12px; color: #999; margin-top: 4px; }
.actions { display: flex; gap: 8px; margin-top: 8px; }
.empty-state { text-align: center; padding: 40px 0; color: #999; }
</style>