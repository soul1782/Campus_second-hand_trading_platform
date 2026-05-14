<template>
  <div class="edit-page">
    <div class="form-card">
      <!-- ✅ 新增：顶部返回/放弃按钮 -->
      <div class="form-header-actions">
        <div class="back-link" @click="$router.back()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          <span>放弃修改</span>
        </div>
      </div>

      <div class="form-header">
        <h2>编辑商品信息</h2>
        <div class="status-badge" :class="product.status === 1 ? 'on-sale' : 'off-sale'">
          {{ product.status === 1 ? '🟢 在售中' : '⚫ 已下架' }}
        </div>
      </div>

      <form @submit.prevent="submitUpdate">
        <!-- 图片管理 -->
        <div class="form-group">
          <label>商品图片 <span class="tip">(最多5张，首张为封面)</span></label>
          <div class="upload-area">
            <div v-for="(url, index) in allImages" :key="index" class="preview-item">
              <img :src="normalizeImageUrl(url)" alt="preview" />
              <button type="button" class="remove-btn" @click="removeImage(index)">×</button>
            </div>
            <label v-if="allImages.length < 5" class="upload-btn">
              <input type="file" accept="image/*" @change="handleImageSelect" />
              <span class="icon">+</span>
            </label>
          </div>
        </div>

        <!-- 标题 -->
        <div class="form-group">
          <label>商品标题</label>
          <input type="text" v-model="form.title" maxlength="50" required />
        </div>

        <!-- 分类 & 成色 -->
        <div class="form-row">
          <div class="form-group half">
            <label>物品分类</label>
            <select v-model.number="form.categoryId" required>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="form-group half">
            <label>新旧程度</label>
            <select v-model.number="form.conditionLevel" required>
              <option :value="1">全新</option><option :value="2">99新</option>
              <option :value="3">95新</option><option :value="4">9成新</option>
              <option :value="5">8成新</option><option :value="6">7成新及以下</option>
            </select>
          </div>
        </div>

        <!-- 价格 -->
        <div class="form-row">
          <div class="form-group half">
            <label>售价 (元)</label>
            <input type="number" v-model.number="form.price" step="0.01" min="0" required />
          </div>
          <div class="form-group half">
            <label>原价 (元，选填)</label>
            <input type="number" v-model.number="form.originalPrice" step="0.01" min="0" />
          </div>
        </div>

        <!-- ✅ 是否议价 -->
        <div class="form-group">
          <label>是否接受议价</label>
          <select v-model.number="form.isBargain">
            <option :value="1">✅ 可以议价（更容易成交）</option>
            <option :value="0">❌ 不议价（一口价）</option>
          </select>
        </div>

        <!-- 描述 -->
        <div class="form-group">
          <label>详细描述</label>
          <textarea v-model="form.description" rows="4" maxlength="500"></textarea>
        </div>

        <!-- 校区 & 地点 -->
        <div class="form-row">
          <div class="form-group half">
            <label>所在校区</label>
            <input type="text" v-model="form.campus" required />
          </div>
          <div class="form-group half">
            <label>期望交易地点</label>
            <input type="text" v-model="form.tradeLocation" />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <button type="submit" class="btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? '保存中...' : '保存修改' }}
          </button>
          <button type="button" class="btn-secondary" @click="toggleStatus" v-if="product.id">
            {{ product.status === 1 ? '⬇️ 下架商品' : '⬆️ 重新上架' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EditProduct',
  data() {
    return {
      product: { id: null, status: 1, images: [] },
      form: { 
        title: '', 
        categoryId: '', 
        conditionLevel: '', 
        price: null, 
        originalPrice: null, 
        description: '', 
        campus: '', 
        tradeLocation: '',
        isBargain: 1 // ✅ 默认值
      },
      categories: [],
      existingImages: [],
      newImageFiles: [],
      isSubmitting: false
    }
  },
  computed: {
    allImages() {
      return [...this.existingImages, ...this.newImageFiles.map(f => URL.createObjectURL(f))]
    }
  },
  async mounted() {
    if (!localStorage.getItem('token')) return this.$router.push('/login')
    await Promise.all([this.fetchCategories(), this.fetchProductDetail()])
  },
  methods: {
    normalizeImageUrl(url) {
      if (!url) return ''
      return url.startsWith('/') ? `http://localhost:8087${url}` : url
    },
    async fetchCategories() {
      const res = await axios.get('/api/categories')
      this.categories = res.data.map(c => ({ id: c.categoryId, name: c.categoryName }))
    },
    async fetchProductDetail() {
      const id = this.$route.params.id
      const res = await axios.get(`/api/products/${id}`)
      const data = res.data
      this.product = { id: data.id || data.productId, status: data.status || 1, images: data.images || [] }
      this.existingImages = [...this.product.images]

      // ✅ 回显时包含 isBargain
      this.form = {
        title: data.title, 
        categoryId: data.categoryId, 
        conditionLevel: data.conditionLevel,
        price: Number(data.price), 
        originalPrice: data.originalPrice ? Number(data.originalPrice) : null,
        description: data.description, 
        campus: data.campus, 
        tradeLocation: data.tradeLocation,
        isBargain: data.isBargain !== undefined ? data.isBargain : 1
      }
    },
    handleImageSelect(e) {
      const file = e.target.files[0]
      if (file && this.allImages.length < 5) {
        this.newImageFiles.push(file)
      }
      e.target.value = ''
    },
    removeImage(index) {
      if (index < this.existingImages.length) {
        this.existingImages.splice(index, 1)
      } else {
        this.newImageFiles.splice(index - this.existingImages.length, 1)
      }
    },
    async uploadNewImages() {
      const urls = []
      for (const file of this.newImageFiles) {
        const formData = new FormData()
        formData.append('file', file)
        const res = await axios.post('/api/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        urls.push(res.data.url)
      }
      return urls
    },
    async submitUpdate() {
      this.isSubmitting = true
      try {
        const uploadedUrls = await this.uploadNewImages()
        const finalImages = [...this.existingImages, ...uploadedUrls]

        // ✅ 提交时 form 中已包含 isBargain
        await axios.put(`/api/products/${this.product.id}`, {
          ...this.form,
          imageUrls: finalImages
        })
        alert('修改成功！')
        this.$router.push(`/product/${this.product.id}`)
      } catch (e) {
        alert('保存失败: ' + (e.response?.data?.message || e.message))
      } finally {
        this.isSubmitting = false
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
  },
  beforeDestroy() {
    this.newImageFiles.forEach(f => URL.revokeObjectURL(URL.createObjectURL(f)))
  }
}
</script>

<style scoped>
.edit-page { min-height: 100vh; background: #f5f6fa; padding: 24px 16px 100px; }
.form-card { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

/* ✅ 新增：返回链接样式 */
.form-header-actions { margin-bottom: 16px; }
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}
.back-link:hover { color: #ff4d4f; } /* 编辑页用红色提示放弃 */
.back-link svg { width: 16px; height: 16px; }

.form-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; }
.form-header h2 { font-size: 24px; color: #333; margin: 0; }
.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; }
.status-badge.on-sale { background: #e6f4ea; color: #389e0d; }
.status-badge.off-sale { background: #f5f5f5; color: #8c8c8c; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 15px; font-weight: 500; color: #333; margin-bottom: 8px; }
.tip { font-size: 12px; color: #999; font-weight: normal; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 12px 14px; border: 1px solid #ddd; border-radius: 10px;
  font-size: 15px; background: #fafafa; transition: all 0.2s; box-sizing: border-box;
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  border-color: #2b6aff; background: #fff; outline: none; box-shadow: 0 0 0 3px rgba(43,106,255,0.1);
}
.form-row { display: flex; gap: 16px; }
.form-group.half { flex: 1; }
.upload-area { display: flex; flex-wrap: wrap; gap: 12px; }
.preview-item { position: relative; width: 100px; height: 100px; border-radius: 10px; overflow: hidden; border: 1px solid #eee; }
.preview-item img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn { position: absolute; top: 4px; right: 4px; width: 22px; height: 22px; border-radius: 50%; background: rgba(0,0,0,0.6); color: #fff; border: none; cursor: pointer; font-size: 16px; line-height: 1; display: flex; align-items: center; justify-content: center; }
.upload-btn { width: 100px; height: 100px; border: 2px dashed #ccc; border-radius: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; background: #fafafa; }
.upload-btn:hover { border-color: #2b6aff; background: rgba(43,106,255,0.05); }
.upload-btn input { display: none; }
.icon { font-size: 32px; color: #999; }
.action-row { display: flex; gap: 16px; margin-top: 24px; }
.btn-primary { flex: 1; padding: 14px; background: #2b6aff; color: #fff; border: none; border-radius: 12px; font-size: 16px; font-weight: 600; cursor: pointer; }
.btn-primary:disabled { background: #a0b4f7; cursor: not-allowed; }
.btn-secondary { flex: 1; padding: 14px; background: #fff; color: #333; border: 1px solid #ddd; border-radius: 12px; font-size: 16px; font-weight: 600; cursor: pointer; }
.btn-secondary:hover { background: #f9f9f9; }
</style>