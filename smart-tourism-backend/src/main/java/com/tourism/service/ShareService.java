package com.tourism.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.ShareRecord;
import com.tourism.mapper.ShareRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 分享服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService extends ServiceImpl<ShareRecordMapper, ShareRecord> {

    private final ShareStatisticsService shareStatisticsService;
    private final PointsService pointsService;

    /**
     * 创建分享记录
     */
    @Transactional
    public ShareRecord createShare(String contentType, Long contentId, String platform) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户也可以分享
        }

        // 检查今日分享次数(仅对登录用户)
        int todayCount = 0;
        if (userId != null) {
            todayCount = baseMapper.getTodayShareCount(userId);
            if (todayCount >= 10) {
                throw new RuntimeException("今日分享次数已达上限");
            }

            // 检查24小时内是否已分享过该内容
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            int recentCount = baseMapper.checkRecentShare(userId, contentType, contentId, since);
            if (recentCount > 0) {
                // 已分享过,返回之前的记录
                return baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShareRecord>()
                        .eq(ShareRecord::getUserId, userId)
                        .eq(ShareRecord::getContentType, contentType)
                        .eq(ShareRecord::getContentId, contentId)
                        .ge(ShareRecord::getCreateTime, since)
                        .last("LIMIT 1")
                );
            }
        }

        // 生成分享链接
        String shareUrl = generateShareUrl(contentType, contentId);

        // 创建分享记录
        ShareRecord record = new ShareRecord();
        record.setUserId(userId);
        record.setContentType(contentType);
        record.setContentId(contentId);
        record.setPlatform(platform);
        record.setShareUrl(shareUrl);
        record.setViewCount(0);
        save(record);

        // 更新统计数据
        shareStatisticsService.incrementShareCount(contentType, contentId);

        // 奖励积分(仅登录用户,且每天最多3次分享奖励)
        if (userId != null && todayCount < 3) {
            try {
                pointsService.addPoints(userId, 5, "分享内容");
                log.info("用户 {} 分享内容获得5积分", userId);
            } catch (Exception e) {
                log.error("分享积分奖励失败: userId={}", userId, e);
            }
        }

        return record;
    }

    /**
     * 生成分享链接
     */
    private String generateShareUrl(String contentType, Long contentId) {
        // 使用Base64编码生成短链接
        String data = contentType + ":" + contentId;
        String encoded = Base64.getUrlEncoder().encodeToString(data.getBytes());
        return "/s/" + encoded;
    }

    /**
     * 解析分享链接
     */
    public ShareRecord getByShareUrl(String shareUrl) {
        return baseMapper.getByShareUrl(shareUrl);
    }

    /**
     * 记录访问
     */
    @Transactional
    public void recordView(Long shareId) {
        ShareRecord record = getById(shareId);
        if (record == null) {
            return;
        }

        // 增加访问次数
        baseMapper.incrementViewCount(shareId);

        // 更新统计数据
        shareStatisticsService.incrementViewCount(record.getContentType(), record.getContentId());

        // 奖励分享者积分(每个分享链接最多奖励10次访问)
        if (record.getUserId() != null && record.getViewCount() < 10) {
            try {
                pointsService.addPoints(record.getUserId(), 2, "分享被访问");
                log.info("用户 {} 的分享被访问获得2积分", record.getUserId());
            } catch (Exception e) {
                log.error("分享访问积分奖励失败: userId={}", record.getUserId(), e);
            }
        }
    }
}
