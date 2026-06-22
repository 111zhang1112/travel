<template>
  <div class="community-manage">
    <h2 class="page-title">社区管理</h2>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ postStats.pending || 0 }}</div>
        <div class="stat-label">待审核动态</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ postStats.published || 0 }}</div>
        <div class="stat-label">已发布动态</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ commentStats.normal || 0 }}</div>
        <div class="stat-label">正常评论</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ commentStats.deleted || 0 }}</div>
        <div class="stat-label">已删除评论</div>
      </div>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 动态管理 -->
      <el-tab-pane label="动态审核" name="post">
        <div class="filter-bar">
          <el-select v-model="postFilter.status" placeholder="状态筛选" clearable @change="loadPosts">
            <el-option label="待审核" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已删除" :value="3" />
          </el-select>
          <el-input v-model="postFilter.keyword" placeholder="搜索内容" clearable @keyup.enter="loadPosts" style="width: 200px" />
          <el-button type="primary" @click="loadPosts">搜索</el-button>
        </div>

        <el-table :data="postList" v-loading="postLoading" stripe>
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="发布者" width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="32" :src="row.avatar" />
                <span>{{ row.nickname || row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="内容" min-width="200">
            <template #default="{ row }">
              <div class="post-content">
                <p>{{ row.content?.substring(0, 100) }}{{ row.content?.length > 100 ? '...' : '' }}</p>
                <div v-if="row.images" class="post-images">
                  <el-image 
                    v-for="(img, idx) in parseImages(row.images)" 
                    :key="idx"
                    :src="img" 
                    :preview-src-list="parseImages(row.images)"
                    fit="cover"
                    style="width: 60px; height: 60px; margin-right: 4px;"
                  />
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="位置" prop="location" width="120" />
          <el-table-column label="互动" width="120">
            <template #default="{ row }">
              <div class="interaction-info">
                <span>👁 {{ row.viewCount }}</span>
                <span>❤ {{ row.likeCount }}</span>
                <span>💬 {{ row.commentCount }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getPostStatusType(row.status)">{{ getPostStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" prop="createTime" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button type="success" size="small" @click="handleApprovePost(row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleRejectPost(row)">拒绝</el-button>
              </template>
              <template v-else-if="row.status === 3">
                <el-button type="primary" size="small" @click="handleRestorePost(row)">恢复</el-button>
              </template>
              <template v-else>
                <el-button type="danger" size="small" @click="handleDeletePost(row)">删除</el-button>
              </template>
              <el-button type="info" size="small" @click="handleViewPost(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="postPagination.page"
          v-model:page-size="postPagination.size"
          :total="postPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </el-tab-pane>

      <!-- 评论管理 -->
      <el-tab-pane label="评论管理" name="comment">
        <div class="filter-bar">
          <el-select v-model="commentFilter.status" placeholder="状态筛选" clearable @change="loadComments">
            <el-option label="已删除" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="待审核" :value="2" />
          </el-select>
          <el-select v-model="commentFilter.targetType" placeholder="类型筛选" clearable @change="loadComments">
            <el-option label="动态评论" value="POST" />
            <el-option label="景点评论" value="SCENIC" />
            <el-option label="酒店评论" value="HOTEL" />
            <el-option label="攻略评论" value="GUIDE" />
          </el-select>
          <el-input v-model="commentFilter.keyword" placeholder="搜索内容" clearable @keyup.enter="loadComments" style="width: 200px" />
          <el-button type="primary" @click="loadComments">搜索</el-button>
          <el-button type="danger" :disabled="selectedComments.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>

        <el-table :data="commentList" v-loading="commentLoading" stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="评论者" width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="32" :src="row.avatar" />
                <span>{{ row.nickname || row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评论内容" prop="content" min-width="200" show-overflow-tooltip />
          <el-table-column label="评论类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ getTargetTypeText(row.targetType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="目标ID" prop="targetId" width="80" />
          <el-table-column label="点赞数" prop="likeCount" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getCommentStatusType(row.status)">{{ getCommentStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="评论时间" prop="createTime" width="160" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button type="primary" size="small" @click="handleRestoreComment(row)">恢复</el-button>
              </template>
              <template v-else>
                <el-button type="danger" size="small" @click="handleDeleteComment(row)">删除</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="commentPagination.page"
          v-model:page-size="commentPagination.size"
          :total="commentPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadComments"
          @current-change="loadComments"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 动态详情弹窗 -->
    <el-dialog v-model="postDetailVisible" title="动态详情" width="600px">
      <div v-if="currentPost" class="post-detail">
        <div class="detail-header">
          <el-avatar :size="48" :src="currentPost.avatar" />
          <div class="detail-user">
            <div class="name">{{ currentPost.nickname || currentPost.username }}</div>
            <div class="time">{{ currentPost.createTime }}</div>
          </div>
        </div>
        <div class="detail-content">{{ currentPost.content }}</div>
        <div v-if="currentPost.images" class="detail-images">
          <el-image 
            v-for="(img, idx) in parseImages(currentPost.images)" 
            :key="idx"
            :src="img" 
            :preview-src-list="parseImages(currentPost.images)"
            fit="cover"
            style="width: 150px; height: 150px; margin: 4px;"
          />
        </div>
        <div v-if="currentPost.location" class="detail-location">
          📍 {{ currentPost.location }}
        </div>
        <div class="detail-stats">
          <span>👁 {{ currentPost.viewCount }} 浏览</span>
          <span>❤ {{ currentPost.likeCount }} 点赞</span>
          <span>💬 {{ currentPost.commentCount }} 评论</span>
        </div>
        <div v-if="currentPost.status === 2" class="reject-reason">
          <el-alert type="error" :closable="false">
            拒绝原因：{{ currentPost.rejectReason || '无' }}
          </el-alert>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminGetPostList, adminApprovePost, adminRejectPost, adminDeletePost, adminRestorePost, adminGetPostStats,
  adminGetCommentList, adminDeleteComment, adminRestoreComment, adminBatchDeleteComments, adminGetCommentStats
} from '@/api/admin'

const activeTab = ref('post')

// 动态相关
const postList = ref([])
const postLoading = ref(false)
const postFilter = ref({ status: null, keyword: '' })
const postPagination = ref({ page: 1, size: 10, total: 0 })
const postStats = ref({})
const postDetailVisible = ref(false)
const currentPost = ref(null)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingPost = ref(null)

// 评论相关
const commentList = ref([])
const commentLoading = ref(false)
const commentFilter = ref({ status: null, targetType: '', keyword: '' })
const commentPagination = ref({ page: 1, size: 10, total: 0 })
const commentStats = ref({})
const selectedComments = ref([])

onMounted(() => {
  loadPosts()
  loadPostStats()
  loadCommentStats()
})

const handleTabChange = (tab) => {
  if (tab === 'post') {
    loadPosts()
  } else {
    loadComments()
  }
}

// 动态管理方法
const loadPosts = async () => {
  postLoading.value = true
  try {
    const res = await adminGetPostList({
      page: postPagination.value.page,
      size: postPagination.value.size,
      status: postFilter.value.status,
      keyword: postFilter.value.keyword
    })
    postList.value = res.records || []
    postPagination.value.total = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    postLoading.value = false
  }
}

const loadPostStats = async () => {
  try {
    postStats.value = await adminGetPostStats()
  } catch (e) {
    console.error(e)
  }
}

const handleApprovePost = async (row) => {
  try {
    await adminApprovePost(row.id)
    ElMessage.success('审核通过')
    loadPosts()
    loadPostStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleRejectPost = (row) => {
  rejectingPost.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await adminRejectPost(rejectingPost.value.id, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadPosts()
    loadPostStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDeletePost = (row) => {
  ElMessageBox.confirm('确定要删除这条动态吗？', '提示', { type: 'warning' })
    .then(async () => {
      await adminDeletePost(row.id)
      ElMessage.success('删除成功')
      loadPosts()
      loadPostStats()
    })
    .catch(() => {})
}

const handleRestorePost = async (row) => {
  try {
    await adminRestorePost(row.id)
    ElMessage.success('恢复成功')
    loadPosts()
    loadPostStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleViewPost = (row) => {
  currentPost.value = row
  postDetailVisible.value = true
}

// 评论管理方法
const loadComments = async () => {
  commentLoading.value = true
  try {
    const res = await adminGetCommentList({
      page: commentPagination.value.page,
      size: commentPagination.value.size,
      status: commentFilter.value.status,
      targetType: commentFilter.value.targetType,
      keyword: commentFilter.value.keyword
    })
    commentList.value = res.records || []
    commentPagination.value.total = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    commentLoading.value = false
  }
}

const loadCommentStats = async () => {
  try {
    commentStats.value = await adminGetCommentStats()
  } catch (e) {
    console.error(e)
  }
}

const handleDeleteComment = (row) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', { type: 'warning' })
    .then(async () => {
      await adminDeleteComment(row.id)
      ElMessage.success('删除成功')
      loadComments()
      loadCommentStats()
    })
    .catch(() => {})
}

const handleRestoreComment = async (row) => {
  try {
    await adminRestoreComment(row.id)
    ElMessage.success('恢复成功')
    loadComments()
    loadCommentStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleSelectionChange = (selection) => {
  selectedComments.value = selection
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedComments.value.length} 条评论吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const ids = selectedComments.value.map(c => c.id)
      await adminBatchDeleteComments(ids)
      ElMessage.success('批量删除成功')
      loadComments()
      loadCommentStats()
    })
    .catch(() => {})
}

// 工具方法
const parseImages = (images) => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const getPostStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const getPostStatusText = (status) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已拒绝', 3: '已删除' }
  return map[status] || '未知'
}

const getCommentStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getCommentStatusText = (status) => {
  const map = { 0: '已删除', 1: '正常', 2: '待审核' }
  return map[status] || '未知'
}

const getTargetTypeText = (type) => {
  const map = { POST: '动态', SCENIC: '景点', HOTEL: '酒店', GUIDE: '攻略' }
  return map[type] || type
}
</script>

<style lang="scss" scoped>
.community-manage {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #333;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #409eff;
  }

  .stat-label {
    font-size: 14px;
    color: #999;
    margin-top: 8px;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;

  span {
    font-size: 13px;
  }
}

.post-content {
  p {
    margin: 0 0 8px;
    line-height: 1.5;
  }
}

.post-images {
  display: flex;
  flex-wrap: wrap;
}

.interaction-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.post-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .detail-user {
    .name {
      font-weight: 600;
      font-size: 16px;
    }
    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .detail-content {
    font-size: 15px;
    line-height: 1.6;
    margin-bottom: 16px;
  }

  .detail-images {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 16px;
  }

  .detail-location {
    color: #409eff;
    font-size: 14px;
    margin-bottom: 16px;
  }

  .detail-stats {
    display: flex;
    gap: 20px;
    color: #666;
    font-size: 14px;
    padding: 12px 0;
    border-top: 1px solid #eee;
  }

  .reject-reason {
    margin-top: 16px;
  }
}
</style>
