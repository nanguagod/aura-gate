<template>
  <!-- 上传组件的包装容器 -->
  <div class="upload-wrapper">
    <!-- Element Plus 的上传组件 -->
    <!--
      v-model:file-list: 核心数据绑定，管理图片列表
      :action: 上传接口地址，图片要发送到的服务器位置
      :headers: 请求头，携带token用于身份验证
      list-type="picture-card": 显示为图片卡片样式
      :limit: 最多上传数量限制
      :multiple="true": 允许一次选择多张图片
      :before-upload: 上传前的校验函数
      :on-success: 上传成功回调
      :on-error: 上传失败回调
      :on-preview: 点击图片预览
      :on-remove: 删除图片回调
      :on-exceed: 超过数量限制回调
      :class: 动态class，上传满后隐藏"+"按钮
    -->
    <el-upload
        v-model:file-list="fileList"
        :action="uploadUrl"
        :headers="headers"
        list-type="picture-card"
        :limit="limit"
        :multiple="true"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-preview="handlePreview"
        :on-remove="handleRemove"
        :on-exceed="handleExceed"
        :class="{ hide: fileList.length >= limit }"
    >
      <!-- 上传按钮内部显示的内容：一个加号图标 -->
      <el-icon><Plus /></el-icon>
    </el-upload>

    <!-- 底部提示信息，告诉用户上传规则 -->
    <div v-if="showTip" class="el-upload__tip">
      请上传
      <!-- 如果有文件大小限制，显示大小要求 -->
      <span v-if="fileSize">大小不超过 <b style="color: #ff0000;">{{ fileSize }}MB</b></span>
      <!-- 如果有文件类型限制，显示格式要求 -->
      <span v-if="fileType"> 格式为 <b style="color: #ff0000;">{{ fileType.join("/") }}</b></span>
      的文件
    </div>

    <!-- 图片预览对话框，点击图片后显示大图 -->
    <el-dialog v-model="open" title="预览" width="800px" append-to-body>
      <!-- 预览的大图 -->
      <img :src="openImageUrl" style="width: 100%; display: block" alt="Preview" />
    </el-dialog>
  </div>
</template>

<script setup>
// 引入Vue核心功能：ref(响应式数据)、computed(计算属性)、watch(监听器)
import { ref, computed, watch } from "vue";
// 从工具函数获取用户的登录token（身份验证）
import { getToken } from "@/utils/auth";
// Element Plus的消息提示组件
import { ElMessage } from "element-plus";
// 加号图标组件
import { Plus } from "@element-plus/icons-vue";

// ============================================
// 【1. 定义组件接收的属性（Props）】
// 这些是父组件可以传递给本组件的参数
// ============================================
const props = defineProps({
  // modelValue: 最重要的属性，双向绑定的图片地址数据
  // 支持两种格式：
  // 1. 字符串：用逗号分隔的图片地址，如 "url1,url2,url3"
  // 2. 数组：图片地址数组，如 ["url1", "url2", "url3"]
  modelValue: [String, Array],

  // limit: 最多允许上传几张图片，默认5张
  limit: { type: Number, default: 5 },

  // fileSize: 单个图片的最大大小（单位MB），默认5MB
  fileSize: { type: Number, default: 5 }, // MB

  // fileType: 允许上传的图片格式，默认允许png、jpg、jpeg
  fileType: { type: Array, default: () => ["png", "jpg", "jpeg"] },

  // isShowTip: 是否显示底部的提示文字，默认显示
  isShowTip: { type: Boolean, default: true },
});

// ============================================
// 【2. 定义组件对外触发的事件】
// 当组件内部数据变化时，通过这个事件通知父组件
// ============================================
const emit = defineEmits(["update:modelValue"]);

// ============================================
// 【3. 基础配置】
// 配置上传所需的基本信息
// ============================================
// 从环境变量获取服务器基础地址
const baseUrl = import.meta.env.VITE_APP_BASE_API;
// 拼接完整的上传接口地址
const uploadUrl = ref(`${baseUrl}/file/upload`); // 根据实际接口调整
// 设置请求头，携带用户的身份令牌（token）
const headers = ref({ Authorization: "Bearer " + getToken() });

// ============================================
// 【4. 组件内部状态管理】
// 定义组件内部需要使用的响应式变量
// ============================================
// fileList: 存储图片列表的数组，格式符合Element Upload要求
// 每个元素包含：name(文件名)、url(图片地址)、response(服务器返回数据)等
const fileList = ref([]);

// open: 控制预览对话框的显示/隐藏
const open = ref(false);

// openImageUrl: 预览对话框中要显示的大图地址
const openImageUrl = ref("");

// showTip: 计算属性，判断是否需要显示底部提示
// 条件：父组件要求显示提示 且 （有文件类型限制 或 有文件大小限制）
const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize));

