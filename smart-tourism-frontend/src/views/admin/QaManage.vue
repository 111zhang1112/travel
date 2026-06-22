<template>
  <div class="qa-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>问答管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="问题管理" name="questions">
          <el-form :inline="true" :model="questionQuery" class="query-form">
            <el-form-item label="状态">
              <el-select v-model="questionQuery.status" placeholder="全部" clearable>
                <el-option label="待审核" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已拒绝" :value="2" />
                <el-option label="已删除" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadQuestions">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="questions" stripe v-loading="loadingQuestions">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="问题标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{ row }">
                {{ getCategoryLabel(row.category) }}
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="提问者" width="120" />
            <el-table-column prop="answerCount" label="回答数" width="80" align="center" />
            <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
                <el-tag v-else type="info">已删除</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="success" size="small" @click="handleReview(row, 1)">
                  通过
                </el-button>
                <el-button v-if="row.status === 0" type="warning" size="small" @click="handleReject(row)">
                  拒绝
                </el-button>
                <el-button v-if="row.status !== 3" type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="questionQuery.page"
            v-model:page-size="questionQuery.size"
            :total="questionTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadQuestions"
            @current-change="loadQuestions"
          />
        </el-tab-pane>

        <el-tab-pane label="答案管理" name="answers">
          <el-form :inline="true" :model="answerQuery" class="query-form">
            <el-form-item label="状态">
              <el-select v-model="answerQuery.status" placeholder="全部" clearable>
                <el-option label="已删除" :value="0" />
                <el-option label="正常" :value="1" />
                <el-option label="待审核" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAnswers">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="answers" stripe v-loading="loadingAnswers">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="questionId" label="问题ID" width="100" />
            <el-table-column prop="content" label="回答内容" min-width="300" show-overflow-tooltip />
            <el-table-column prop="likeCount" label="点赞数" width="80" align="center" />
            <el-table-column prop="isBest" label="最佳答案" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isBest" type="success">是</el-tag>
                <el-tag v-else type="info">否</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="danger">已删除</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">正常</el-tag>
                <el-tag v-else type="warning">待审核</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status !== 0" type="danger" size="small" @click="handleDeleteAnswer(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="answerQuery.page"
            v-model:page-size="answerQuery.size"
            :total="answerTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadAnswers"
            @current-change="loadAnswers"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const activeTab = ref('questions')

// 问题管理
const loadingQuestions = ref(false)
const questions = ref([])
const questionTotal = ref(0)
const questionQuery = ref({
  page: 1,
  size: 10,
  status: null
})

// 答案管理
const loadingAnswers = ref(false)
const answers = ref([])
const answerTotal = ref(0)
const answerQuery = ref({
  page: 1,
  size: 10,
  status: null
})

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentQuestion = ref(null)

onMounted(() => {
  loadQuestions()
})

const loadQuestions = async () => {
  loadingQuestions.value = true
  try {
    const res = await request({
      url: '/admin/qa/questions',
      method: 'get',
      params: questionQuery.value
    })
    questions.value = res.records || []
    questionTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loadingQuestions.value = false
  }
}

const loadAnswers = async () => {
  loadingAnswers.value = true
  try {
    const res = await request({
      url: '/admin/qa/answers',
      method: 'get',
      params: answerQuery.value
    })
    answers.value = res.records || []
    answerTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loadingAnswers.value = false
  }
}

const handleReview = async (row, status) => {
  try {
    await request({
      url: `/admin/qa/questions/${row.id}/review`,
      method: 'put',
      params: { status }
    })
    ElMessage.success('审核成功')
    loadQuestions()
  } catch (e) {
    ElMessage.error('审核失败')
  }
}

const handleReject = (row) => {
  currentQuestion.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    await request({
      url: `/admin/qa/questions/${currentQuestion.value.id}/review`,
      method: 'put',
      params: {
        status: 2,
        rejectReason: rejectReason.value
      }
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadQuestions()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这个问题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({
      url: `/admin/qa/questions/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteAnswer = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这个回答吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({
      url: `/admin/qa/answers/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    loadAnswers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getCategoryLabel = (category) => {
  const map = {
    SCENIC: '景点相关',
    HOTEL: '酒店住宿',
    ROUTE: '路线规划',
    FOOD: '美食推荐',
    TRAFFIC: '交通出行',
    OTHER: '其他'
  }
  return map[category] || category
}
</script>

<style lang="scss" scoped>
.qa-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
