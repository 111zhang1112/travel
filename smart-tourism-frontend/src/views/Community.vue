<template>
  <div class="community-page">
    <div class="community-header">
      <h1>旅行社区</h1>
      <p>分享你的旅行故事，发现精彩瞬间</p>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="community-tabs">
      <el-tab-pane label="动态广场" name="posts"></el-tab-pane>
      <el-tab-pane label="结伴同行" name="companion"></el-tab-pane>
      <el-tab-pane label="问答社区" name="qa"></el-tab-pane>
    </el-tabs>

    <div class="community-content" v-if="activeTab === 'posts'">
      <!-- 发布动态 -->
      <div class="post-form" v-if="userStore.isLoggedIn">
        <div class="form-header">
          <el-avatar :size="40" :src="userStore.user?.avatar" />
          <span>{{ userStore.user?.nickname || userStore.user?.username }}</span>
        </div>
        <el-input
          v-model="newPost.content"
          type="textarea"
          :rows="3"
          placeholder="分享你的旅行故事..."
          maxlength="500"
          show-word-limit
        />
        <div class="form-footer">
          <div class="form-actions">
            <el-input v-model="newPost.location" placeholder="添加位置" style="width: 200px" size="small">
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
        </div>
      </div>
      <div class="login-tip" v-else>
        <p>登录后即可发布动态</p>
        <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
      </div>

      <!-- 动态列表 -->
      <div class="post-list" v-loading="loading">
        <div class="post-item" v-for="post in posts" :key="post.id">
          <div class="post-header">
            <el-avatar :size="48" :src="post.avatar" />
            <div class="post-user">
              <div class="username">{{ post.nickname || post.username }}</div>
              <div class="post-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          
          <div class="post-content">{{ post.content }}</div>
          
          <div class="post-images" v-if="post.images">
            <el-image
              v-for="(img, idx) in parseImages(post.images)"
              :key="idx"
              :src="img"
              :preview-src-list="parseImages(post.images)"
              fit="cover"
              class="post-image"
            />
          </div>
          
          <div class="post-location" v-if="post.location">
            <el-icon><Location /></el-icon>
            {{ post.location }}
          </div>
          
          <div class="post-stats">
            <span class="stat-item" @click="handleLikePost(post)">
              <el-icon><Star /></el-icon>
              {{ post.likeCount }} 赞
            </span>
            <span class="stat-item" @click="toggleComments(post)">
              <el-icon><ChatDotRound /></el-icon>
              {{ post.commentCount }} 评论
            </span>
            <span class="stat-item">
              <el-icon><View /></el-icon>
              {{ post.viewCount }} 浏览
            </span>
          </div>

          <!-- 评论区 -->
          <div class="comment-section" v-if="post.showComments">
            <div class="comment-list">
              <div class="comment-item" v-for="comment in post.comments" :key="comment.id">
                <el-avatar :size="32" :src="comment.avatar" />
                <div class="comment-content">
                  <div class="comment-user">{{ comment.nickname || comment.username }}</div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-footer">
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    <span class="comment-like" @click="handleLikeComment(comment)">
                      <el-icon><Star /></el-icon> {{ comment.likeCount }}
                    </span>
                  </div>
                </div>
              </div>
              <el-empty v-if="post.comments?.length === 0" description="暂无评论" :image-size="60" />
            </div>
            
            <div class="comment-input" v-if="userStore.isLoggedIn">
              <el-input
                v-model="post.newComment"
                placeholder="写下你的评论..."
                size="small"
                @keyup.enter="handleComment(post)"
              >
                <template #append>
                  <el-button @click="handleComment(post)">发送</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && posts.length === 0" description="暂无动态" />
        
        <div class="load-more" v-if="hasMore">
          <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
        </div>
      </div>
    </div>

    <!-- 结伴同行内容 -->
    <div class="companion-content" v-if="activeTab === 'companion'">
      <!-- 发布结伴 -->
      <div class="companion-form" v-if="userStore.isLoggedIn">
        <h3>发布结伴信息</h3>
        <el-form :model="newCompanion" label-width="80px" size="small">
          <el-form-item label="标题">
            <el-input v-model="newCompanion.title" placeholder="例如：五一假期杭州西湖游" maxlength="50" />
          </el-form-item>
          <el-form-item label="目的地">
            <el-input v-model="newCompanion.destination" placeholder="例如：杭州" />
          </el-form-item>
          <el-form-item label="出行日期">
            <el-date-picker
              v-model="companionDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="需要人数">
            <el-input-number v-model="newCompanion.peopleNeeded" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="预算范围">
            <el-select v-model="newCompanion.budgetRange" placeholder="请选择">
              <el-option label="经济" value="LOW" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="豪华" value="HIGH" />
            </el-select>
          </el-form-item>
          <el-form-item label="旅行风格">
            <el-select v-model="newCompanion.travelStyle" placeholder="请选择">
              <el-option label="休闲" value="LEISURE" />
              <el-option label="冒险" value="ADVENTURE" />
              <el-option label="文化" value="CULTURE" />
              <el-option label="美食" value="FOOD" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系方式">
            <el-select v-model="newCompanion.contactMethod" placeholder="请选择">
              <el-option label="微信" value="WECHAT" />
              <el-option label="电话" value="PHONE" />
              <el-option label="QQ" value="QQ" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系信息">
            <el-input v-model="newCompanion.contactInfo" placeholder="请输入联系方式" />
          </el-form-item>
          <el-form-item label="详细描述">
            <el-input
              v-model="newCompanion.content"
              type="textarea"
              :rows="3"
              placeholder="详细描述你的行程计划..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="其他要求">
            <el-input
              v-model="newCompanion.requirements"
              type="textarea"
              :rows="2"
              placeholder="对同行伙伴的要求（选填）"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePublishCompanion" :loading="publishingCompanion">发布结伴</el-button>
            <el-button @click="resetCompanionForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="login-tip" v-else>
        <p>登录后即可发布结伴信息</p>
        <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
      </div>

      <!-- 筛选条件 -->
      <div class="companion-filter">
        <el-input v-model="companionFilter.destination" placeholder="目的地" clearable style="width: 150px" />
        <el-select v-model="companionFilter.budgetRange" placeholder="预算范围" clearable style="width: 120px">
          <el-option label="经济" value="LOW" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="豪华" value="HIGH" />
        </el-select>
        <el-select v-model="companionFilter.travelStyle" placeholder="旅行风格" clearable style="width: 120px">
          <el-option label="休闲" value="LEISURE" />
          <el-option label="冒险" value="ADVENTURE" />
          <el-option label="文化" value="CULTURE" />
          <el-option label="美食" value="FOOD" />
        </el-select>
        <el-button type="primary" @click="loadCompanions">搜索</el-button>
      </div>

      <!-- 结伴列表 -->
      <div class="companion-list" v-loading="loadingCompanions">
        <div class="companion-item" v-for="companion in companions" :key="companion.id">
          <div class="companion-header">
            <el-avatar :size="48" :src="companion.avatar" />
            <div class="companion-user">
              <div class="username">{{ companion.nickname || companion.username }}</div>
              <div class="companion-time">{{ formatTime(companion.createTime) }}</div>
            </div>
            <el-tag v-if="companion.status === 1" type="success" size="small">招募中</el-tag>
            <el-tag v-else-if="companion.status === 2" type="info" size="small">已满员</el-tag>
          </div>
          
          <h3 class="companion-title">{{ companion.title }}</h3>
          
          <div class="companion-info">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ companion.destination }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ companion.startDate }} 至 {{ companion.endDate }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ companion.currentPeople }}/{{ companion.peopleNeeded + 1 }}人</span>
            </div>
            <div class="info-item">
              <el-tag size="small">{{ getBudgetLabel(companion.budgetRange) }}</el-tag>
              <el-tag size="small" type="success">{{ getStyleLabel(companion.travelStyle) }}</el-tag>
            </div>
          </div>
          
          <div class="companion-content">{{ companion.content }}</div>
          
          <div class="companion-footer">
            <div class="companion-stats">
              <span class="stat-item" @click="handleLikeCompanion(companion)">
                <el-icon><Star /></el-icon>
                {{ companion.likeCount }}
              </span>
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ companion.viewCount }}
              </span>
            </div>
            <el-button
              v-if="userStore.isLoggedIn && companion.userId !== userStore.user?.id && companion.status === 1"
              type="primary"
              size="small"
              :disabled="companion.hasApplied"
              @click="handleApplyCompanion(companion)"
            >
              {{ companion.hasApplied ? '已申请' : '申请加入' }}
            </el-button>
          </div>
        </div>

        <el-empty v-if="!loadingCompanions && companions.length === 0" description="暂无结伴信息" />
        
        <div class="load-more" v-if="hasMoreCompanions">
          <el-button @click="loadMoreCompanions" :loading="loadingMoreCompanions">加载更多</el-button>
        </div>
      </div>
    </div>

    <!-- 问答社区内容 -->
    <div class="qa-content" v-if="activeTab === 'qa'">
      <!-- 发布问题 -->
      <div class="qa-form" v-if="userStore.isLoggedIn">
        <h3>提问</h3>
        <el-form :model="newQuestion" label-width="80px" size="small">
          <el-form-item label="问题标题">
            <el-input v-model="newQuestion.title" placeholder="简洁明了地描述你的问题" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="问题分类">
            <el-select v-model="newQuestion.category" placeholder="请选择分类">
              <el-option label="景点相关" value="SCENIC" />
              <el-option label="酒店住宿" value="HOTEL" />
              <el-option label="路线规划" value="ROUTE" />
              <el-option label="美食推荐" value="FOOD" />
              <el-option label="交通出行" value="TRAFFIC" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题详情">
            <el-input
              v-model="newQuestion.content"
              type="textarea"
              :rows="4"
              placeholder="详细描述你的问题..."
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="newQuestion.tags" placeholder="用逗号分隔，例如：杭州,西湖,一日游" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePublishQuestion" :loading="publishingQuestion">发布问题</el-button>
            <el-button @click="resetQuestionForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="login-tip" v-else>
        <p>登录后即可提问</p>
        <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
      </div>

      <!-- 筛选条件 -->
      <div class="qa-filter">
        <el-input v-model="qaFilter.keyword" placeholder="搜索问题" clearable style="width: 200px" />
        <el-select v-model="qaFilter.category" placeholder="分类" clearable style="width: 120px">
          <el-option label="景点相关" value="SCENIC" />
          <el-option label="酒店住宿" value="HOTEL" />
          <el-option label="路线规划" value="ROUTE" />
          <el-option label="美食推荐" value="FOOD" />
          <el-option label="交通出行" value="TRAFFIC" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="qaFilter.isSolved" placeholder="状态" clearable style="width: 120px">
          <el-option label="未解决" :value="0" />
          <el-option label="已解决" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadQuestions">搜索</el-button>
      </div>

      <!-- 问题列表 -->
      <div class="qa-list" v-loading="loadingQuestions">
        <div class="qa-item" v-for="question in questions" :key="question.id" @click="handleViewQuestion(question)">
          <div class="qa-header">
            <el-avatar :size="40" :src="question.avatar" />
            <div class="qa-user">
              <div class="username">{{ question.nickname || question.username }}</div>
              <div class="qa-time">{{ formatTime(question.createTime) }}</div>
            </div>
            <el-tag v-if="question.isSolved" type="success" size="small">已解决</el-tag>
            <el-tag v-else type="info" size="small">未解决</el-tag>
          </div>
          
          <h3 class="qa-title">{{ question.title }}</h3>
          
          <div class="qa-content-text">{{ question.content }}</div>
          
          <div class="qa-tags" v-if="question.tags">
            <el-tag v-for="tag in question.tags.split(',')" :key="tag" size="small" type="info">{{ tag }}</el-tag>
          </div>
          
          <div class="qa-footer">
            <div class="qa-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ question.viewCount }} 浏览
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ question.answerCount }} 回答
              </span>
              <span class="stat-item" @click.stop="handleLikeQuestion(question)">
                <el-icon><Star /></el-icon>
                {{ question.likeCount }}
              </span>
            </div>
            <el-tag size="small">{{ getCategoryLabel(question.category) }}</el-tag>
          </div>
        </div>

        <el-empty v-if="!loadingQuestions && questions.length === 0" description="暂无问题" />
        
        <div class="load-more" v-if="hasMoreQuestions">
          <el-button @click="loadMoreQuestions" :loading="loadingMoreQuestions">加载更多</el-button>
        </div>
      </div>
    </div>

    <!-- 问题详情对话框 -->
    <el-dialog v-model="questionDialogVisible" title="问题详情" width="800px" :close-on-click-modal="false">
      <div class="question-detail" v-if="currentQuestion">
        <div class="question-header">
          <el-avatar :size="48" :src="currentQuestion.avatar" />
          <div class="question-user">
            <div class="username">{{ currentQuestion.nickname || currentQuestion.username }}</div>
            <div class="question-time">{{ formatTime(currentQuestion.createTime) }}</div>
          </div>
          <el-tag v-if="currentQuestion.isSolved" type="success">已解决</el-tag>
          <el-tag v-else type="info">未解决</el-tag>
        </div>
        
        <h2 class="question-title">{{ currentQuestion.title }}</h2>
        
        <div class="question-content">{{ currentQuestion.content }}</div>
        
        <div class="question-tags" v-if="currentQuestion.tags">
          <el-tag v-for="tag in currentQuestion.tags.split(',')" :key="tag" size="small" type="info">{{ tag }}</el-tag>
        </div>
        
        <div class="question-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ currentQuestion.viewCount }} 浏览
          </span>
          <span class="stat-item" @click="handleLikeQuestion(currentQuestion)">
            <el-icon><Star /></el-icon>
            {{ currentQuestion.likeCount }} 点赞
          </span>
        </div>
        
        <el-divider />
        
        <h3>{{ currentQuestion.answerCount }} 个回答</h3>
        
        <!-- 回答列表 -->
        <div class="answer-list" v-loading="loadingAnswers">
          <div class="answer-item" v-for="answer in answers" :key="answer.id">
            <div class="answer-header">
              <el-avatar :size="40" :src="answer.avatar" />
              <div class="answer-user">
                <div class="username">{{ answer.nickname || answer.username }}</div>
                <div class="answer-time">{{ formatTime(answer.createTime) }}</div>
              </div>
              <el-tag v-if="answer.isBest" type="success" size="small">最佳答案</el-tag>
            </div>
            
            <div class="answer-content">{{ answer.content }}</div>
            
            <div class="answer-footer">
              <span class="answer-like" @click="handleLikeAnswer(answer)">
                <el-icon><Star /></el-icon>
                {{ answer.likeCount }} 点赞
              </span>
              <el-button
                v-if="userStore.isLoggedIn && currentQuestion.userId === userStore.user?.id && !currentQuestion.isSolved"
                type="success"
                size="small"
                @click="handleSetBestAnswer(answer)"
              >
                设为最佳答案
              </el-button>
              <el-button
                v-if="userStore.isLoggedIn && answer.userId === userStore.user?.id"
                type="danger"
                size="small"
                @click="handleDeleteAnswer(answer)"
              >
                删除
              </el-button>
            </div>
          </div>
          
          <el-empty v-if="!loadingAnswers && answers.length === 0" description="暂无回答，快来抢沙发吧！" :image-size="80" />
        </div>
        
        <!-- 回答输入框 -->
        <div class="answer-input" v-if="userStore.isLoggedIn">
          <h4>写下你的回答</h4>
          <el-input
            v-model="newAnswer"
            type="textarea"
            :rows="4"
            placeholder="分享你的经验和见解..."
            maxlength="1000"
            show-word-limit
          />
          <el-button type="primary" @click="handleSubmitAnswer" :loading="submittingAnswer" style="margin-top: 12px">
            提交回答
          </el-button>
        </div>
        <div class="login-tip" v-else style="margin-top: 20px">
          <p>登录后即可回答问题</p>
          <el-button type="primary" size="small" @click="$router.push('/login')">去登录</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Star, ChatDotRound, View, Calendar, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getPosts, createPost, likePost, getComments, createComment, likeComment } from '@/api/community'
