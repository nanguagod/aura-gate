import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './permission'

// 按需注册 Element Plus 图标（仅注册实际使用和数据库可能引用的图标）
import {
  User, Lock, Plus, Delete, Search, UploadFilled, Loading, WarningFilled,
  ArrowDown, DataBoard, MagicStick, ChatDotRound, ChatDotSquare,
  Document, FolderOpened, Promotion, Setting, Avatar, Menu,
  Edit, Refresh, InfoFilled, Check, Close, List
} from '@element-plus/icons-vue'

const iconMap = {
  User, Lock, Plus, Delete, Search, UploadFilled, Loading, WarningFilled,
  ArrowDown, DataBoard, MagicStick, ChatDotRound, ChatDotSquare,
  Document, FolderOpened, Promotion, Setting, Avatar, Menu,
  Edit, Refresh, InfoFilled, Check, Close, List
}

const app = createApp(App)

// Register used icons for dynamic <component :is="iconName" /> resolution
for (const [key, component] of Object.entries(iconMap)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
