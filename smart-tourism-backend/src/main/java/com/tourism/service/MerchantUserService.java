package com.tourism.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.dto.*;
import com.tourism.entity.MerchantUser;
import com.tourism.mapper.MerchantUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商家用户服务
 */
@Service
@RequiredArgsConstructor
public class MerchantUserService extends ServiceImpl<MerchantUserMapper, MerchantUser> {

    private final ScenicSpotService scenicSpotService;
    private final HotelService hotelService;

    /**
     * 商家注册
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInfoResponse register(MerchantRegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getUsername, request.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建商家用户
        MerchantUser merchant = new MerchantUser();
        BeanUtils.copyProperties(request, merchant);
        
        // 加密密码
        merchant.setPassword(BCrypt.hashpw(request.getPassword()));
        
        // 设置初始状态为待审核
        merchant.setStatus("PENDING");
        
        this.save(merchant);
        
        // 返回商家信息
        return convertToResponse(merchant);
    }

    /**
     * 商家登录
     */
    public MerchantLoginResponse login(MerchantLoginRequest request) {
        // 查询商家用户
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getUsername, request.getUsername());
        MerchantUser merchant = this.getOne(wrapper);
        
        if (merchant == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), merchant.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查商家状态
        if ("DISABLED".equals(merchant.getStatus())) {
            throw new BusinessException("账号已被禁用,请联系管理员");
        }
        
        if ("REJECTED".equals(merchant.getStatus())) {
            throw new BusinessException("入驻申请已被拒绝,原因: " + merchant.getRejectReason());
        }
        
        if ("PENDING".equals(merchant.getStatus())) {
            throw new BusinessException("入驻申请审核中,请耐心等待");
        }
        
        // 登录成功,生成token
        StpUtil.login(merchant.getId());
        String token = StpUtil.getTokenValue();
        
        // 构造响应
        MerchantLoginResponse response = new MerchantLoginResponse();
        response.setToken(token);
        response.setMerchantInfo(convertToResponse(merchant));
        
        return response;
    }

    /**
     * 获取当前登录商家信息
     */
    public MerchantInfoResponse getCurrentMerchantInfo() {
        Long merchantId = StpUtil.getLoginIdAsLong();
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        return convertToResponse(merchant);
    }

    /**
     * 更新商家信息
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInfoResponse updateMerchantInfo(MerchantUpdateRequest request) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        // 更新信息
        if (StringUtils.hasText(request.getContactPerson())) {
            merchant.setContactPerson(request.getContactPerson());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            merchant.setContactPhone(request.getContactPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            merchant.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAddress())) {
            merchant.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getDescription())) {
            merchant.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getBusinessLicense())) {
            merchant.setBusinessLicense(request.getBusinessLicense());
            // 如果更新了营业执照,需要重新审核
            merchant.setStatus("PENDING");
            merchant.setRejectReason(null);
        }
        
        this.updateById(merchant);
        
        return convertToResponse(merchant);
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, merchant.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 更新密码
        merchant.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(merchant);
    }

    /**
     * 分页查询商家列表(管理员)
     */
    public PageResult<MerchantUser> getMerchantList(Integer page, Integer size, String keyword, 
                                                     String status, String merchantType) {
        Page<MerchantUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MerchantUser::getUsername, keyword)
                    .or().like(MerchantUser::getMerchantName, keyword)
                    .or().like(MerchantUser::getContactPerson, keyword)
                    .or().like(MerchantUser::getContactPhone, keyword));
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(MerchantUser::getStatus, status);
        }
        
        if (StringUtils.hasText(merchantType)) {
            wrapper.eq(MerchantUser::getMerchantType, merchantType);
        }
        
        wrapper.orderByDesc(MerchantUser::getCreateTime);
        
        Page<MerchantUser> result = this.page(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取待审核商家列表(按类型)
     */
    public PageResult<MerchantUser> getPendingMerchants(Integer page, Integer size, String merchantType) {
        Page<MerchantUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MerchantUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantUser::getStatus, "PENDING");
        
        if (StringUtils.hasText(merchantType)) {
            wrapper.eq(MerchantUser::getMerchantType, merchantType);
        }
        
        wrapper.orderByAsc(MerchantUser::getCreateTime);
        
        Page<MerchantUser> result = this.page(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveMerchant(Long merchantId) {
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        merchant.setStatus("APPROVED");
        merchant.setRejectReason(null);
        this.updateById(merchant);
    }

    /**
     * 审核拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectMerchant(Long merchantId, String reason) {
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        merchant.setStatus("REJECTED");
        merchant.setRejectReason(reason);
        this.updateById(merchant);
    }

    /**
     * 禁用商家
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableMerchant(Long merchantId) {
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        merchant.setStatus("DISABLED");
        this.updateById(merchant);
        
        // 下架该商家的所有产品
        disableMerchantProducts(merchantId, merchant.getMerchantType());
    }
    
    /**
     * 下架商家的所有产品
     */
    private void disableMerchantProducts(Long merchantId, String merchantType) {
        if ("SCENIC".equals(merchantType)) {
            // 下架所有景点
            LambdaQueryWrapper<com.tourism.entity.ScenicSpot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(com.tourism.entity.ScenicSpot::getMerchantId, merchantId);
            wrapper.eq(com.tourism.entity.ScenicSpot::getStatus, 1);
            
            java.util.List<com.tourism.entity.ScenicSpot> spots = scenicSpotService.list(wrapper);
            spots.forEach(spot -> {
                spot.setStatus(0);
                scenicSpotService.updateById(spot);
            });
        } else if ("HOTEL".equals(merchantType)) {
            // 下架所有酒店
            LambdaQueryWrapper<com.tourism.entity.Hotel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(com.tourism.entity.Hotel::getMerchantId, merchantId);
            wrapper.eq(com.tourism.entity.Hotel::getStatus, 1);
            
            java.util.List<com.tourism.entity.Hotel> hotels = hotelService.list(wrapper);
            hotels.forEach(hotel -> {
                hotel.setStatus(0);
                hotelService.updateById(hotel);
            });
        }
    }

    /**
     * 启用商家
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableMerchant(Long merchantId) {
        MerchantUser merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        merchant.setStatus("APPROVED");
        this.updateById(merchant);
    }

    /**
     * 转换为响应DTO
     */
    private MerchantInfoResponse convertToResponse(MerchantUser merchant) {
        MerchantInfoResponse response = new MerchantInfoResponse();
        BeanUtils.copyProperties(merchant, response);
        // 不返回密码
        return response;
    }
}
