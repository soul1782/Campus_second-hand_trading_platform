<template>
  <div class="home-wrapper">
    <!-- 搜索与筛选栏 -->
    <div class="search-filters">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input type="text" placeholder="搜索商品名称" v-model="searchQuery" @keyup.enter="handleSearch" />
        </div>
      </div>
      
      <div class="filter-bar">
        <!-- 分类筛选 -->
        <div class="filter-section">
          <div class="filter-label">分类:</div>
          <div class="filter-tags">
            <div v-for="cat in categories" :key="cat.id" :class="['filter-tag', { active: activeCategory === cat.id }]" @click="onCategoryChange(cat.id)">
              {{ cat.name }}
            </div>
          </div>
        </div>

        <!-- 价格筛选 -->
        <div class="filter-section">
          <div class="filter-label">价格:</div>
          <div class="price-inputs">
            <input type="number" placeholder="最低价" v-model.number="priceMin" @change="resetAndFetch" />
            <span class="dash">-</span>
            <input type="number" placeholder="最高价" v-model.number="priceMax" @change="resetAndFetch" />
          </div>
        </div>

        <!-- 成色筛选 -->
        <div class="filter-section">
          <div class="filter-label">成色:</div>
          <div class="condition-filters">
            <div v-for="level in conditionLevels" :key="level.value" :class="['condition-tag', { active: selectedCondition === level.value }]" @click="onConditionChange(level.value)">
              {{ level.label }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid">
      <div class="product-card" v-for="product in displayProducts" :key="product.id" @click="$router.push(`/product/${product.id}`)">
        <div class="product-image">
  <img 
    v-if="product.images && product.images.length > 0" 
    :src="product.images[0]" 
    :alt="product.title"
    @error="handleImageError"
    class="product-img"
  />
  <!-- 图片加载失败时显示默认图标 -->
  <svg v-else viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.5">
    <rect x="3" y="3" width="18" height="18" rx="2"/>
    <circle cx="12" cy="12" r="5"/>
  </svg>
</div>
        <div class="product-info">
          <div class="product-title">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-tags">
            <span class="tag condition">{{ product.condition || '未知' }}</span>
            <span class="tag campus">{{ product.campus }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ 新增：分页控件 -->
    <div class="pagination-bar" v-if="totalPages > 1">
      <button class="page-btn" :disabled="!hasPrev" @click="goToPage(currentPage - 1)">
        &laquo; 上一页
      </button>
      <span class="page-info">
        第 {{ currentPage + 1 }} / {{ totalPages }} 页 (共 {{ totalElements }} 件)
      </span>
      <button class="page-btn" :disabled="!hasNext" @click="goToPage(currentPage + 1)">
        下一页 &raquo;
      </button>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item active"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg><span>首页</span></div>
      <div class="nav-item"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg><span>分类</span></div>
      <div class="nav-item nav-publish" @click="$router.push('/publish')"><div class="publish-btn"><svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></div><span>发布</span></div>
      <div class="nav-item"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg><span>购物车</span></div>
      <div class="nav-item" @click="$router.push('/my')"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg><span>我的</span></div>
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
      priceMin: null,
      priceMax: null,
      selectedCondition: null,
      categories: [{ id: 'all', name: '全部' }],
      products: [],
      // ✅ 分页状态
      currentPage: 0,
      totalPages: 1,
      totalElements: 0,
      hasNext: false,
      hasPrev: false
    }
  },
  computed: {
    conditionLevels() {
      return [
        { value: null, label: '全部' },
        { value: 1, label: '全新' },
        { value: 2, label: '99新' },
        { value: 3, label: '95新' },
        { value: 4, label: '9成新' },
        { value: 5, label: '8成新' },
        { value: 6, label: '7成新及以下' }
      ]
    },
    displayProducts() {
      return this.products.map(p => ({
        ...p,
        price: Number(p.price).toFixed(2)
      }))
    }
  },
  watch: {
    '$route'() { this.resetAndFetch() },
    searchQuery(val) { if (val === '') this.resetAndFetch() }
  },
  activated() { this.resetAndFetch() },
  methods: {
    // ✅ 统一请求方法（支持传页码）
    async fetchProducts(page = 0) {
      try {
        const params = { page, size: 10 } // 保持与后端 @PageableDefault(size=10) 一致
        if (this.searchQuery.trim()) params.keyword = this.searchQuery.trim()
        if (this.activeCategory !== 'all') params.categoryId = this.activeCategory
        if (typeof this.priceMin === 'number' && !isNaN(this.priceMin) && this.priceMin >= 0) params.minPrice = this.priceMin
        if (typeof this.priceMax === 'number' && !isNaN(this.priceMax) && this.priceMax >= 0) params.maxPrice = this.priceMax
        if (this.selectedCondition !== null && this.selectedCondition !== undefined) params.conditionLevel = this.selectedCondition

        const res = await axios.get('/api/products', { params })
        let productList = res.data.content || []
        this.products = Array.isArray(productList) ? productList : []

        // 更新分页元数据
        this.totalPages = res.data.totalPages || 1
        this.totalElements = res.data.totalElements || 0
        this.currentPage = res.data.number || 0
        this.hasNext = !res.data.last
        this.hasPrev = !res.data.first
      } catch (e) {
        console.error('获取商品失败', e)
      }
    },
    // 切换页码
    goToPage(page) {
      if (page < 0 || page >= this.totalPages) return
      this.fetchProducts(page)
      window.scrollTo({ top: 0, behavior: 'smooth' }) // 平滑滚回顶部
    },
    // 搜索/筛选时重置到第一页
    handleSearch() { this.resetAndFetch() },
    resetAndFetch() {
      this.currentPage = 0
      this.fetchProducts(0)
    },
    onCategoryChange(catId) {
      this.activeCategory = catId
      this.resetAndFetch()
    },
    onConditionChange(level) {
      this.selectedCondition = level
      this.resetAndFetch()
    },
    async fetchCategories() {
      try {
        const res = await axios.get('/api/categories')
        const list = res.data.map(c => ({ id: c.categoryId, name: c.categoryName }))
        this.categories = [{ id: 'all', name: '全部' }, ...list]
      } catch (e) {
        console.error('获取分类失败', e)
      }
    }
  },handleImageError(e) {
    // 图片加载失败时隐藏 img，显示 svg 图标
    e.target.style.display = 'none';
    const svg = e.target.nextElementSibling;
    if (svg && svg.tagName === 'svg') {
      svg.style.display = 'block';
    }
  },
  mounted() {
    if (!localStorage.getItem('token')) {
      this.$router.push('/login')
      return
    }
    this.fetchCategories()
    this.fetchProducts(0)
  }
}
</script>

