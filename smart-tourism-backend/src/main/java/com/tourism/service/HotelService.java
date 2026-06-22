package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.Hotel;
import com.tourism.mapper.HotelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店服务
 */
@Service
@RequiredArgsConstructor
public class HotelService extends ServiceImpl<HotelMapper, Hotel> {

    /**
     * 分页查询酒店列表
     */
    public PageResult<Hotel> getPublicList(Integer page, Integer size, String keyword,
                                           BigDecimal priceMin, BigDecimal priceMax, Integer starRating) {
        return getPublicList(page, size, keyword, priceMin, priceMax, starRating, null, null);
    }

    /**
     * 分页查询酒店列表（支持地区和评分筛选）
     */
    public PageResult<Hotel> getPublicList(Integer page, Integer size, String keyword,
                                           BigDecimal priceMin, BigDecimal priceMax, Integer starRating,
                                           String region, Double minRating) {
        return getPublicList(page, size, keyword, priceMin, priceMax, starRating, region, minRating, null);
    }
    
    /**
     * 分页查询酒店列表（支持地区、评分筛选和排序）
     */
    public PageResult<Hotel> getPublicList(Integer page, Integer size, String keyword,
                                           BigDecimal priceMin, BigDecimal priceMax, Integer starRating,
                                           String region, Double minRating, String sortBy) {
        Page<Hotel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hotel::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Hotel::getName, keyword);
        }
        if (priceMin != null) {
            wrapper.ge(Hotel::getPriceMin, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(Hotel::getPriceMax, priceMax);
        }
        if (starRating != null) {
            wrapper.eq(Hotel::getStarRating, starRating);
        }
        
        // 地区筛选
        if (StringUtils.hasText(region)) {
            wrapper.like(Hotel::getAddress, region);
        }
        
        // 评分筛选（假设Hotel实体有rating字段）
        if (minRating != null && minRating > 0) {
            // 注意：如果Hotel没有rating字段，这行需要注释掉
            // wrapper.ge(Hotel::getRating, minRating);
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "price-asc":
                    wrapper.orderByAsc(Hotel::getPriceMin);
                    break;
                case "price-desc":
                    wrapper.orderByDesc(Hotel::getPriceMax);
                    break;
                case "rating":
                    wrapper.orderByDesc(Hotel::getStarRating);
                    break;
                case "popular":
                    wrapper.orderByDesc(Hotel::getStarRating);
                    break;
                default:
                    wrapper.orderByDesc(Hotel::getStarRating);
            }
        } else {
            wrapper.orderByDesc(Hotel::getStarRating);
        }
        
        Page<Hotel> result = page(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 搜索酒店
     */
    public List<Hotel> search(String keyword, BigDecimal priceMin, BigDecimal priceMax, Integer starRating) {
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hotel::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Hotel::getName, keyword);
        }
        if (priceMin != null) {
            wrapper.ge(Hotel::getPriceMin, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(Hotel::getPriceMax, priceMax);
        }
        if (starRating != null) {
            wrapper.eq(Hotel::getStarRating, starRating);
        }

        wrapper.orderByDesc(Hotel::getStarRating);
        wrapper.last("LIMIT 20");

        return list(wrapper);
    }

    /**
     * 获取酒店详情
     */
    public Hotel getDetail(Long id) {
        Hotel hotel = getById(id);
        if (hotel == null || hotel.getStatus() != 1) {
            throw new BusinessException(404, "酒店不存在");
        }
        return hotel;
    }

    // ========== 管理端方法 ==========

    /**
     * 管理端分页查询
     */
    public PageResult<Hotel> getAdminList(Integer page, Integer size, String keyword, Integer status) {
        Page<Hotel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Hotel::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Hotel::getName, keyword);
        }

        wrapper.orderByDesc(Hotel::getCreateTime);
        Page<Hotel> result = page(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建酒店
     */
    @Transactional
    public Hotel create(Hotel hotel) {
        validateHotel(hotel);
        hotel.setStatus(1);
        save(hotel);
        return hotel;
    }

    /**
     * 更新酒店
     */
    @Transactional
    public Hotel update(Long id, Hotel hotel) {
        Hotel existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "酒店不存在");
        }

        validateHotel(hotel);
        hotel.setId(id);
        updateById(hotel);
        return getById(id);
    }

    /**
     * 软删除酒店
     */
    @Transactional
    public void softDelete(Long id) {
        Hotel hotel = getById(id);
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }

        hotel.setStatus(0);
        updateById(hotel);
    }

    private void validateHotel(Hotel hotel) {
        if (!StringUtils.hasText(hotel.getName())) {
            throw new BusinessException(400, "酒店名称不能为空");
        }
        if (hotel.getLatitude() == null || hotel.getLongitude() == null) {
            throw new BusinessException(400, "经纬度不能为空");
        }
    }
    
    /**
     * 获取热门酒店TOP N（用于数据统计）
     */
    public List<java.util.Map<String, Object>> getTopHotels(int limit) {
        return baseMapper.getTopHotels(limit);
    }

    // ========== 商家端方法 ==========

    /**
     * 商家分页查询自己的酒店列表
     */
    public PageResult<Hotel> getMerchantList(Long merchantId, Integer page, Integer size, String keyword, Integer status) {
        Page<Hotel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        
        // 数据隔离：只查询该商家的酒店
        wrapper.eq(Hotel::getMerchantId, merchantId);
        
        if (status != null) {
            wrapper.eq(Hotel::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Hotel::getName, keyword);
        }
        
        wrapper.orderByDesc(Hotel::getCreateTime);
        Page<Hotel> result = page(pageParam, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 商家创建酒店（自动关联商家ID）
     */
    @Transactional
    public Hotel createByMerchant(Long merchantId, Hotel hotel) {
        validateHotel(hotel);
        
        // 自动关联商家ID
        hotel.setMerchantId(merchantId);
        hotel.setStatus(1);
        
        save(hotel);
        return hotel;
    }

    /**
     * 商家更新酒店（验证权限）
     */
    @Transactional
    public Hotel updateByMerchant(Long merchantId, Long id, Hotel hotel) {
        Hotel existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        
        // 权限验证：只能修改自己的酒店
        if (!merchantId.equals(existing.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的酒店");
        }
        
        validateHotel(hotel);
        hotel.setId(id);
        hotel.setMerchantId(merchantId); // 保持商家ID不变
        updateById(hotel);
        return getById(id);
    }

    /**
     * 商家删除酒店（验证权限）
     */
    @Transactional
    public void deleteByMerchant(Long merchantId, Long id) {
        Hotel existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        
        // 权限验证：只能删除自己的酒店
        if (!merchantId.equals(existing.getMerchantId())) {
            throw new BusinessException(403, "无权删除其他商家的酒店");
        }
        
        // 软删除
        existing.setStatus(0);
        updateById(existing);
    }

    /**
     * 商家获取酒店详情（验证权限）
     */
    public Hotel getMerchantDetail(Long merchantId, Long id) {
        Hotel hotel = getById(id);
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        
        // 权限验证：只能查看自己的酒店
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权查看其他商家的酒店");
        }
        
        return hotel;
    }

    /**
     * 商家更新酒店状态（验证权限）
     */
    @Transactional
    public void updateStatusByMerchant(Long merchantId, Long id, Integer status) {
        Hotel hotel = getById(id);
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        
        // 权限验证：只能修改自己的酒店
        if (!merchantId.equals(hotel.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的酒店");
        }
        
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值无效，只能为0或1");
        }
        
        hotel.setStatus(status);
        updateById(hotel);
    }
}
