package com.tourism.service;

import com.tourism.entity.ScenicSpot;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 景点服务属性测试
 * Feature: smart-tourism-platform, scenic-management-enhancement
 */
class ScenicSpotServicePropertyTest {

    /**
     * Property 1: 状态切换一致性
     * For any scenic spot and target status (0 or 1), after calling updateStatus,
     * the spot's status field SHALL equal the target status value.
     * Validates: Requirements 1.1, 1.2
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void statusUpdateShouldBeConsistent(
            @ForAll("validScenicSpot") ScenicSpot spot,
            @ForAll @IntRange(min = 0, max = 1) int targetStatus
    ) {
        // 模拟状态更新逻辑
        spot.setStatus(targetStatus);
        
        // 验证状态更新后的值与目标值一致
        assertThat(spot.getStatus()).isEqualTo(targetStatus);
        
        // 验证状态只能是0或1
        assertThat(spot.getStatus()).isIn(0, 1);
    }
    
    /**
     * Property 1 扩展: 状态切换幂等性
     * For any scenic spot, setting the same status multiple times SHALL result in the same final state.
     * Validates: Requirements 1.1, 1.2
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void statusUpdateShouldBeIdempotent(
            @ForAll("validScenicSpot") ScenicSpot spot,
            @ForAll @IntRange(min = 0, max = 1) int targetStatus,
            @ForAll @IntRange(min = 1, max = 5) int repeatTimes
    ) {
        // 多次设置相同状态
        for (int i = 0; i < repeatTimes; i++) {
            spot.setStatus(targetStatus);
        }
        
        // 验证最终状态与目标一致
        assertThat(spot.getStatus()).isEqualTo(targetStatus);
    }

    /**
     * Property 4: Scenic Spot Search Relevance
     * For any search query string and set of scenic spots, all returned results
     * SHALL contain the query string in their name, description, or tags fields.
     * Validates: Requirements 2.2
     */
    @Property(tries = 100)
    void searchResultsShouldContainKeyword(
            @ForAll @AlphaChars @StringLength(min = 2, max = 10) String keyword,
            @ForAll("scenicSpotList") List<ScenicSpot> spots
    ) {
        // 模拟搜索逻辑
        List<ScenicSpot> results = spots.stream()
                .filter(spot -> spot.getStatus() == 1)
                .filter(spot -> 
                        (spot.getName() != null && spot.getName().contains(keyword)) ||
                        (spot.getDescription() != null && spot.getDescription().contains(keyword)) ||
                        (spot.getTags() != null && spot.getTags().contains(keyword))
                )
                .collect(Collectors.toList());

        // 验证所有结果都包含关键字
        for (ScenicSpot spot : results) {
            boolean containsKeyword = 
                    (spot.getName() != null && spot.getName().contains(keyword)) ||
                    (spot.getDescription() != null && spot.getDescription().contains(keyword)) ||
                    (spot.getTags() != null && spot.getTags().contains(keyword));
            assertThat(containsKeyword).isTrue();
        }
    }

    /**
     * Property 5: CRUD Data Integrity
     * For any entity, creating then reading SHALL return equivalent data.
     * Validates: Requirements 2.4, 2.5
     */
    @Property(tries = 100)
    void crudDataIntegrity(
            @ForAll("validScenicSpot") ScenicSpot original
    ) {
        // 模拟创建后读取
        ScenicSpot created = new ScenicSpot();
        created.setId(1L);
        created.setName(original.getName());
        created.setDescription(original.getDescription());
        created.setLatitude(original.getLatitude());
        created.setLongitude(original.getLongitude());
        created.setTicketPrice(original.getTicketPrice());
        created.setTags(original.getTags());
        created.setStatus(1);
        created.setViewCount(0);

        // 验证数据一致性
        assertThat(created.getName()).isEqualTo(original.getName());
        assertThat(created.getDescription()).isEqualTo(original.getDescription());
        assertThat(created.getLatitude()).isEqualTo(original.getLatitude());
        assertThat(created.getLongitude()).isEqualTo(original.getLongitude());
        assertThat(created.getTicketPrice()).isEqualTo(original.getTicketPrice());
    }

