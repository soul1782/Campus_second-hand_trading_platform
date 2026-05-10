<template>
  <div class="home-wrapper">
    <div class="home-page">
      <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input type="text" placeholder="搜索商品名称" v-model="searchQuery" />
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <div
        v-for="cat in categories"
        :key="cat.id"
        :class="['category-tab', { active: activeCategory === cat.id }]"
        @click="onCategoryChange(cat.id)"
      >
        {{ cat.name }}
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid">
      <div class="product-card" v-for="product in displayProducts" :key="product.id">
        <div class="product-image">
          <svg v-if="product.icon === 'box'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
          <svg v-else-if="product.icon === 'book'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M4 19.5A2.5 2.5 0 016.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>
          </svg>
          <svg v-else-if="product.icon === 'lamp'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M9 18h6"/>
            <path d="M10 22h4"/>
            <path d="M12 2v1"/>
            <path d="M12 7a5 5 0 00-5 5c0 2 2 3 2 6h6c0-3 2-4 2-6a5 5 0 00-5-5z"/>
          </svg>
          <svg v-else-if="product.icon === 'bike'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <circle cx="5.5" cy="17.5" r="3.5"/>
            <circle cx="18.5" cy="17.5" r="3.5"/>
            <path d="M15 6h-3l-3 9"/>
            <path d="M15 6a1 1 0 011 1v1"/>
            <path d="M12 17l-2-4h4l-2 4z"/>
          </svg>
          <svg v-else-if="product.icon === 'shoe'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M4 16v-2a2 2 0 012-2h12a2 2 0 012 2v2"/>
            <path d="M4 16h16"/>
            <path d="M8 12l2-4 2 4"/>
          </svg>
          <svg v-else-if="product.icon === 'game'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <rect x="2" y="6" width="20" height="12" rx="2"/>
            <path d="M6 12h4"/>
            <path d="M8 10v4"/>
            <circle cx="17" cy="12" r="1.5"/>
            <circle cx="14" cy="12" r="1.5"/>
          </svg>
          <svg v-else-if="product.icon === 'headphone'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M3 18v-6a9 9 0 0118 0v6"/>
            <path d="M21 19a2 2 0 01-2 2h-1a2 2 0 01-2-2v-3a2 2 0 012-2h3z"/>
            <path d="M3 19a2 2 0 002 2h1a2 2 0 002-2v-3a2 2 0 00-2-2H3z"/>
          </svg>
          <svg v-else-if="product.icon === 'laptop'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <rect x="2" y="3" width="20" height="14" rx="2"/>
            <line x1="2" y1="17" x2="22" y2="17"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
          </svg>
          <svg v-else-if="product.icon === 'camera'" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="12" cy="12" r="5"/>
            <path d="M12 2v2M12 20v2M2 12h2M20 12h2"/>
          </svg>
        </div>
        <div class="product-info">
          <div class="product-title">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-tags">
            <span class="tag condition">{{ product.condition }}</span>
            <span class="tag campus">{{ product.campus }}</span>
          </div>
        </div>
      </div>
    </div>

      <!-- 底部导航 -->
      <div class="bottom-nav">
        <div class="nav-item active">
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
        <div class="nav-item" @click="$router.push('/my')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <span>我的</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      searchQuery: '',
      activeCategory: 'all',
      categories: [{ id: 'all', name: '全部' }],
      products: []
    }
  },
  computed: {
    iconMap() {
      return {
        'digital': 'box',
        'books': 'book',
        'life': 'lamp',
        'transport': 'bike',
        'beauty': 'box',
        'clothes': 'shoe'
      }
    },
    displayProducts() {
      return this.products.map(p => ({
        ...p,
        icon: this.iconMap[p.category] || 'box'
      }))
    }
  },
  watch: {
    searchQuery() {
      this.fetchProducts()
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await axios.get('/api/categories')
        const list = res.data.map(c => ({
          id: c.categoryId,
          name: c.categoryName
        }))
        this.categories = [{ id: 'all', name: '全部' }, ...list]
      } catch (e) {
        console.error('获取分类失败', e)
      }
    },
    async fetchProducts() {
      try {
        const params = {}
        if (this.activeCategory !== 'all') {
          params.categoryId = this.activeCategory
        }
        if (this.searchQuery.trim()) {
          params.keyword = this.searchQuery.trim()
        }
        const res = await axios.get('/api/products', { params })
        this.products = res.data.map(p => ({
          id: p.id,
          title: p.title,
          price: p.price,
          condition: p.condition,
          campus: p.campus,
          category: p.category,
          icon: p.icon
        }))
      } catch (e) {
        console.error('获取商品失败', e)
      }
    },
    onCategoryChange(catId) {
      this.activeCategory = catId
      this.fetchProducts()
    }
  },
  mounted() {
    if (!localStorage.getItem('token')) {
      this.$router.push('/login')
      return
    }
    this.fetchCategories()
    this.fetchProducts()
  }
}
</script>

<style scoped>
.home-wrapper {
  min-height: 100vh;
  background: #f5f6fa;
}

.home-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 80px;
  box-sizing: border-box;
}

/* 搜索栏 */
.search-bar {
  padding: 16px 24px;
  background: #fff;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: #f0f1f5;
  border-radius: 24px;
  padding: 10px 18px;
  gap: 10px;
}

.search-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.search-input-wrapper input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #333;
  width: 100%;
}

.search-input-wrapper input::placeholder {
  color: #999;
}

/* 分类标签 */
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 24px;
  background: #fff;
}

.category-tab {
  padding: 8px 22px;
  border-radius: 20px;
  font-size: 15px;
  color: #666;
  background: #f0f1f5;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.category-tab.active {
  background: #2b6aff;
  color: #fff;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px 24px;
}

@media (min-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (min-width: 992px) and (max-width: 1199px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 767px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.product-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.product-image {
  aspect-ratio: 1;
  background: #f0f1f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.product-image svg {
  width: 55%;
  height: 55%;
}

.product-info {
  padding: 12px 14px 14px;
}

.product-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 600;
  margin-bottom: 10px;
}

.product-tags {
  display: flex;
  gap: 8px;
}

.tag {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 5px;
}

.tag.condition {
  color: #2b6aff;
  background: rgba(43, 106, 255, 0.08);
}

.tag.campus {
  color: #ff7a45;
  background: rgba(255, 122, 69, 0.08);
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
