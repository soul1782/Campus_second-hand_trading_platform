<template>
  <div class="product-detail">
    <!-- 🔹 图片轮播 -->
    <div class="swiper-container">
      <!-- ✅ 新增：悬浮返回按钮 -->
      <div class="floating-back" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </div>

      <van-swipe class="swiper" :autoplay="3000" indicator-color="white" @change="onSwipeChange">
        <van-swipe-item v-for="(img, index) in imageList" :key="index">
          <img :src="normalizeImageUrl(img)" class="swiper-img" @error="handleImageError" />
        </van-swipe-item>
      </van-swipe>
      <div class="image-count">{{ currentSlide + 1 }} / {{ imageList.length }}</div>
    </div>

    <!-- 🔹 商品信息 -->
    <div class="info-card">
      <h1 class="title">{{ product.title }}</h1>
      
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="original-price">原价 ¥{{ product.originalPrice }}</span>
      </div>

      <div class="meta-tags">
        <span class="tag condition">{{ product.condition }}</span>
        <span class="tag campus">{{ product.campus }}校区</span>
        <span class="tag views">👁 {{ product.viewCount || 0 }} 浏览</span>
      </div>

      <div class="trade-info">
        <div class="info-item"><span class="label">交易地点</span><span class="value">{{ product.tradeLocation || '面议' }}</span></div>
        <div class="info-item"><span class="label">交易方式</span><span class="value">{{ formatTradeMethod(product.tradeMethod) }}</span></div>
        <div class="info-item"><span class="label">是否议价</span><span class="value">{{ product.isBargain ? '✅ 可小刀' : '❌ 不议价' }}</span></div>
      </div>
    </div>

    <!-- 🔹 卖家卡片 -->
    <div class="seller-card" @click="goToSellerPage">
      <div class="seller-avatar">
        <img :src="normalizeImageUrl(product.sellerAvatar) || 'https://placehold.co/40x40/eee/999?text=U'" />
      </div>
      <div class="seller-info">
        <div class="seller-name">{{ product.sellerName || '匿名用户' }}</div>
        <div class="seller-stats">
          <span class="rating">⭐ {{ product.sellerRating || '5.0' }}</span>
          <span class="sep">|</span>
          <span>成交 {{ product.sellerDealCount || 0 }} 单</span>
        </div>
      </div>
      <van-button size="small" type="primary" @click.stop="contactSeller">联系 TA</van-button>
    </div>

    <!-- 🔹 仅卖家可见的管理操作 -->
    <div v-if="isOwner" class="owner-actions">
      <button class="action-btn edit" @click="$router.push(`/edit/${product.id}`)">✏️ 编辑商品</button>
      <button class="action-btn status" @click="toggleStatus">
        {{ product.status === 1 ? '⬇️ 下架' : '⬆️ 重新上架' }}
      </button>
    </div>

    <!-- 🔹 商品详情 -->
    <div class="detail-section">
      <h3 class="section-title">商品详情</h3>
      <div class="description" v-if="product.description" v-html="formatDescription(product.description)"></div>
      <div v-else class="empty-desc">卖家暂未填写更多描述～</div>
    </div>

    <!-- 🔹 底部操作栏 -->
    <div class="action-bar">
      <div class="left-actions">
        <div class="action-btn" @click="toggleFavorite">
          <van-icon :name="isFavorite ? 'like' : 'like-o'" :color="isFavorite ? '#ee0a24' : '#666'" />
          <span :style="{ color: isFavorite ? '#ee0a24' : '' }">收藏</span>
        </div>
        <div class="action-btn" @click="goToCart">
          <van-icon name="shopping-cart-o" />
          <span>购物车</span>
        </div>
        <div class="action-btn" @click="shareProduct">
          <van-icon name="share-o" />
          <span>分享</span>
        </div>
      </div>
      <div class="right-actions">
        <van-button type="warning" size="small" @click="addToCart">加入购物车</van-button>
        <van-button type="danger" size="small" @click="buyNow">立即购买</van-button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ProductDetail',
  data() {
    return {
      product: {
        id: '', title: '', price: 0, originalPrice: null,
        condition: '', campus: '', tradeLocation: '',
        tradeMethod: 3, isBargain: 1, viewCount: 0,
        sellerId: null, sellerName: '', sellerAvatar: null,
        sellerRating: 5.0, sellerDealCount: 0,
        description: '', images: [], status: 1
      },
      isFavorite: false,
      currentSlide: 0,
      isOwner: false,
      currentUser: null
    }
  },
  computed: {
    imageList() {
      if (!this.product.images || !Array.isArray(this.product.images) || this.product.images.length === 0) {
        return ['https://placehold.co/800x800/eee/999?text=No+Image']
      }
      return this.product.images
    }
  },
  async mounted() {
    // 1. 获取当前登录用户
    const userStr = localStorage.getItem('user')
    if (userStr) {
      this.currentUser = JSON.parse(userStr)
    }
    // 2. 加载商品详情
    await this.fetchProductDetail()
    // 3. 判断是否为卖家
    this.isOwner = this.currentUser && this.product.sellerId === this.currentUser.userId
  },
  methods: {
    // ✅ 归一化图片 URL：相对路径拼接后端地址
    normalizeImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('/') && !url.includes('http')) {
        return 'http://localhost:8087' + url
      }
      return url
    },
    handleImageError(e) {
      e.target.src = 'https://placehold.co/800x800/eee/999?text=Load+Failed'
    },
    onSwipeChange(index) {
      this.currentSlide = index
    },
    async fetchProductDetail() {
      const id = this.$route.params.id
      try {
        const res = await axios.get(`/api/products/${id}`)
        const data = res.data
        this.product = {
          ...this.product,
          id: data.id || data.productId,
          title: data.title,
          price: Number(data.price).toFixed(2),
          originalPrice: data.originalPrice ? Number(data.originalPrice).toFixed(2) : null,
          condition: data.condition || '未知',
          campus: data.campus,
          tradeLocation: data.tradeLocation,
          tradeMethod: data.tradeMethod || 3,
          isBargain: data.isBargain !== undefined ? data.isBargain : 1,
          viewCount: data.viewCount || 0,
          sellerId: data.sellerId,
          sellerName: data.sellerName,
          sellerAvatar: data.sellerAvatar,
          sellerRating: data.sellerRating || 5.0,
          sellerDealCount: data.sellerDealCount || 0,
          description: data.description,
          images: data.images || [],
          status: data.status || 1
        }
      } catch (e) {
        console.error('获取商品详情失败', e)
        alert('商品加载失败，请重试')
        this.$router.back()
      }
    },
    formatTradeMethod(method) {
      const map = { 1: '仅支持自提', 2: '仅支持快递', 3: '自提/快递均可' }
      return map[method] || '面议'
    },
    formatDescription(text) {
      if (!text) return ''
      return text.replace(/\n/g, '<br/>')
    },
    contactSeller() {
      alert('正在跳转聊天界面...')
      // TODO: 跳转聊天页
    },
    toggleFavorite() {
      this.isFavorite = !this.isFavorite
      alert(this.isFavorite ? '已加入收藏' : '已取消收藏')
      // TODO: 调用收藏 API
    },
    addToCart() {
      alert('已加入购物车')
      // TODO: 调用购物车 API
    },
    buyNow() {
      alert('正在创建订单...')
      // TODO: 跳转订单确认页
    },
    goToCart() {
      this.$router.push('/cart')
    },
    goToSellerPage() {
      // TODO: 跳转卖家主页 /seller/${this.product.sellerId}
    },
    shareProduct() {
      if (navigator.share) {
        navigator.share({
          title: this.product.title,
          text: `校园闲置：${this.product.title}，仅售 ¥${this.product.price}`,
          url: window.location.href
        })
      } else {
        navigator.clipboard.writeText(window.location.href)
        alert('商品链接已复制到剪贴板')
      }
    },
    async toggleStatus() {
      const newStatus = this.product.status === 1 ? 3 : 1
      const action = newStatus === 1 ? '上架' : '下架'
      if (!confirm(`确定要${action}该商品吗？`)) return
      
      try {
        await axios.patch(`/api/products/${this.product.id}/status?status=${newStatus}`)
        this.product.status = newStatus
        alert(`${action}成功！`)
      } catch (e) {
        alert('状态更新失败')
      }
    }
  }
}
</script>

