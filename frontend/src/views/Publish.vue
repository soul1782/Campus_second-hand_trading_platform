<template>
  <div class="publish-page">
    <div class="form-card">
      <!-- ✅ 新增：顶部返回/取消按钮 -->
      <div class="form-header-actions">
        <div class="back-link" @click="$router.back()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          <span>取消发布</span>
        </div>
      </div>

      <div class="form-header">
        <h2>发布二手物品</h2>
        <p class="subtitle">真实描述，快速成交</p>
      </div>

      <form @submit.prevent="submitProduct">
        <!-- 图片上传区 -->
        <div class="form-group">
          <label>商品图片 <span class="tip">(最多5张，首张为封面)</span></label>
          <div class="upload-area">
            <div v-for="(url, index) in imagePreviews" :key="index" class="preview-item">
              <img :src="url" alt="preview" />
              <button type="button" class="remove-btn" @click="removeImage(index)">×</button>
            </div>
            <label v-if="imageFiles.length < 5" class="upload-btn">
              <input type="file" multiple accept="image/*" @change="handleImageSelect" />
              <span class="icon">+</span>
            </label>
          </div>
        </div>

        <!-- 标题 -->
        <div class="form-group">
          <label>商品标题</label>
          <input type="text" v-model="form.title" placeholder="品牌+型号+核心卖点，如：iPhone14 256G 蓝色" maxlength="50" required />
          <div class="char-count">{{ form.title.length }}/50</div>
        </div>

        <!-- 分类 & 成色 -->
        <div class="form-row">
          <div class="form-group half">
            <label>物品分类</label>
            <select v-model="form.categoryId" required>
              <option value="" disabled>请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="form-group half">
            <label>新旧程度</label>
            <select v-model.number="form.conditionLevel" required>
              <option value="" disabled>请选择</option>
              <option :value="1">全新</option>
              <option :value="2">99新</option>
              <option :value="3">95新</option>
              <option :value="4">9成新</option>
              <option :value="5">8成新</option>
              <option :value="6">7成新及以下</option>
            </select>
          </div>
        </div>

        <!-- 价格 -->
        <div class="form-row">
          <div class="form-group half">
            <label>售价 (元)</label>
            <input type="number" v-model.number="form.price" placeholder="0.00" step="0.01" min="0" required />
          </div>
          <div class="form-group half">
            <label>原价 (元，选填)</label>
            <input type="number" v-model.number="form.originalPrice" placeholder="选填" step="0.01" min="0" />
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
          <textarea v-model="form.description" placeholder="描述物品来源、使用情况、瑕疵、交易偏好等..." rows="4" maxlength="500"></textarea>
          <div class="char-count">{{ form.description.length }}/500</div>
        </div>

        <!-- 校区 & 交易地点 -->
        <div class="form-row">
          <div class="form-group half">
            <label>所在校区</label>
            <input type="text" v-model="form.campus" placeholder="例如：本部校区" required />
          </div>
          <div class="form-group half">
            <label>期望交易地点</label>
            <input type="text" v-model="form.tradeLocation" placeholder="例如：图书馆正门/食堂门口" />
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '发布中...' : '立即发布' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Publish',
  data() {
    return {
      form: {
        title: '',
        categoryId: '',
        conditionLevel: '',
        price: null,
        originalPrice: null,
        description: '',
        campus: '',
        tradeLocation: '',
        isBargain: 1 // ✅ 默认允许议价
      },
      imageFiles: [],
      imagePreviews: [],
      categories: [],
      isSubmitting: false
    }
  },
  mounted() {
    if (!localStorage.getItem('token')) {
      this.$router.push('/login')
      return
    }
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await axios.get('/api/categories')
        // ✅ 关键修复：将后端返回的 categoryId/categoryName 映射为前端需要的 id/name
        this.categories = (res.data || []).map(c => ({
          id: c.categoryId,
          name: c.categoryName
        }))
      } catch (e) {
        console.error('获取分类失败', e)
      }
    },
    handleImageSelect(e) {
      const files = Array.from(e.target.files)
      const remaining = 5 - this.imageFiles.length
      const toAdd = files.slice(0, remaining)

      toAdd.forEach(file => {
        this.imageFiles.push(file)
        // 创建本地预览 URL
        this.imagePreviews.push(URL.createObjectURL(file))
      })

      // 清空 input 允许重复选择同一文件
      e.target.value = ''
    },
    removeImage(index) {
      URL.revokeObjectURL(this.imagePreviews[index])
      this.imagePreviews.splice(index, 1)
      this.imageFiles.splice(index, 1)
    },
    async uploadImages() {
      const urls = []
      for (const file of this.imageFiles) {
        const formData = new FormData()
        formData.append('file', file)
        const res = await axios.post('/api/upload/image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        urls.push(res.data.url)
      }
      return urls
    },
    async submitProduct() {
      if (this.form.categoryId === '' || this.form.conditionLevel === '') {
        alert('请选择分类和新旧程度')
        return
      }

      this.isSubmitting = true
      try {
        // 1. 上传图片，获取 URL 数组
        let imageUrls = []
        if (this.imageFiles.length > 0) {
          imageUrls = await this.uploadImages()  // 返回 ["/images/xxx.jpg", ...]
        }

        // 2. 组装提交数据
        const payload = {
          ...this.form,
          sellerId: this.getSellerIdFromToken(),
          status: 1,
          quantity: 1,
          tradeMethod: 3,
          // ✅ 直接使用表单中用户选择的值
          isBargain: this.form.isBargain,
          imageUrls: imageUrls 
        }

        await axios.post('/api/products', payload)
        alert('发布成功！')
        this.$router.push('/')
      } catch (e) {
        console.error('发布失败', e)
        alert('发布失败: ' + (e.response?.data?.message || e.message))
      } finally {
        this.isSubmitting = false
      }
    },
    getSellerIdFromToken() {
      // 示例：解析 JWT 或从 localStorage 获取，请根据实际登录逻辑修改
      const userStr = localStorage.getItem('user')
      return userStr ? JSON.parse(userStr).userId : 1
    }
  },
  beforeDestroy() {
    // 组件销毁时释放预览 URL 内存
    this.imagePreviews.forEach(url => URL.revokeObjectURL(url))
  }
}
</script>

