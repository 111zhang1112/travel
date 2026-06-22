package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.entity.Category;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 景点服务
 */
@Service
@RequiredArgsConstructor
public class ScenicSpotService extends ServiceImpl<ScenicSpotMapper, ScenicSpot> {

    private final CategoryService categoryService;

    /**
     * 分页查询景点列表（公开接口，只返回上架的）
     */
    public PageResult<ScenicSpot> getPublicList(Integer page, Integer size, String keyword) {
        return getPublicList(page, size, keyword, null, null, null, null);
    }

    /**
     * 分页查询景点列表（公开接口，支持筛选）
     */
    public PageResult<ScenicSpot> getPublicList(Integer page, Integer size, String keyword, 
                                                 java.math.BigDecimal priceMin, java.math.BigDecimal priceMax, 
                                                 String region, Double minRating) {
        return getPublicList(page, size, keyword, priceMin, priceMax, region, minRating, null, null);
    }
    
    /**
     * 分页查询景点列表（公开接口，支持筛选和排序）
     */
    public PageResult<ScenicSpot> getPublicList(Integer page, Integer size, String keyword, 
                                                 java.math.BigDecimal priceMin, java.math.BigDecimal priceMax, 
                                                 String region, Double minRating, Long categoryId, String sortBy) {
        Page<ScenicSpot> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);  // 只查询上架的
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ScenicSpot::getName, keyword)
                    .or()
                    .like(ScenicSpot::getDescription, keyword)
                    .or()
                    .like(ScenicSpot::getTags, keyword)
            );
        }
        
        // 价格筛选
        if (priceMin != null) {
            wrapper.ge(ScenicSpot::getTicketPrice, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(ScenicSpot::getTicketPrice, priceMax);
        }
        
        // 地区筛选
        if (StringUtils.hasText(region)) {
            wrapper.eq(ScenicSpot::getLocation, region);
        }
        
        // 评分筛选
        if (minRating != null && minRating > 0) {
            wrapper.ge(ScenicSpot::getRating, minRating);
        }
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(ScenicSpot::getCategoryId, categoryId);
        }
        
        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "price-asc":
                    wrapper.orderByAsc(ScenicSpot::getTicketPrice);
                    break;
                case "price-desc":
                    wrapper.orderByDesc(ScenicSpot::getTicketPrice);
                    break;
                case "rating":
                    wrapper.orderByDesc(ScenicSpot::getRating);
                    break;
                case "popular":
                    wrapper.orderByDesc(ScenicSpot::getViewCount);
                    break;
                default:
                    wrapper.orderByDesc(ScenicSpot::getViewCount);
            }
        } else {
            wrapper.orderByDesc(ScenicSpot::getViewCount);
        }
        
        Page<ScenicSpot> result = page(pageParam, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 搜索景点
     */
    public List<ScenicSpot> search(String keyword) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ScenicSpot::getName, keyword)
                    .or()
                    .like(ScenicSpot::getDescription, keyword)
                    .or()
                    .like(ScenicSpot::getTags, keyword)
            );
        }
        
        wrapper.orderByDesc(ScenicSpot::getRating);
        wrapper.last("LIMIT 20");
        
        return list(wrapper);
    }

    /**
     * 获取景点详情（增加浏览量）
     */
    @Transactional
    public ScenicSpot getDetail(Long id) {
        ScenicSpot spot = getById(id);
        if (spot == null || spot.getStatus() != 1) {
            throw new BusinessException(404, "景点不存在");
        }
        
        // 增加浏览量
        spot.setViewCount(spot.getViewCount() + 1);
        updateById(spot);
        
        return spot;
    }

    /**
     * 获取热门景点
     */
    public List<ScenicSpot> getPopular(int limit) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);
        wrapper.orderByDesc(ScenicSpot::getViewCount);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    /**
     * 获取高评分景点
     */
    public List<ScenicSpot> getTopRated(int limit) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);
        wrapper.orderByDesc(ScenicSpot::getRating);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    /**
     * 按地区查询景点（使用location字段）
     */
    public List<ScenicSpot> getByRegion(String location, int limit) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);
        wrapper.eq(ScenicSpot::getLocation, location);
        wrapper.orderByDesc(ScenicSpot::getRating);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    /**
     * 获取所有地区列表（去重，使用location字段）
     */
    public List<String> getAllRegions() {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpot::getStatus, 1);
        wrapper.select(ScenicSpot::getLocation);
        wrapper.isNotNull(ScenicSpot::getLocation);
        wrapper.ne(ScenicSpot::getLocation, "");
        wrapper.groupBy(ScenicSpot::getLocation);
        
        return list(wrapper).stream()
                .map(ScenicSpot::getLocation)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    // ========== 管理端方法 ==========

    /**
     * 管理端分页查询（包含所有状态，支持分类筛选和分类名称关联）
     */
    public PageResult<ScenicSpot> getAdminList(Integer page, Integer size, String keyword, Integer status, Long categoryId) {
        Page<ScenicSpot> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(ScenicSpot::getStatus, status);
        }
        
        if (categoryId != null) {
            wrapper.eq(ScenicSpot::getCategoryId, categoryId);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ScenicSpot::getName, keyword);
        }
        
        wrapper.orderByDesc(ScenicSpot::getCreateTime);
        Page<ScenicSpot> result = page(pageParam, wrapper);
        
        // 填充分类名称
        fillCategoryNames(result.getRecords());
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    /**
     * 管理端分页查询（兼容旧接口）
     */
    public PageResult<ScenicSpot> getAdminList(Integer page, Integer size, String keyword, Integer status) {
        return getAdminList(page, size, keyword, status, null);
    }
    
    /**
     * 更新景点状态（上架/下架）
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        ScenicSpot spot = getById(id);
        if (spot == null) {
            throw new BusinessException(404, "景点不存在");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值无效，只能为0或1");
        }
        spot.setStatus(status);
        updateById(spot);
    }
    
    /**
     * 获取景点分类列表
     */
    public List<Category> getScenicCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getType, "SCENIC");
        wrapper.eq(Category::getStatus, 1);
        wrapper.orderByAsc(Category::getSort);
        return categoryService.list(wrapper);
    }
    
    /**
     * 填充分类名称
     */
    private void fillCategoryNames(List<ScenicSpot> spots) {
        if (spots == null || spots.isEmpty()) {
            return;
        }
        
        // 获取所有分类ID
        List<Long> categoryIds = spots.stream()
                .map(ScenicSpot::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (categoryIds.isEmpty()) {
            return;
        }
        
        // 批量查询分类
        List<Category> categories = categoryService.listByIds(categoryIds);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 填充分类名称
        spots.forEach(spot -> {
            if (spot.getCategoryId() != null) {
                spot.setCategoryName(categoryMap.get(spot.getCategoryId()));
            }
        });
    }

    /**
     * 创建景点
     */
    @Transactional
    public ScenicSpot create(ScenicSpot spot) {
        validateSpot(spot);
        spot.setViewCount(0);
        spot.setStatus(1);
        save(spot);
        return spot;
    }

    /**
     * 更新景点
     */
    @Transactional
    public ScenicSpot update(Long id, ScenicSpot spot) {
        ScenicSpot existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        validateSpot(spot);
        spot.setId(id);
        updateById(spot);
        return getById(id);
    }

    /**
     * 软删除景点
     */
    @Transactional
    public void softDelete(Long id) {
        ScenicSpot spot = getById(id);
        if (spot == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        spot.setStatus(0);  // 设置为下架状态
        updateById(spot);
    }

    /**
     * 验证景点数据
     */
    private void validateSpot(ScenicSpot spot) {
        if (!StringUtils.hasText(spot.getName())) {
            throw new BusinessException(400, "景点名称不能为空");
        }
        if (spot.getLatitude() == null || spot.getLongitude() == null) {
            throw new BusinessException(400, "经纬度不能为空");
        }
        if (spot.getTicketPrice() == null) {
            throw new BusinessException(400, "门票价格不能为空");
        }
    }
    
    /**
     * 获取热门景点TOP N（用于数据统计）
     */
    public List<Map<String, Object>> getTopScenic(int limit) {
        return baseMapper.getTopScenic(limit);
    }

    // ========== 商家端方法 ==========

    /**
     * 商家分页查询自己的景点列表
     */
    public PageResult<ScenicSpot> getMerchantList(Long merchantId, Integer page, Integer size, String keyword, Integer status) {
        Page<ScenicSpot> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        
        // 数据隔离：只查询该商家的景点
        wrapper.eq(ScenicSpot::getMerchantId, merchantId);
        
        if (status != null) {
            wrapper.eq(ScenicSpot::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ScenicSpot::getName, keyword);
        }
        
        wrapper.orderByDesc(ScenicSpot::getCreateTime);
        Page<ScenicSpot> result = page(pageParam, wrapper);
        
        // 填充分类名称
        fillCategoryNames(result.getRecords());
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 商家创建景点（自动关联商家ID）
     */
    @Transactional
    public ScenicSpot createByMerchant(Long merchantId, ScenicSpot spot) {
        validateSpot(spot);
        
        // 自动关联商家ID
        spot.setMerchantId(merchantId);
        spot.setViewCount(0);
        spot.setStatus(1);
        
        save(spot);
        return spot;
    }

    /**
     * 商家更新景点（验证权限）
     */
    @Transactional
    public ScenicSpot updateByMerchant(Long merchantId, Long id, ScenicSpot spot) {
        ScenicSpot existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        // 权限验证：只能修改自己的景点
        if (!merchantId.equals(existing.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的景点");
        }
        
        validateSpot(spot);
        spot.setId(id);
        spot.setMerchantId(merchantId); // 保持商家ID不变
        updateById(spot);
        return getById(id);
    }

    /**
     * 商家删除景点（验证权限）
     */
    @Transactional
    public void deleteByMerchant(Long merchantId, Long id) {
        ScenicSpot existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        // 权限验证：只能删除自己的景点
        if (!merchantId.equals(existing.getMerchantId())) {
            throw new BusinessException(403, "无权删除其他商家的景点");
        }
        
        // 软删除
        existing.setStatus(0);
        updateById(existing);
    }

    /**
     * 商家获取景点详情（验证权限）
     */
    public ScenicSpot getMerchantDetail(Long merchantId, Long id) {
        ScenicSpot spot = getById(id);
        if (spot == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        // 权限验证：只能查看自己的景点
        if (!merchantId.equals(spot.getMerchantId())) {
            throw new BusinessException(403, "无权查看其他商家的景点");
        }
        
        return spot;
    }

    /**
     * 商家更新景点状态（验证权限）
     */
    @Transactional
    public void updateStatusByMerchant(Long merchantId, Long id, Integer status) {
        ScenicSpot spot = getById(id);
        if (spot == null) {
            throw new BusinessException(404, "景点不存在");
        }
        
        // 权限验证：只能修改自己的景点
        if (!merchantId.equals(spot.getMerchantId())) {
            throw new BusinessException(403, "无权修改其他商家的景点");
        }
        
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值无效，只能为0或1");
        }
        
        spot.setStatus(status);
        updateById(spot);
    }
}
