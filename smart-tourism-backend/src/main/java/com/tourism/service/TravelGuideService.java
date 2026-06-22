package com.tourism.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.SysUser;
import com.tourism.entity.TravelGuide;
import com.tourism.mapper.TravelGuideMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 旅游攻略服务
 */
@Service
@RequiredArgsConstructor
public class TravelGuideService extends ServiceImpl<TravelGuideMapper, TravelGuide> {

    private final UserService userService;

    /**
     * 公开列表（只显示已发布的）
     */
    public PageResult<TravelGuide> getPublicList(Integer page, Integer size, String keyword) {
        Page<TravelGuide> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TravelGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelGuide::getStatus, 1);  // 已发布

        if (StringUtils.hasText(keyword)) {
            wrapper.like(TravelGuide::getTitle, keyword);
        }

        wrapper.orderByDesc(TravelGuide::getCreateTime);
        Page<TravelGuide> result = page(pageParam, wrapper);

        // 填充作者信息
        result.getRecords().forEach(this::fillAuthorInfo);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取详情
     */
    @Transactional
    public TravelGuide getDetail(Long id) {
        TravelGuide guide = getById(id);
        if (guide == null || guide.getStatus() != 1) {
            throw new BusinessException(404, "攻略不存在");
        }

        // 增加浏览量
        guide.setViewCount(guide.getViewCount() + 1);
        updateById(guide);

        fillAuthorInfo(guide);
        return guide;
    }

    /**
     * 创建攻略
     */
    @Transactional
    public TravelGuide create(TravelGuide guide) {
        if (!StringUtils.hasText(guide.getTitle())) {
            throw new BusinessException(400, "标题不能为空");
        }

        guide.setUserId(StpUtil.getLoginIdAsLong());
        guide.setViewCount(0);
        guide.setLikeCount(0);
        guide.setCollectCount(0);
        guide.setStatus(0);  // 待审核
        save(guide);
        return guide;
    }

    /**
     * 更新攻略
     */
    @Transactional
    public TravelGuide update(Long id, TravelGuide guide) {
        TravelGuide existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "攻略不存在");
        }

        // 只能修改自己的攻略
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!existing.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权修改");
        }

        guide.setId(id);
        guide.setStatus(0);  // 修改后重新审核
        updateById(guide);
        return getById(id);
    }

    // ========== 管理端方法 ==========

    /**
     * 管理端列表
     */
    public PageResult<TravelGuide> getAdminList(Integer page, Integer size, Integer status) {
        Page<TravelGuide> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TravelGuide> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(TravelGuide::getStatus, status);
        }

        wrapper.orderByDesc(TravelGuide::getCreateTime);
        Page<TravelGuide> result = page(pageParam, wrapper);

        result.getRecords().forEach(this::fillAuthorInfo);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approve(Long id) {
        TravelGuide guide = getById(id);
        if (guide == null) {
            throw new BusinessException(404, "攻略不存在");
        }
        if (guide.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核状态的攻略");
        }

        guide.setStatus(1);
        updateById(guide);
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void reject(Long id) {
        TravelGuide guide = getById(id);
        if (guide == null) {
            throw new BusinessException(404, "攻略不存在");
        }
        if (guide.getStatus() != 0) {
            throw new BusinessException(400, "只能审核待审核状态的攻略");
        }

        guide.setStatus(2);
        updateById(guide);
    }

    private void fillAuthorInfo(TravelGuide guide) {
        SysUser user = userService.getUserById(guide.getUserId());
        if (user != null) {
            guide.setAuthorName(user.getNickname());
            guide.setAuthorAvatar(user.getAvatar());
        }
    }
}
