<template>
  <!-- 文件上传组件 -->
  <div style="width: 100%;">
    <!-- Element Plus 的文件上传组件 -->
    <!--
      v-model:file-list: 双向绑定文件列表（控制显示哪些文件）
      :action: 上传到哪个服务器地址（API接口）
      :headers: 上传时带上的请求头（比如登录token）
      :drag: 是否支持拖拽上传（true=支持拖拽）
      :multiple: 是否支持多选文件（true=可以一次选多个文件）
      :before-upload: 上传前的校验函数（检查文件类型、大小等）
      :on-success: 上传成功后的处理函数
      :on-error: 上传失败后的处理函数
      :on-remove: 删除文件时的处理函数
      :on-exceed: 超过数量限制时的处理函数
      :accept: 接受的文件类型（比如 .pdf,.jpg）
      :file-size: 文件大小限制（单位是MB）
      :limit: 最多上传几个文件
    -->
    <el-upload
        v-model:file-list="fileList"
        :action="uploadUrl"
        :headers="headers"
        :drag="true"
        :multiple="true"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-remove="handleRemove"
        :on-exceed="handleExceed"
        :accept="accept"
        :file-size="fileSizeLimit"
        :limit="limit"
    >
      <!-- 上传区域中间的图标 -->
      <el-icon class="el-icon--upload">
        <upload-filled/>
      </el-icon>

      <!-- 上传区域的提示文字 -->
      <div class="el-upload__text">
        点击或拖拽文件到此处上传
      </div>

      <!-- 底部的小提示 -->
      <template #tip>
        <div class="el-upload__tip">
          单个文件最大 {{ fileSizeLimit }}MB 支持格式：{{ accept }}
        </div>
      </template>
    </el-upload>

    <!--
      上传进度条（只显示正在上传的文件）
      v-for="file in fileList": 遍历文件列表
      v-if="file?.status === 'uploading'": 只显示状态为"上传中"的文件
      ?.: 安全访问操作符（如果file是null/undefined也不会报错）
    -->
    <div v-for="file in fileList" :key="file.uid" v-if="file?.status === 'uploading'">
      <!-- 进度条组件 -->
      <!--
      :percentage="file?.percentage || 0"  进度百分比
      :stroke-width="2"                    进度条粗细
      :text-inside="true"                  文字在进度条内部
        -->
      <el-progress
          :percentage="file?.percentage || 0"
          :stroke-width="2"
          :text-inside="true"
      />
    </div>
  </div>
</template>

<script setup>
// 导入必要的 Vue 函数和 Element Plus 组件
import {ref, computed, watch} from "vue";      // Vue的核心功能
import {getToken} from "@/utils/auth";         // 获取用户登录token
import {ElMessage} from "element-plus";        // 消息提示组件
import {UploadFilled} from "@element-plus/icons-vue";  // 上传图标

// 定义组件接收的属性（父组件传过来的参数）
const props = defineProps({
  // 双向绑定的文件列表数据（v-model绑定的值）
  // 可以是字符串（逗号分隔的url）或数组
  modelValue: {
    type: [String, Array],
    default: () => []  // 默认值是空数组
  },

  // 上传数量限制（最多能传几个文件）
  limit: {
    type: Number,
    default: 2  // 默认最多传2个
  },

  // 文件大小限制（单位是MB）
  fileSizeLimit: {
    type: Number,
    default: 100  // 默认100MB
  },

  // 接受的文件类型（哪些格式的文件可以上传）
  // 示例：".pdf,.doc,.docx"
  accept: {
    type: String,
    default: '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar'  // 默认支持这些格式
  },

  // 自定义上传接口（如果想用自己的上传地址，不用默认的）
  customAction: {
    type: String,
    default: null  // 默认用系统配置的地址
  }
});

// 定义组件触发的事件（告诉父组件发生了什么）
const emit = defineEmits([
  "update:modelValue",  // 更新v-model的值
  "success",            // 上传成功
  "error",              // 上传失败
  "remove",             // 删除文件
  "exceed"              // 超出数量限制
]);

// 基础配置
const baseUrl = import.meta.env.VITE_APP_BASE_API;  // 从环境变量读取基础地址
const uploadUrl = ref(props.customAction || `${baseUrl}/file/upload`);  // 上传地址（优先用自定义的）
const headers = ref({Authorization: "Bearer " + getToken()});  // 请求头，带上登录token

// 组件内部状态
const rawFileList = ref([]);  // 原始文件列表（存储实际文件数据）

