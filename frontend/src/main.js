// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 1. 引入 Vant 组件
import {
  Swipe,
  SwipeItem,
  Image as VanImage,
  Rate,
  Button,
  Icon,
  Empty,
  Toast // 确保引入 Toast 用于提示
} from 'vant'

// 2. 引入 Vant 样式
import 'vant/lib/index.css'

// 3. 创建应用实例
const app = createApp(App)

// 4. 全局注册 Vant 组件
app.use(Swipe)
app.use(SwipeItem)
app.use(VanImage)
app.use(Rate)
app.use(Button)
app.use(Icon)
app.use(Empty)
// Toast 是函数式组件，通常直接引入即可，不需要 use

app.use(router).mount('#app')