import request from './request'

/**
 * 获取问题列表
 */
export function getQuestions(params) {
  return request({
    url: '/qa/questions',
    method: 'get',
    params
  })
}

/**
 * 获取问题详情
 */
export function getQuestionDetail(id) {
  return request({
    url: `/qa/questions/${id}`,
    method: 'get'
  })
}

/**
 * 发布问题
 */
export function createQuestion(data) {
  return request({
    url: '/qa/questions',
    method: 'post',
    data
  })
}

/**
 * 点赞问题
 */
export function likeQuestion(id) {
  return request({
    url: `/qa/questions/${id}/like`,
    method: 'post'
  })
}

/**
 * 获取问题的答案列表
 */
export function getAnswers(questionId) {
  return request({
    url: `/qa/questions/${questionId}/answers`,
    method: 'get'
  })
}

/**
 * 回答问题
 */
export function createAnswer(data) {
  return request({
    url: '/qa/answers',
    method: 'post',
    data
  })
}

/**
 * 点赞答案
 */
export function likeAnswer(id) {
  return request({
    url: `/qa/answers/${id}/like`,
    method: 'post'
  })
}

/**
 * 设置最佳答案
 */
export function setBestAnswer(questionId, answerId) {
  return request({
    url: `/qa/questions/${questionId}/best-answer/${answerId}`,
    method: 'post'
  })
}

/**
 * 删除答案
 */
export function deleteAnswer(id) {
  return request({
    url: `/qa/answers/${id}`,
    method: 'delete'
  })
}