<style scoped>
.home-wrapper {
  min-height: 100vh;
  background: #f5f6fa;
  /* ✅ 增加底部 padding 防止分页控件被底部导航遮挡 */
  padding-bottom: calc(70px + env(safe-area-inset-bottom) + 100px);
}
.search-filters { background: #fff; padding: 16px 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.search-bar { margin-bottom: 16px; }
.search-input-wrapper { display: flex; align-items: center; background: #f0f1f5; border-radius: 24px; padding: 10px 18px; gap: 10px; }
.search-icon { width: 20px; height: 20px; flex-shrink: 0; }
.search-input-wrapper input { border: none; background: transparent; outline: none; font-size: 15px; color: #333; width: 100%; }
.filter-bar { display: flex; flex-direction: column; gap: 12px; font-size: 14px; color: #333; }
.filter-section { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-label { font-weight: 600; min-width: 40px; color: #2b6aff; }
.filter-tags { display: flex; gap: 10px; flex: 1; overflow-x: auto; padding-bottom: 4px; }
.filter-tag { padding: 6px 16px; border-radius: 20px; font-size: 14px; background: #f0f1f5; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.filter-tag.active { background: #2b6aff; color: #fff; }
.price-inputs { display: flex; align-items: center; gap: 10px; flex: 1; }
.price-inputs input { width: 80px; padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; text-align: center; }
.dash { color: #999; font-weight: bold; }
.condition-filters { display: flex; gap: 10px; flex: 1; overflow-x: auto; padding-bottom: 4px; }
.condition-tag { padding: 6px 14px; border-radius: 20px; font-size: 13px; background: #f0f1f5; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.condition-tag.active { background: #ff9800; color: #fff; }

.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; padding: 16px 24px; }
@media (min-width: 1200px) { .product-grid { grid-template-columns: repeat(5, 1fr); } }
@media (min-width: 992px) and (max-width: 1199px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (min-width: 768px) and (max-width: 991px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 767px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }

.product-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); transition: transform 0.2s, box-shadow 0.2s; cursor: pointer; }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.product-image {
  aspect-ratio: 1;
  background: #f0f1f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  position: relative;
}
.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.product-image svg {
  width: 55%;
  height: 55%;
  display: none; /* 默认隐藏，图片加载失败时显示 */
}
.product-info { padding: 12px 14px 14px; }
.product-title { font-size: 15px; color: #333; font-weight: 500; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-price { font-size: 18px; color: #ff4d4f; font-weight: 600; margin-bottom: 10px; }
.product-tags { display: flex; gap: 8px; }
.tag { font-size: 12px; padding: 3px 8px; border-radius: 5px; }
.tag.condition { color: #2b6aff; background: rgba(43,106,255,0.08); }
.tag.campus { color: #ff7a45; background: rgba(255,122,69,0.08); }

/* ✅ 分页控件样式 */
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 20px;
  margin-top: 10px;
}
.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { background: #2b6aff; color: #fff; border-color: #2b6aff; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; background: #f9f9f9; }
.page-info { font-size: 14px; color: #666; }

.bottom-nav { position: fixed; bottom: 0; left: 0; right: 0; height: 70px; background: #fff; display: flex; justify-content: space-around; align-items: center; border-top: 1px solid #eee; z-index: 100; padding-bottom: env(safe-area-inset-bottom); }
.nav-item { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: #999; font-size: 13px; cursor: pointer; flex: 1; }
.nav-item svg { width: 26px; height: 26px; }
.nav-item.active { color: #2b6aff; }
.nav-publish { position: relative; top: -12px; }
.publish-btn { width: 56px; height: 56px; background: #2b6aff; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 16px rgba(43,106,255,0.35); }
.publish-btn svg { width: 28px; height: 28px; }
.nav-publish span { margin-top: 4px; }
</style>