import { getCompanions, createCompanion, likeCompanion, applyCompanion } from '@/api/companion'
import { getQuestions, createQuestion, likeQuestion, getAnswers, createAnswer, likeAnswer, setBestAnswer, deleteAnswer } from '@/api/qa'

const userStore = useUserStore()

// 标签页
const activeTab = ref('posts')

const loading = ref(false)
const loadingMore = ref(false)
const publishing = ref(false)
const posts = ref([])
const page = ref(1)
const hasMore = ref(true)

const newPost = ref({
  content: '',
  location: ''
})

// 结伴相关
const loadingCompanions = ref(false)
const loadingMoreCompanions = ref(false)
const publishingCompanion = ref(false)
const companions = ref([])
const companionPage = ref(1)
const hasMoreCompanions = ref(true)

const companionDateRange = ref([])
const newCompanion = ref({
  title: '',
  destination: '',
  peopleNeeded: 1,
  budgetRange: '',
  travelStyle: '',
  contactMethod: '',
  contactInfo: '',
  content: '',
  requirements: ''
})

const companionFilter = ref({
  destination: '',
  budgetRange: '',
  travelStyle: ''
})

// 问答相关
const loadingQuestions = ref(false)
const loadingMoreQuestions = ref(false)
const publishingQuestion = ref(false)
const questions = ref([])
const questionPage = ref(1)
const hasMoreQuestions = ref(true)

