<template>
  <!-- 使用Element Plus的图片组件 -->
  <!-- :src 绑定图片路径 -->
  <!-- fit="cover" 图片自适应容器，保持比例填满，可能裁剪 -->
  <!-- :style 动态绑定宽高样式 -->
  <!-- preview-src-list 预览时的图片列表（多张图片时可左右切换） -->
  <!-- preview-teleported 预览层挂载到body，避免样式冲突 -->
  <el-image
      :src="`${realSrc}`"
      fit="cover"
      :style="`width:${realWidth};height:${realHeight};`"
      :preview-src-list="realSrcList"
      preview-teleported
  >
    <!-- 当图片加载失败时显示的内容 -->
    <template #error>
      <div>
        <!-- 显示一个图片占位图标 -->
        <el-icon><pictureFilled /></el-icon>
      </div>
    </template>
  </el-image>
</template>

<script setup>
import {PictureFilled} from "@element-plus/icons-vue";
import {computed} from "vue";

// 定义组件接收的参数（父组件传递过来的）
const props = defineProps({
  // 图片地址
  src: {
    type: String,
    default: ""
  },
  // 宽度，可以是数字或字符串（如：100 或 "100px"）
  width: {
    type: [Number, String],
    default: ""
  },
  // 高度，可以是数字或字符串
  height: {
    type: [Number, String],
    default: ""
  }
})

// 计算属性：处理单个图片路径
const realSrc = computed(() => {
  // 如果没有传入图片地址，直接返回
  if (!props.src) {
    return
  }

  // 如果传入了多个图片地址（逗号分隔），只取第一个
  let real_src = props.src.split(",")[0]

  // 判断是否是完整的网络地址（http或https开头）
  if (/^(https?:)/.test(real_src)) {
    return real_src  // 是网络地址，直接返回
  }

  // 如果是本地静态资源（以 /src 开头），直接返回路径
  // 这种情况通常用于项目内的静态文件
  if (real_src.startsWith('/src')) {
    return real_src
  }

  // 既不是完整网络地址，也不是本地静态资源
  // 可能是相对路径或服务器上的图片路径
  // 给它加上API接口前缀，形成完整的访问地址
  return import.meta.env.VITE_APP_BASE_API + real_src
})

// 计算属性：处理多张图片预览列表
const realSrcList = computed(() => {
  // 如果没有传入图片地址，直接返回
  if (!props.src) {
    return
  }

  // 将逗号分隔的字符串转换为数组
  let real_src_list = props.src.split(",")
  let srcList = []  // 用于存储处理后的图片地址数组

  // 遍历每个图片地址
  real_src_list.forEach(item => {
    // 如果是完整的网络地址
    if (item.startsWith('http')) {
      return srcList.push(item)
    }

    // 如果是本地静态资源
    if (item.startsWith('/src')) {
      return srcList.push(item)
    }

    // 其他情况：添加API前缀
    return srcList.push(import.meta.env.VITE_APP_BASE_API + item)
  })

  // 返回处理后的图片地址数组
  return srcList
})

// 计算属性：处理宽度值
const realWidth = computed(() =>
    // 如果width是字符串（如："100px"），直接使用
    // 如果width是数字（如：100），添加"px"单位
    typeof props.width == "string" ? props.width : `${props.width}px`
)

// 计算属性：处理高度值
const realHeight = computed(() =>
    // 同上
    typeof props.height == "string" ? props.height : `${props.height}px`
)
</script>

<style scoped>

</style>