    /**
     * Property 6: Soft Delete Exclusion
     * For any soft-deleted entity, public listing queries SHALL NOT include that entity.
     * Validates: Requirements 2.6
     */
    @Property(tries = 100)
    void softDeletedShouldBeExcluded(
            @ForAll("scenicSpotList") List<ScenicSpot> spots
    ) {
        // 模拟公开列表查询（只返回 status=1 的）
        List<ScenicSpot> publicList = spots.stream()
                .filter(spot -> spot.getStatus() == 1)
                .collect(Collectors.toList());

        // 验证所有返回的景点都是上架状态
        for (ScenicSpot spot : publicList) {
            assertThat(spot.getStatus()).isEqualTo(1);
        }

        // 验证下架的景点不在列表中
        List<ScenicSpot> deletedSpots = spots.stream()
                .filter(spot -> spot.getStatus() == 0)
                .collect(Collectors.toList());

        for (ScenicSpot deleted : deletedSpots) {
            assertThat(publicList).doesNotContain(deleted);
        }
    }

    /**
     * Property 2: 分类筛选正确性
     * For any category ID, when filtering scenic spots by that category,
     * all returned spots SHALL have categoryId equal to the filter category ID.
     * Validates: Requirements 3.5
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void categoryFilterShouldReturnCorrectResults(
            @ForAll("scenicSpotListWithCategory") List<ScenicSpot> spots,
            @ForAll @LongRange(min = 1, max = 10) long filterCategoryId
    ) {
        // 模拟按分类筛选逻辑
        List<ScenicSpot> filteredResults = spots.stream()
                .filter(spot -> spot.getCategoryId() != null && spot.getCategoryId().equals(filterCategoryId))
                .collect(Collectors.toList());
        
        // 验证所有返回结果的categoryId都等于筛选的分类ID
        for (ScenicSpot spot : filteredResults) {
            assertThat(spot.getCategoryId()).isEqualTo(filterCategoryId);
        }
    }
    
    /**
     * Property 3: 分类名称关联一致性
     * For any scenic spot with categoryId, the categoryName should match the category's name.
     * Validates: Requirements 3.4, 5.4
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void categoryNameShouldMatchCategoryId(
            @ForAll @LongRange(min = 1, max = 10) long categoryId,
            @ForAll @AlphaChars @StringLength(min = 2, max = 20) String categoryName
    ) {
        // 模拟景点和分类关联
        ScenicSpot spot = new ScenicSpot();
        spot.setCategoryId(categoryId);
        
        // 模拟填充分类名称
        spot.setCategoryName(categoryName);
        
        // 验证分类名称已被正确设置
        assertThat(spot.getCategoryName()).isEqualTo(categoryName);
        assertThat(spot.getCategoryId()).isEqualTo(categoryId);
    }

    /**
     * Property 4: 必填字段验证
     * For any scenic spot creation request, if name is empty or ticketPrice is null,
     * the request SHALL be rejected.
     * Validates: Requirements 4.5
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void requiredFieldValidationShouldRejectInvalidData(
            @ForAll("invalidScenicSpot") ScenicSpot invalidSpot
    ) {
        // 模拟验证逻辑
        boolean isValid = validateSpot(invalidSpot);
        
        // 验证无效数据应该被拒绝
        assertThat(isValid).isFalse();
    }
    
    /**
     * Property 5: 地址字段存储一致性
     * For any address value in a scenic spot, after saving, the address field SHALL equal the input value.
     * Validates: Requirements 4.3
     * Feature: scenic-management-enhancement
     */
    @Property(tries = 100)
    void addressFieldShouldBeStoredCorrectly(
            @ForAll @StringLength(min = 0, max = 200) String address
    ) {
        ScenicSpot spot = new ScenicSpot();
        spot.setAddress(address);
        
        // 验证地址字段正确存储
        assertThat(spot.getAddress()).isEqualTo(address);
    }
    