// 计算属性：对文件列表进行包装处理
const fileList = computed({
  get() {
    // 确保每个文件都有status状态
    return rawFileList.value.map(file => ({
      ...file,  // 展开原文件的所有属性
      status: file.status || 'success'  // 如果没有status，默认是成功状态
    }));
  },
  set(value) {
    // 当组件内部修改fileList时，更新原始数据
    rawFileList.value = value;
  }
});

// 监听外部值变化（父组件传的modelValue变化时）
watch(
    () => props.modelValue,  // 监听modelValue
    (val) => {
      // 如果值是空的，清空列表
      if (!val) {
        rawFileList.value = [];
        return;
      }

      // 处理数据：如果是数组直接用，如果是字符串就按逗号分割
      const list = Array.isArray(val) ? val : val.split(",");

      // 转换数据格式，让Element Plus能正确显示
      rawFileList.value = list.map((item, index) => {
        let fullUrl = item;  // 文件完整地址

        // 如果地址不是完整的http地址（可能是相对路径）
        if (item && !item.startsWith("http") && !item.startsWith("blob:")) {
          fullUrl = baseUrl + item;  // 拼接基础地址
        }

        // 返回Element Plus需要的格式
        return {
          name: item.split('/').pop() || `file-${index}`,  // 文件名（从url中提取）
          url: fullUrl,                                    // 文件完整地址
          uid: Date.now() + index,                         // 唯一ID
          status: 'success'                                // 状态（已上传成功）
        };
      });
    },
    {immediate: true, deep: true}  // 立即执行一次，深度监听
);

// 触发父组件更新（告诉父组件现在的文件列表）
const emitChange = () => {
  // 1. 过滤出有效的文件（有url或response中有url的）
  const urls = fileList.value
      .filter(file => file.url || file.response?.data?.url || file.response?.url)
      .map(file => {
        // 获取文件的url（可能是直接的url，也可能是response里的url）
        let url = file.response ? (file.response.data?.url || file.response.url) : file.url;

        // 如果url包含基础地址，去掉基础地址部分（只保存相对路径）
        if (url && url.startsWith(baseUrl)) {
          return url.replace(baseUrl, "");
        }
        return url;
      })
      .filter(Boolean);  // 过滤掉空值

  // 2. 用逗号连接所有url，触发更新
  emit("update:modelValue", urls.join(","));
};

// 上传前校验（检查文件是否合格）
const handleBeforeUpload = (file) => {
  // 检查文件类型
  const fileName = file.name.toLowerCase();  // 文件名转小写
  const acceptedTypes = props.accept.toLowerCase().split(',');  // 允许的类型转小写
  const isTypeValid = acceptedTypes.some(type =>
      fileName.endsWith(type.toLowerCase())  // 检查文件后缀是否符合
  );

  if (!isTypeValid) {
    ElMessage.error(`不支持的文件格式，请上传 ${props.accept} 格式的文件`);
    return false;  // 返回false阻止上传
  }

  // 检查文件大小（字节转MB）
  const isSizeValid = file.size / 1024 / 1024 < props.fileSizeLimit;
  if (!isSizeValid) {
    ElMessage.error(`文件大小不能超过 ${props.fileSizeLimit}MB`);
    return false;
  }

  // 检查数量限制
  if (fileList.value.length >= props.limit) {
    ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
    return false;
  }

  return true;  // 返回true允许上传
};

// 上传成功处理
const handleSuccess = (res, file) => {
  // 根据后端返回的code判断是否成功（假设200是成功）
  if (res.code === 200) {
    ElMessage.success('上传成功')

    // 更新父组件数据
    emitChange();
    // 告诉父组件上传成功
    emit("success", res, file);
  } else {
    // 上传失败（服务器返回了错误）
    ElMessage.error(res.msg || "上传失败");

    // 从列表中移除这个失败的文件
    const index = rawFileList.value.indexOf(file);
    if (index !== -1) {
      rawFileList.value.splice(index, 1);
    }

    // 告诉父组件上传失败
    emit("error", res, file);
  }
};

// 上传失败处理（网络错误、服务器错误等）
const handleError = (error, file) => {
  console.error('Upload error:', error);  // 控制台打印错误
  ElMessage.error("文件上传失败，请重试");  // 给用户提示
  emit("error", error, file);  // 告诉父组件
};

// 删除文件处理
const handleRemove = (file) => {
  // 从列表中移除这个文件
  const index = rawFileList.value.indexOf(file);
  if (index !== -1) {
    rawFileList.value.splice(index, 1);
  }

  // 更新父组件数据
  emitChange();
  // 告诉父组件删除了文件
  emit("remove", file);
};

// 超出数量限制处理
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
  emit("exceed");  // 告诉父组件超限了
};
</script>

<style scoped>

</style>
