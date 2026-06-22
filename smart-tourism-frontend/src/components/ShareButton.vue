<template>
  <el-button 
    type="primary" 
    :icon="Share" 
    @click="showDialog = true"
    :size="size"
  >
    {{ text }}
  </el-button>

  <ShareDialog
    v-model="showDialog"
    :content-type="contentType"
    :content-id="contentId"
    :content-data="contentData"
    @share-success="handleShareSuccess"
  />
</template>

<script setup>
import { ref } from 'vue'
import { Share } from '@element-plus/icons-vue'
import ShareDialog from './ShareDialog.vue'

const props = defineProps({
  contentType: {
    type: String,
    required: true,
    validator: (value) => ['scenic', 'hotel', 'guide', 'route'].includes(value)
  },
  contentId: {
    type: Number,
    required: true
  },
  contentData: {
    type: Object,
    required: true
  },
  text: {
    type: String,
    default: '分享'
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['share-success'])

const showDialog = ref(false)

const handleShareSuccess = (data) => {
  emit('share-success', data)
}
</script>
