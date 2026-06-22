package com.tourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.CommunityPost;
import com.tourism.mapper.CommunityPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 社区动态服务
 */
@Service
@RequiredArgsConstructor
public class CommunityPostService extends ServiceImpl<CommunityPostMapper, CommunityPost> {

    /**
     * 分页查询动态（带用户信息）
     */
    public PageResult<CommunityPost> getPostList(Integer page, Integer size, Integer status, Long userId, String keyword) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        IPage<CommunityPost> result = baseMapper.selectPageWithUser(pageParam, status, userId, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取动态详情（带用户信息）
     */
    public CommunityPost getPostDetailWithUser(Long id) {
        return baseMapper.selectByIdWithUser(id);
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approve(Long id) {
        CommunityPost post = getById(id);
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        if (post.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核的动态");
        }
        post.setStatus(1);
        updateById(post);
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void reject(Long id, String reason) {
        CommunityPost post = getById(id);
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        if (post.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核的动态");
        }
        post.setStatus(2);
        post.setRejectReason(reason);
        updateById(post);
    }

    /**
     * 删除动态（软删除）
     */
    @Transactional
    public void softDelete(Long id) {
        CommunityPost post = getById(id);
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        post.setStatus(3);
        updateById(post);
    }

    /**
     * 恢复动态
     */
    @Transactional
    public void restore(Long id) {
        CommunityPost post = getById(id);
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        if (post.getStatus() != 3) {
            throw new BusinessException(400, "只能恢复已删除的动态");
        }
        post.setStatus(1);
        updateById(post);
    }

    /**
     * 获取统计数据
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", baseMapper.countByStatus(0));
        stats.put("published", baseMapper.countByStatus(1));
        stats.put("rejected", baseMapper.countByStatus(2));
        stats.put("deleted", baseMapper.countByStatus(3));
        stats.put("total", count());
        return stats;
    }
}





