<style scoped>
.publish-page { min-height: 100vh; background: #f5f6fa; padding: 24px 16px 100px; }
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
.back-link:hover { color: #ff4d4f; }
.back-link svg { width: 16px; height: 16px; }

.form-header { margin-bottom: 28px; text-align: center; }
.form-header h2 { font-size: 24px; color: #333; margin: 0 0 8px; }
.subtitle { font-size: 14px; color: #666; margin: 0; }
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
.char-count { text-align: right; font-size: 12px; color: #999; margin-top: 4px; }

.upload-area { display: flex; flex-wrap: wrap; gap: 12px; }
.preview-item { position: relative; width: 100px; height: 100px; border-radius: 10px; overflow: hidden; border: 1px solid #eee; }
.preview-item img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn {
  position: absolute; top: 4px; right: 4px; width: 22px; height: 22px; border-radius: 50%;
  background: rgba(0,0,0,0.6); color: #fff; border: none; cursor: pointer; font-size: 16px; line-height: 1;
  display: flex; align-items: center; justify-content: center;
}
.upload-btn {
  width: 100px; height: 100px; border: 2px dashed #ccc; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;
  background: #fafafa;
}
.upload-btn:hover { border-color: #2b6aff; background: rgba(43,106,255,0.05); }
.upload-btn input { display: none; }
.icon { font-size: 32px; color: #999; }

.submit-btn {
  width: 100%; padding: 14px; background: #2b6aff; color: #fff; border: none; border-radius: 12px;
  font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 12px; transition: all 0.2s;
}
.submit-btn:hover:not(:disabled) { background: #1a52d6; transform: translateY(-1px); }
.submit-btn:disabled { background: #a0b4f7; cursor: not-allowed; }
</style>