const newQuestion = ref({
  title: '',
  category: '',
  content: '',
  tags: ''
})

const qaFilter = ref({
  keyword: '',
  category: '',
  isSolved: null
})

const questionDialogVisible = ref(false)
const currentQuestion = ref(null)
const answers = ref([])
const loadingAnswers = ref(false)
const newAnswer = ref('')
const submittingAnswer = ref(false)

onMounted(() => {
  loadPosts()
})

const handleTabChange = (tab) => {
  if (tab === 'companion' && companions.value.length === 0) {
    loadCompanions()
  } else if (tab === 'qa' && questions.value.length === 0) {
    loadQuestions()
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPosts({ page: 1, size: 10 })
    posts.value = (res.records || []).map(p => ({ ...p, showComments: false, comments: [], newComment: '' }))
    hasMore.value = (res.records || []).length >= 10
    page.value = 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  loadingMore.value = true
  try {
    page.value++
    const res = await getPosts({ page: page.value, size: 10 })
    const newPosts = (res.records || []).map(p => ({ ...p, showComments: false, comments: [], newComment: '' }))
    posts.value.push(...newPosts)
    hasMore.value = (res.records || []).length >= 10
  } catch (e) {
    console.error(e)
  } finally {
    loadingMore.value = false
  }
}

const handlePublish = async () => {
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  
  publishing.value = true
  try {
    await createPost({
      content: newPost.value.content,
      location: newPost.value.location
    })
    ElMessage.success('发布成功，等待审核')
    newPost.value = { content: '', location: '' }
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const handleLikePost = async (post) => {
  try {
    await likePost(post.id)
    post.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && !post.commentsLoaded) {
    try {
      const res = await getComments({ targetType: 'POST', targetId: post.id, size: 50 })
      post.comments = res.records || []
      post.commentsLoaded = true
    } catch (e) {
      console.error(e)
    }
  }
}

const handleComment = async (post) => {
  if (!post.newComment?.trim()) return
  
  try {
    const comment = await createComment({
      targetType: 'POST',
      targetId: post.id,
      content: post.newComment
    })
    post.comments.unshift({
      ...comment,
      nickname: userStore.user?.nickname,
      username: userStore.user?.username,
      avatar: userStore.user?.avatar
    })
    post.commentCount++
    post.newComment = ''
    ElMessage.success('评论成功')
  } catch (e) {
    ElMessage.error('评论失败')
  }
}

const handleLikeComment = async (comment) => {
  try {
    await likeComment(comment.id)
    comment.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const parseImages = (images) => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}

// 结伴相关方法
const loadCompanions = async () => {
  loadingCompanions.value = true
  try {
    const res = await getCompanions({
      page: 1,
      size: 10,
      ...companionFilter.value
    })
    companions.value = res.records || []
    hasMoreCompanions.value = (res.records || []).length >= 10
    companionPage.value = 1
  } catch (e) {
    console.error(e)
  } finally {
    loadingCompanions.value = false
  }
}

const loadMoreCompanions = async () => {
  loadingMoreCompanions.value = true
  try {
    companionPage.value++
    const res = await getCompanions({
      page: companionPage.value,
      size: 10,
      ...companionFilter.value
    })
    const newCompanions = res.records || []
    companions.value.push(...newCompanions)
    hasMoreCompanions.value = newCompanions.length >= 10
  } catch (e) {
    console.error(e)
  } finally {
    loadingMoreCompanions.value = false
  }
}

const handlePublishCompanion = async () => {
  if (!newCompanion.value.title || !newCompanion.value.destination || !companionDateRange.value?.length) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  publishingCompanion.value = true
  try {
    await createCompanion({
      ...newCompanion.value,
      startDate: companionDateRange.value[0],
      endDate: companionDateRange.value[1]
    })
    ElMessage.success('发布成功，等待审核')
    resetCompanionForm()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    publishingCompanion.value = false
  }
}

const resetCompanionForm = () => {
  newCompanion.value = {
    title: '',
    destination: '',
    peopleNeeded: 1,
    budgetRange: '',
    travelStyle: '',
    contactMethod: '',
    contactInfo: '',
    content: '',
    requirements: ''
  }
  companionDateRange.value = []
}

const handleLikeCompanion = async (companion) => {
  try {
    await likeCompanion(companion.id)
    companion.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const handleApplyCompanion = async (companion) => {
  try {
    const { value: message } = await ElMessageBox.prompt('请输入申请留言', '申请加入', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '介绍一下自己，说明为什么想加入...'
    })
    
    await applyCompanion(companion.id, { message })
    companion.hasApplied = true
    ElMessage.success('申请已提交')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '申请失败')
    }
  }
}

const getBudgetLabel = (budget) => {
  const map = { LOW: '经济', MEDIUM: '中等', HIGH: '豪华' }
  return map[budget] || budget
}

const getStyleLabel = (style) => {
  const map = { LEISURE: '休闲', ADVENTURE: '冒险', CULTURE: '文化', FOOD: '美食' }
  return map[style] || style
}

// 问答相关方法
const loadQuestions = async () => {
  loadingQuestions.value = true
  try {
    const res = await getQuestions({
      page: 1,
      size: 10,
      ...qaFilter.value
    })
    questions.value = res.records || []
    hasMoreQuestions.value = (res.records || []).length >= 10
    questionPage.value = 1
  } catch (e) {
    console.error(e)
  } finally {
    loadingQuestions.value = false
  }
}

const loadMoreQuestions = async () => {
  loadingMoreQuestions.value = true
  try {
    questionPage.value++
    const res = await getQuestions({
      page: questionPage.value,
      size: 10,
      ...qaFilter.value
    })
    const newQuestions = res.records || []
    questions.value.push(...newQuestions)
    hasMoreQuestions.value = newQuestions.length >= 10
  } catch (e) {
    console.error(e)
  } finally {
    loadingMoreQuestions.value = false
  }
}

const handlePublishQuestion = async () => {
  if (!newQuestion.value.title || !newQuestion.value.content || !newQuestion.value.category) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  publishingQuestion.value = true
  try {
    await createQuestion(newQuestion.value)
    ElMessage.success('发布成功，等待审核')
    resetQuestionForm()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    publishingQuestion.value = false
  }
}

const resetQuestionForm = () => {
  newQuestion.value = {
    title: '',
    category: '',
    content: '',
    tags: ''
  }
}

const handleLikeQuestion = async (question) => {
  try {
    await likeQuestion(question.id)
    question.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const handleViewQuestion = async (question) => {
  currentQuestion.value = question
  questionDialogVisible.value = true
  loadAnswers(question.id)
}

const loadAnswers = async (questionId) => {
  loadingAnswers.value = true
  try {
    answers.value = await getAnswers(questionId)
  } catch (e) {
    console.error(e)
  } finally {
    loadingAnswers.value = false
  }
}

const handleSubmitAnswer = async () => {
  if (!newAnswer.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }
  
  submittingAnswer.value = true
  try {
    const answer = await createAnswer({
      questionId: currentQuestion.value.id,
      content: newAnswer.value
    })
    answers.value.unshift({
      ...answer,
      nickname: userStore.user?.nickname,
      username: userStore.user?.username,
      avatar: userStore.user?.avatar
    })
    currentQuestion.value.answerCount++
    newAnswer.value = ''
    ElMessage.success('回答成功')
  } catch (e) {
    ElMessage.error(e.message || '回答失败')
  } finally {
    submittingAnswer.value = false
  }
}

const handleLikeAnswer = async (answer) => {
  try {
    await likeAnswer(answer.id)
    answer.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const handleSetBestAnswer = async (answer) => {
  try {
    await ElMessageBox.confirm('确定设置为最佳答案吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await setBestAnswer(currentQuestion.value.id, answer.id)
    answer.isBest = 1
    currentQuestion.value.isSolved = 1
    currentQuestion.value.bestAnswerId = answer.id
    ElMessage.success('设置成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '设置失败')
    }
  }
}

const handleDeleteAnswer = async (answer) => {
  try {
    await ElMessageBox.confirm('确定删除这个回答吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAnswer(answer.id)
    answers.value = answers.value.filter(a => a.id !== answer.id)
    currentQuestion.value.answerCount--
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
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
.community-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.community-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 28px;
    color: #333;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.post-form {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  .form-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    
    span {
      font-weight: 500;
    }
  }
  
  .form-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
  }
}

.login-tip {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  p {
    color: #666;
    margin-bottom: 16px;
  }
}

.post-list {
  .post-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
  
  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .post-user {
    .username {
      font-weight: 500;
      font-size: 15px;
    }
    .post-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .post-content {
    font-size: 15px;
    line-height: 1.6;
    color: #333;
    margin-bottom: 12px;
    white-space: pre-wrap;
  }
  
  .post-images {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
    
    .post-image {
      width: 150px;
      height: 150px;
      border-radius: 8px;
    }
  }
  
  .post-location {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #409eff;
    font-size: 13px;
    margin-bottom: 12px;
  }
  
  .post-stats {
    display: flex;
    gap: 24px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 13px;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
}

.comment-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  
  .comment-list {
    max-height: 300px;
    overflow-y: auto;
  }
  
  .comment-item {
    display: flex;
    gap: 10px;
    margin-bottom: 12px;
  }
  
  .comment-content {
    flex: 1;
    
    .comment-user {
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .comment-text {
      font-size: 14px;
      color: #333;
      line-height: 1.5;
    }
    
    .comment-footer {
      display: flex;
      gap: 16px;
      margin-top: 4px;
      font-size: 12px;
      color: #999;
      
      .comment-like {
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 2px;
        
        &:hover {
          color: #409eff;
        }
      }
    }
  }
  
  .comment-input {
    margin-top: 12px;
  }
}

.load-more {
  text-align: center;
  padding: 20px;
}

.community-tabs {
  max-width: 800px;
  margin: 0 auto 20px;
}

.companion-content {
  max-width: 800px;
  margin: 0 auto;
}

.companion-form {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  h3 {
    margin-bottom: 16px;
    font-size: 16px;
    color: #333;
  }
}

.companion-filter {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 12px;
  align-items: center;
}

.companion-list {
  .companion-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
  
  .companion-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .companion-user {
    flex: 1;
    
    .username {
      font-weight: 500;
      font-size: 15px;
    }
    .companion-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .companion-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
  
  .companion-info {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 12px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 14px;
    }
  }
  
  .companion-content {
    font-size: 14px;
    line-height: 1.6;
    color: #666;
    margin-bottom: 12px;
    white-space: pre-wrap;
  }
  
  .companion-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
  
  .companion-stats {
    display: flex;
    gap: 24px;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 13px;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
}

.qa-content {
  max-width: 800px;
  margin: 0 auto;
}

.qa-form {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  h3 {
    margin-bottom: 16px;
    font-size: 16px;
    color: #333;
  }
}

.qa-filter {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 12px;
  align-items: center;
}

.qa-list {
  .qa-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }
  }
  
  .qa-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .qa-user {
    flex: 1;
    
    .username {
      font-weight: 500;
      font-size: 14px;
    }
    .qa-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .qa-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    line-height: 1.4;
  }
  
  .qa-content-text {
    font-size: 14px;
    line-height: 1.6;
    color: #666;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .qa-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .qa-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
  
  .qa-stats {
    display: flex;
    gap: 24px;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 13px;
      
      &:hover {
        color: #409eff;
      }
    }
  }
}

.question-detail {
  .question-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .question-user {
    flex: 1;
    
    .username {
      font-weight: 500;
      font-size: 15px;
    }
    .question-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .question-title {
    font-size: 22px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
    line-height: 1.4;
  }
  
  .question-content {
    font-size: 15px;
    line-height: 1.8;
    color: #333;
    margin-bottom: 16px;
    white-space: pre-wrap;
  }
  
  .question-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .question-stats {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 14px;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
  }
  
  .answer-list {
    max-height: 500px;
    overflow-y: auto;
    margin-bottom: 20px;
  }
  
  .answer-item {
    padding: 16px;
    margin-bottom: 12px;
    background: #f8f9fa;
    border-radius: 8px;
  }
  
  .answer-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
  }
  
  .answer-user {
    flex: 1;
    
    .username {
      font-weight: 500;
      font-size: 14px;
    }
    .answer-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .answer-content {
    font-size: 14px;
    line-height: 1.6;
    color: #333;
    margin-bottom: 12px;
    white-space: pre-wrap;
  }
  
  .answer-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .answer-like {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 13px;
      cursor: pointer;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .answer-input {
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 12px;
    }
  }
}
</style>
