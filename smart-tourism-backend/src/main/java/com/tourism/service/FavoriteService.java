package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.dto.FavoriteDTO;
import com.tourism.entity.Hotel;
import com.tourism.entity.Interaction;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.InteractionMapper;
import com.tourism.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final InteractionMapper interactionMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final HotelMapper hotelMapper;

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        wrapper.eq(Interaction::getTargetType, targetType);
        wrapper.eq(Interaction::getTargetId, targetId);
        wrapper.eq(Interaction::getActionType, "FAVORITE");
        
        if (interactionMapper.selectCount(wrapper) > 0) {
            return; // 已收藏，不重复添加
        }
        
        // 添加收藏记录
        Interaction interaction = new Interaction();
        interaction.setUserId(userId);
        interaction.setTargetType(targetType);
        interaction.setTargetId(targetId);
        interaction.setActionType("FAVORITE");
        interactionMapper.insert(interaction);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        wrapper.eq(Interaction::getTargetType, targetType);
        wrapper.eq(Interaction::getTargetId, targetId);
        wrapper.eq(Interaction::getActionType, "FAVORITE");
        
        interactionMapper.delete(wrapper);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        wrapper.eq(Interaction::getTargetType, targetType);
        wrapper.eq(Interaction::getTargetId, targetId);
        wrapper.eq(Interaction::getActionType, "FAVORITE");
        
        return interactionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取用户收藏列表
     */
    public List<FavoriteDTO> getUserFavorites(Long userId, String targetType) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        wrapper.eq(Interaction::getActionType, "FAVORITE");
        
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Interaction::getTargetType, targetType);
        }
        
        wrapper.orderByDesc(Interaction::getCreateTime);
        
        List<Interaction> interactions = interactionMapper.selectList(wrapper);
        List<FavoriteDTO> favorites = new ArrayList<>();
        
        for (Interaction interaction : interactions) {
            FavoriteDTO dto = new FavoriteDTO();
            dto.setId(interaction.getId());
            dto.setTargetType(interaction.getTargetType());
            dto.setTargetId(interaction.getTargetId());
            dto.setCreateTime(interaction.getCreateTime());
            
            // 根据类型查询详细信息
            if ("SCENIC".equals(interaction.getTargetType())) {
                ScenicSpot scenic = scenicSpotMapper.selectById(interaction.getTargetId());
                if (scenic != null) {
                    dto.setName(scenic.getName());
                    dto.setLocation(scenic.getLocation());
                    dto.setRating(scenic.getRating() != null ? scenic.getRating().doubleValue() : null);
                    dto.setPrice(scenic.getTicketPrice() != null ? scenic.getTicketPrice().doubleValue() : null);
                    
                    // 获取第一张图片
                    String images = scenic.getImages();
                    if (images != null && !images.isEmpty()) {
                        try {
                            // 简单解析JSON数组的第一个元素
                            String firstImage = images.replace("[", "")
                                                     .replace("]", "")
                                                     .replace("\"", "")
                                                     .split(",")[0]
                                                     .trim();
                            dto.setImage(firstImage);
                        } catch (Exception e) {
                            dto.setImage(null);
                        }
                    }
                    
                    favorites.add(dto);
                }
            } else if ("HOTEL".equals(interaction.getTargetType())) {
                Hotel hotel = hotelMapper.selectById(interaction.getTargetId());
                if (hotel != null) {
                    dto.setName(hotel.getName());
                    dto.setLocation(hotel.getAddress());
                    dto.setRating(hotel.getRating() != null ? hotel.getRating().doubleValue() : null);
                    dto.setPrice(hotel.getPriceMin() != null ? hotel.getPriceMin().doubleValue() : null);
                    
                    // 获取第一张图片
                    String images = hotel.getImages();
                    if (images != null && !images.isEmpty()) {
                        try {
                            String firstImage = images.replace("[", "")
                                                     .replace("]", "")
                                                     .replace("\"", "")
                                                     .split(",")[0]
                                                     .trim();
                            dto.setImage(firstImage);
                        } catch (Exception e) {
                            dto.setImage(null);
                        }
                    }
                    
                    favorites.add(dto);
                }
            }
        }
        
        return favorites;
    }
}
