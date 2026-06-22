package com.tourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.Comment;
import com.tourism.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    /**
     * 分页查询评论（带用户信息）
     */
    public PageResult<Comment> getCommentList(Integer page, Integer size, Integer status, 
                                               String targetType, Long targetId, String keyword) {
        Page<Comment> pageParam = new Page<>(page, size);
        IPage<Comment> result = baseMapper.selectPageWithUser(pageParam, status, targetType, targetId, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 删除评论（软删除）
     */
    @Transactional
    public void softDelete(Long id) {
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        comment.setStatus(0);
        updateById(comment);
    }

    /**
     * 恢复评论
     */
    @Transactional
    public void restore(Long id) {
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (comment.getStatus() != 0) {
            throw new BusinessException(400, "只能恢复已删除的评论");
        }
        comment.setStatus(1);
        updateById(comment);
    }

    /**
     * 批量删除评论
     */
    @Transactional
    public void batchDelete(java.util.List<Long> ids) {
        for (Long id : ids) {
            Comment comment = getById(id);
            if (comment != null) {
                comment.setStatus(0);
                updateById(comment);
            }
        }
    }

    /**
     * 获取统计数据
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("deleted", baseMapper.countByStatus(0));
        stats.put("normal", baseMapper.countByStatus(1));
        stats.put("pending", baseMapper.countByStatus(2));
        stats.put("total", count());
        return stats;
    }
}
