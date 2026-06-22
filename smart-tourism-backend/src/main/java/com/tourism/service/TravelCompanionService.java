package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.CompanionApplication;
import com.tourism.entity.TravelCompanion;
import com.tourism.mapper.CompanionApplicationMapper;
import com.tourism.mapper.TravelCompanionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 结伴服务
 */
@Service
@RequiredArgsConstructor
public class TravelCompanionService extends ServiceImpl<TravelCompanionMapper, TravelCompanion> {

    private final CompanionApplicationMapper applicationMapper;

    /**
     * 分页查询结伴（带用户信息）
     */
    public PageResult<TravelCompanion> getCompanionList(
            Integer page, Integer size, Integer status, Long userId,
            String destination, String budgetRange, String travelStyle,
            String startDate, String endDate, Long currentUserId) {
        Page<TravelCompanion> pageParam = new Page<>(page, size);
        IPage<TravelCompanion> result = baseMapper.selectPageWithUser(
                pageParam, status, userId, destination, budgetRange, travelStyle, startDate, endDate
        );
        
        // 检查当前用户是否已申请
        if (currentUserId != null) {
            for (TravelCompanion companion : result.getRecords()) {
                Long count = applicationMapper.selectCount(new LambdaQueryWrapper<CompanionApplication>()
                        .eq(CompanionApplication::getCompanionId, companion.getId())
                        .eq(CompanionApplication::getUserId, currentUserId));
                companion.setHasApplied(count > 0);
            }
        }
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approve(Long id) {
        TravelCompanion companion = getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (companion.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核的结伴信息");
        }
        companion.setStatus(1);
        updateById(companion);
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void reject(Long id, String reason) {
        TravelCompanion companion = getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (companion.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核的结伴信息");
        }
        companion.setStatus(4);
        companion.setRejectReason(reason);
        updateById(companion);
    }

    /**
     * 删除结伴（软删除）
     */
    @Transactional
    public void softDelete(Long id) {
        TravelCompanion companion = getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        companion.setStatus(5);
        updateById(companion);
    }

    /**
     * 结束招募
     */
    @Transactional
    public void endRecruit(Long id, Long userId) {
        TravelCompanion companion = getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!companion.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (companion.getStatus() != 1) {
            throw new BusinessException(400, "只能结束招募中的结伴");
        }
        companion.setStatus(3);
        updateById(companion);
    }

    /**
     * 获取统计数据
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", baseMapper.countByStatus(0));
        stats.put("recruiting", baseMapper.countByStatus(1));
        stats.put("full", baseMapper.countByStatus(2));
        stats.put("ended", baseMapper.countByStatus(3));
        stats.put("rejected", baseMapper.countByStatus(4));
        stats.put("deleted", baseMapper.countByStatus(5));
        stats.put("total", count());
        return stats;
    }
}
