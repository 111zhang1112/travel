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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 结伴申请服务
 */
@Service
@RequiredArgsConstructor
public class CompanionApplicationService extends ServiceImpl<CompanionApplicationMapper, CompanionApplication> {

    private final TravelCompanionService companionService;

    /**
     * 分页查询申请（带用户信息）
     */
    public PageResult<CompanionApplication> getApplicationList(
            Integer page, Integer size, Long companionId, Long userId, Integer status) {
        Page<CompanionApplication> pageParam = new Page<>(page, size);
        IPage<CompanionApplication> result = baseMapper.selectPageWithUser(pageParam, companionId, userId, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 申请加入
     */
    @Transactional
    public void apply(Long companionId, Long userId, String message) {
        TravelCompanion companion = companionService.getById(companionId);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (companion.getStatus() != 1) {
            throw new BusinessException(400, "该结伴已结束招募");
        }
        if (companion.getUserId().equals(userId)) {
            throw new BusinessException(400, "不能申请自己发布的结伴");
        }
        
        // 检查是否已申请
        Long count = count(new LambdaQueryWrapper<CompanionApplication>()
                .eq(CompanionApplication::getCompanionId, companionId)
                .eq(CompanionApplication::getUserId, userId));
        if (count > 0) {
            throw new BusinessException(400, "已申请过该结伴");
        }
        
        CompanionApplication application = new CompanionApplication();
        application.setCompanionId(companionId);
        application.setUserId(userId);
        application.setMessage(message);
        application.setStatus(0);
        save(application);
    }

    /**
     * 同意申请
     */
    @Transactional
    public void approve(Long id, Long ownerId, String reply) {
        CompanionApplication application = getById(id);
        if (application == null) {
            throw new BusinessException(404, "申请不存在");
        }
        
        TravelCompanion companion = companionService.getById(application.getCompanionId());
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!companion.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理");
        }
        
        // 检查是否已满员
        if (companion.getCurrentPeople() >= companion.getPeopleNeeded() + 1) {
            throw new BusinessException(400, "已满员");
        }
        
        application.setStatus(1);
        application.setReply(reply);
        updateById(application);
        
        // 更新当前人数
        companion.setCurrentPeople(companion.getCurrentPeople() + 1);
        // 如果满员，更新状态
        if (companion.getCurrentPeople() >= companion.getPeopleNeeded() + 1) {
            companion.setStatus(2);
        }
        companionService.updateById(companion);
    }

    /**
     * 拒绝申请
     */
    @Transactional
    public void reject(Long id, Long ownerId, String reply) {
        CompanionApplication application = getById(id);
        if (application == null) {
            throw new BusinessException(404, "申请不存在");
        }
        
        TravelCompanion companion = companionService.getById(application.getCompanionId());
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!companion.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理");
        }
        
        application.setStatus(2);
        application.setReply(reply);
        updateById(application);
    }
}
