import { createApp } from 'vue'
import { createPinia } from 'pinia'
import store from './stores'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//导入ElementPlus的中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
//引入路由守卫
import './permission'

import SvgIcon from '@/components/SvgIcon'
import 'virtual:svg-icons-register'
import '@/assets/styles/all.css'
import VxeUIBase from 'vxe-pc-ui'
import 'vxe-pc-ui/es/style.css'

//分页组件
import Pagination from '@/components/Pagination'

//图标选择组件
import IconSelect from '@/components/IconSelect'

// 图片预览组件
import ImagePreview from "@/components/ImagePreview"

// 图片上传组件
import ImageUpload from "@/components/ImageUpload"

// 文件上传组件
import FileUpload from "@/components/FileUpload"

// 富文本编辑器组件
import Editor from "@/components/Editor"

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(store)
app.use(VxeUIBase)

//遍历ElementPlus的所有图标, 并且全部注册
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.component('Pagination', Pagination)

app.component('svg-icon', SvgIcon)

app.component('IconSelect', IconSelect)

app.component('ImagePreview', ImagePreview)

app.component('ImageUpload', ImageUpload)

app.component('FileUpload', FileUpload)

app.component('Editor', Editor)

app.use(ElementPlus, {
  //本地化配置: 设置为中文
  locale: zhCn,
})

app.mount('#app')