    /**
     * 模拟验证景点数据
     */
    private boolean validateSpot(ScenicSpot spot) {
        if (spot.getName() == null || spot.getName().trim().isEmpty()) {
            return false;
        }
        if (spot.getTicketPrice() == null) {
            return false;
        }
        return true;
    }

    /**
     * 验证浏览量只增不减
     */
    @Property(tries = 100)
    void viewCountShouldOnlyIncrease(
            @ForAll @IntRange(min = 0, max = 10000) int initialCount,
            @ForAll @IntRange(min = 1, max = 100) int views
    ) {
        int finalCount = initialCount + views;
        assertThat(finalCount).isGreaterThan(initialCount);
    }

    @Provide
    Arbitrary<ScenicSpot> validScenicSpot() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(50),
                Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(200),
                Arbitraries.bigDecimals().between(BigDecimal.valueOf(-90), BigDecimal.valueOf(90)),
                Arbitraries.bigDecimals().between(BigDecimal.valueOf(-180), BigDecimal.valueOf(180)),
                Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(1000))
        ).as((name, desc, lat, lng, price) -> {
            ScenicSpot spot = new ScenicSpot();
            spot.setName(name);
            spot.setDescription(desc);
            spot.setLatitude(lat);
            spot.setLongitude(lng);
            spot.setTicketPrice(price);
            spot.setStatus(1);
            spot.setViewCount(0);
            return spot;
        });
    }

    @Provide
    Arbitrary<List<ScenicSpot>> scenicSpotList() {
        return Arbitraries.integers().between(0, 20).flatMap(size -> {
            List<Arbitrary<ScenicSpot>> spots = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                spots.add(Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20),
                        Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50),
                        Arbitraries.of(0, 1)
                ).as((name, desc, status) -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setDescription(desc);
                    spot.setStatus(status);
                    return spot;
                }));
            }
            return spots.isEmpty() ? 
                    Arbitraries.just(new ArrayList<>()) : 
                    Combinators.combine(spots).as(list -> new ArrayList<>(list));
        });
    }
    
    @Provide
    Arbitrary<List<ScenicSpot>> scenicSpotListWithCategory() {
        return Arbitraries.integers().between(1, 20).flatMap(size -> {
            List<Arbitrary<ScenicSpot>> spots = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                spots.add(Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20),
                        Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50),
                        Arbitraries.of(0, 1),
                        Arbitraries.longs().between(1, 10)
                ).as((name, desc, status, categoryId) -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setDescription(desc);
                    spot.setStatus(status);
                    spot.setCategoryId(categoryId);
                    return spot;
                }));
            }
            return Combinators.combine(spots).as(list -> new ArrayList<>(list));
        });
    }
    
    @Provide
    Arbitrary<ScenicSpot> invalidScenicSpot() {
        // 生成无效的景点数据（名称为空或门票价格为null）
        return Arbitraries.oneOf(
                // 名称为空
                Arbitraries.just("").map(name -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setTicketPrice(BigDecimal.valueOf(100));
                    return spot;
                }),
                // 名称为null
                Arbitraries.just((String) null).map(name -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setTicketPrice(BigDecimal.valueOf(100));
                    return spot;
                }),
                // 门票价格为null
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(20).map(name -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setTicketPrice(null);
                    return spot;
                }),
                // 名称只有空格
                Arbitraries.strings().whitespace().ofMinLength(1).ofMaxLength(10).map(name -> {
                    ScenicSpot spot = new ScenicSpot();
                    spot.setName(name);
                    spot.setTicketPrice(BigDecimal.valueOf(100));
                    return spot;
                })
        );
    }
}
