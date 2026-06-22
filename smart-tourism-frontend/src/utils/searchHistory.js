/**
 * 搜索历史本地存储工具
 */

const STORAGE_KEY = 'searchHistory'
const MAX_HISTORY_SIZE = 10

/**
 * 保存搜索历史到本地存储
 * @param {string} keyword - 搜索关键词
 * @param {string} searchType - 搜索类型
 */
export function saveLocalSearchHistory(keyword, searchType = 'all') {
  if (!keyword || keyword.trim() === '') {
    return
  }
  
  try {
    let history = getLocalSearchHistory()
    
    // 检查是否已存在，如果存在则移除旧的
    history = history.filter(item => item.keyword !== keyword)
    
    // 添加新记录到开头
    history.unshift({
      id: Date.now(), // 使用时间戳作为ID
      keyword: keyword.trim(),
      searchType,
      searchTime: new Date().toISOString()
    })
    
    // 限制历史记录数量
    if (history.length > MAX_HISTORY_SIZE) {
      history = history.slice(0, MAX_HISTORY_SIZE)
    }
    
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history))
  } catch (error) {
    console.error('保存本地搜索历史失败:', error)
  }
}

/**
 * 获取本地搜索历史
 * @returns {Array} 搜索历史列表
 */
export function getLocalSearchHistory() {
  try {
    const history = localStorage.getItem(STORAGE_KEY)
    return history ? JSON.parse(history) : []
  } catch (error) {
    console.error('获取本地搜索历史失败:', error)
    return []
  }
}

/**
 * 删除单条本地搜索历史
 * @param {number} id - 记录ID
 */
export function deleteLocalSearchHistory(id) {
  try {
    let history = getLocalSearchHistory()
    history = history.filter(item => item.id !== id)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history))
  } catch (error) {
    console.error('删除本地搜索历史失败:', error)
  }
}

/**
 * 清空本地搜索历史
 */
export function clearLocalSearchHistory() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch (error) {
    console.error('清空本地搜索历史失败:', error)
  }
}

/**
 * 合并服务器和本地搜索历史
 * @param {Array} serverHistory - 服务器历史记录
 * @returns {Array} 合并后的历史记录
 */
export function mergeSearchHistory(serverHistory) {
  const localHistory = getLocalSearchHistory()
  
  // 如果有服务器历史，优先使用服务器历史
  if (serverHistory && serverHistory.length > 0) {
    return serverHistory
  }
  
  return localHistory
}