<style scoped>
/* 🎨 闲鱼风格 · 商品详情页 */
.product-detail {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 70px; /* 为底部操作栏留空 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 🔹 轮播图 */
.swiper-container { position: relative; width: 100%; background: #fff; }
.swiper-img { width: 100%; height: 300px; object-fit: cover; }

/* ✅ 新增：悬浮返回按钮样式 */
.floating-back {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 36px;
  height: 36px;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  backdrop-filter: blur(4px);
}
.floating-back svg { width: 20px; height: 20px; }

.image-count {
  position: absolute; top: 12px; right: 12px;
  background: rgba(0,0,0,0.6); color: #fff;
  font-size: 12px; padding: 4px 10px; border-radius: 14px;
}

/* 🔹 商品信息卡片 */
.info-card { 
  background: #fff; margin: 10px; padding: 16px;
  border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.title { font-size: 18px; font-weight: 600; color: #333; margin: 0 0 12px; line-height: 1.4; }
.price-row { display: flex; align-items: baseline; gap: 12px; margin-bottom: 12px; }
.price { font-size: 24px; font-weight: bold; color: #ff4d4f; }
.original-price { font-size: 14px; color: #999; text-decoration: line-through; }
.meta-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.tag { font-size: 12px; padding: 4px 10px; border-radius: 14px; background: #f0f0f0; color: #666; }
.tag.condition { background: rgba(43,106,255,0.1); color: #2b6aff; font-weight: 500; }
.tag.campus { background: rgba(255,122,69,0.1); color: #ff7a45; }
.tag.views { background: #f5f5f5; color: #999; }

.trade-info { border-top: 1px solid #f0f0f0; padding-top: 12px; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.info-item .label { color: #666; }
.info-item .value { color: #333; font-weight: 500; }

/* 🔹 卖家卡片 */
.seller-card {
  display: flex; align-items: center; background: #fff;
  margin: 10px; padding: 14px 16px; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); cursor: pointer;
}
.seller-avatar { width: 44px; height: 44px; border-radius: 50%; overflow: hidden; margin-right: 12px; flex-shrink: 0; background: #eee; }
.seller-avatar img { width: 100%; height: 100%; object-fit: cover; }
.seller-info { flex: 1; }
.seller-name { font-size: 15px; font-weight: 500; color: #333; margin-bottom: 4px; }
.seller-stats { font-size: 12px; color: #999; }
.seller-stats .rating { color: #ff9800; font-weight: 500; }
.seller-stats .sep { margin: 0 6px; color: #ddd; }

/* 🔹 卖家管理操作区 */
.owner-actions { display: flex; gap: 12px; margin: 10px 10px 0; }
.action-btn {
  flex: 1; padding: 10px; border: none; border-radius: 10px;
  font-size: 14px; font-weight: 500; cursor: pointer; background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}
.action-btn.edit { color: #2b6aff; }
.action-btn.status { color: #ff7a45; }

/* 🔹 商品详情 */
.detail-section { background: #fff; margin: 10px; padding: 16px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.section-title { font-size: 16px; font-weight: 600; color: #333; margin: 0 0 12px; padding-bottom: 8px; border-bottom: 1px solid #f0f0f0; }
.description { font-size: 14px; color: #666; line-height: 1.6; white-space: pre-wrap; }
.empty-desc { color: #999; font-size: 14px; text-align: center; padding: 20px 0; }

/* 🔹 底部操作栏 */
.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0; height: 56px;
  background: #fff; border-top: 1px solid #eee; display: flex;
  align-items: center; padding: 0 12px; z-index: 100; box-shadow: 0 -2px 10px rgba(0,0,0,0.03);
}
.left-actions { display: flex; gap: 20px; flex: 1; }
.action-btn {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  font-size: 10px; color: #666; cursor: pointer;
}
.action-btn .van-icon { font-size: 20px; }
.right-actions { display: flex; gap: 10px; }
.right-actions .van-button { height: 36px; border-radius: 18px; font-size: 13px; padding: 0 16px; }
</style>