// ============================================
// 【5. 核心逻辑：监听外部值变化，回显图片】
// 当父组件传入的图片地址变化时，更新组件内部显示
// ============================================
watch(() => props.modelValue, (val) => {
  // 如果外部传入的是空值，清空图片列表
  if (!val) {
    fileList.value = [];
    return;
  }

  // 统一转为数组格式（处理两种输入格式）
  // 如果是数组就直接使用，如果是字符串就用逗号分割成数组
  const list = Array.isArray(val) ? val : val.split(",");

  // 将图片地址数组转换成Element Upload组件需要的格式
  // 格式要求：每个文件对象需要有name和url属性
  fileList.value = list.map(url => {
    // 处理图片地址：如果是相对路径，补全为完整地址
    let fullUrl = url;
    // 判断是否是相对路径的条件：
    // 1. 不是以http开头（不是网络地址）
    // 2. 不是以blob:开头（不是本地预览的blob地址）
    if (url && !url.startsWith("http") && !url.startsWith("blob:")) {
      // 补全为完整地址：基础地址 + 相对路径
      fullUrl = baseUrl + url;
    }
    // 返回Element Upload需要的格式
    return { name: url, url: fullUrl };
  });
}, { immediate: true, deep: true });
// immediate: true - 组件创建时立即执行一次
// deep: true - 深度监听，当对象内部属性变化时也触发

// ============================================
// 【6. 工具函数：触发父组件更新】
// 当图片列表变化时，通知父组件更新数据
// ============================================
const emitChange = () => {
  // 从fileList中提取所有图片的相对地址
  const urls = fileList.value.map(file => {
    // 获取图片地址，分两种情况：
    // 1. 新上传的图片：地址在response属性中
    // 2. 已存在的图片：地址在url属性中
    let url = file.response ? (file.response.data?.url || file.response.url) : file.url;

    // 去掉基础地址部分，只保留相对路径存储到数据库
    // 比如：http://example.com/uploads/123.jpg → /uploads/123.jpg
    return url.replace(baseUrl, "");
  });

  // 将数组转换成逗号分隔的字符串，触发更新事件
  // 这样父组件接收到的就是一个字符串格式的图片地址列表
  emit("update:modelValue", urls.join(","));
};

// ============================================
// 【7. 事件处理函数】
// 处理用户的各种操作
// ============================================

// 7.1 上传前校验函数
const handleBeforeUpload = (file) => {
  // 检查文件格式是否符合要求
  // some()方法：检查数组中是否有任一元素满足条件
  const isTypeOk = props.fileType.some(type =>
      // 两种判断方式：
      // 1. 检查文件的type属性（MIME类型）：如"image/jpeg"
      // 2. 检查文件后缀名：如".jpg"
      file.type.includes(type) || file.name.endsWith(type)
  );

  // 检查文件大小是否超限
  // file.size单位是字节，需要转换成MB
  // 1MB = 1024KB = 1024 * 1024字节
  const isSizeOk = file.size / 1024 / 1024 < props.fileSize;

  // 如果格式不对，提示错误并阻止上传
  if (!isTypeOk) {
    ElMessage.error(`格式必须为 ${props.fileType.join("/")}`);
    return false; // 返回false阻止上传
  }

  // 如果大小超限，提示错误并阻止上传
  if (!isSizeOk) {
    ElMessage.error(`大小不能超过 ${props.fileSize}MB`);
    return false; // 返回false阻止上传
  }

  // 所有检查都通过，允许上传
  return true;
};

// 7.2 上传成功处理函数
const handleSuccess = (res, uploadFile) => {
  // res: 服务器返回的数据
  // uploadFile: 当前上传的文件对象

  // 假设服务器返回的code为200表示成功
  if (res.code === 200) {
    // 上传成功，触发数据同步
    // 注意：此时uploadFile已经被Element自动添加到fileList中了
    // 所以我们只需要通知父组件更新数据即可
    emitChange();
  } else {
    // 上传失败（服务器返回了错误码）
    ElMessage.error(res.msg || "上传失败");

    // 从文件列表中移除这个失败的文件
    const index = fileList.value.indexOf(uploadFile);
    if(index !== -1) fileList.value.splice(index, 1);
  }
};

// 7.3 移除图片处理函数
const handleRemove = () => {
  // 用户删除了图片，立即同步数据给父组件
  emitChange();
};

// 7.4 预览图片处理函数
const handlePreview = (file) => {
  // 设置预览对话框要显示的图片地址
  openImageUrl.value = file.url;
  // 打开预览对话框
  open.value = true;
};

// 7.5 超过数量限制处理函数
const handleExceed = () => {
  // 用户尝试上传超过限制数量的图片
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`);
};

// 7.6 上传过程错误处理函数
const handleError = () => {
  // 上传过程中发生错误（网络错误、服务器错误等）
  ElMessage.error("图片上传接口异常");
};
</script>

<style scoped>

</style>
