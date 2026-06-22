package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 路线规划服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final ScenicSpotMapper scenicSpotMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${amap.key}")
    private String amapKey;
    
    /**
     * 规划多景点路线
     */
    public Map<String, Object> planRoute(List<Long> scenicIds, String travelMode) {
        // 1. 获取景点信息
        List<ScenicSpot> scenicSpots = scenicSpotMapper.selectBatchIds(scenicIds);
        if (scenicSpots.size() < 2) {
            throw new RuntimeException("景点数量不足");
        }
        
        // 2. 按照用户提供的顺序排列景点
        Map<Long, ScenicSpot> scenicMap = scenicSpots.stream()
                .collect(Collectors.toMap(ScenicSpot::getId, s -> s));
        List<ScenicSpot> orderedScenics = scenicIds.stream()
                .map(scenicMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 3. 计算每段路线
        List<Map<String, Object>> segments = new ArrayList<>();
        BigDecimal totalDistance = BigDecimal.ZERO;
        int totalDuration = 0;
        
        for (int i = 0; i < orderedScenics.size() - 1; i++) {
            ScenicSpot from = orderedScenics.get(i);
            ScenicSpot to = orderedScenics.get(i + 1);
            
            String origin = from.getLongitude() + "," + from.getLatitude();
            String destination = to.getLongitude() + "," + to.getLatitude();
            
            Map<String, Object> direction = getDirection(origin, destination, travelMode);
            
            Map<String, Object> segment = new HashMap<>();
            segment.put("from", Map.of(
                "id", from.getId(),
                "name", from.getName(),
                "longitude", from.getLongitude(),
                "latitude", from.getLatitude()
            ));
            segment.put("to", Map.of(
                "id", to.getId(),
                "name", to.getName(),
                "longitude", to.getLongitude(),
                "latitude", to.getLatitude()
            ));
            segment.put("distance", direction.get("distance"));
            segment.put("duration", direction.get("duration"));
            segment.put("steps", direction.get("steps"));
            segment.put("polyline", direction.get("polyline"));
            
            segments.add(segment);
            
            // 累加总距离和时间
            if (direction.get("distance") != null) {
                totalDistance = totalDistance.add(new BigDecimal(direction.get("distance").toString()));
            }
            if (direction.get("duration") != null) {
                totalDuration += Integer.parseInt(direction.get("duration").toString());
            }
        }
        
        // 4. 返回完整路线
        Map<String, Object> result = new HashMap<>();
        result.put("scenicSpots", orderedScenics.stream().map(s -> Map.of(
            "id", s.getId(),
            "name", s.getName(),
            "longitude", s.getLongitude(),
            "latitude", s.getLatitude(),
            "address", s.getAddress() != null ? s.getAddress() : ""
        )).collect(Collectors.toList()));
        result.put("segments", segments);
        result.put("totalDistance", totalDistance);
        result.put("totalDuration", totalDuration);
        result.put("travelMode", travelMode);
        
        return result;
    }
    
    /**
     * 获取两点之间的路线(调用高德API)
     */
    public Map<String, Object> getDirection(String origin, String destination, String travelMode) {
        // 先检查距离是否超出API限制
        if (shouldUseFallback(origin, destination, travelMode)) {
            log.info("距离超出{}模式的规划范围，使用降级方案", travelMode);
            return getDefaultDirection(origin, destination);
        }
        
        try {
            String url = buildAmapUrl(origin, destination, travelMode);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || !"1".equals(response.get("status").toString())) {
                log.warn("高德API调用失败: {}，使用降级方案", response);
                return getDefaultDirection(origin, destination);
            }
            
            return parseAmapResponse(response, travelMode);
            
        } catch (Exception e) {
            log.error("调用高德API异常，使用降级方案", e);
            return getDefaultDirection(origin, destination);
        }
    }
    
    /**
     * 判断是否应该使用降级方案
     */
    private boolean shouldUseFallback(String origin, String destination, String travelMode) {
        String[] originCoords = origin.split(",");
        String[] destCoords = destination.split(",");
        
        double distance = calculateDistance(
            Double.parseDouble(originCoords[1]), Double.parseDouble(originCoords[0]),
            Double.parseDouble(destCoords[1]), Double.parseDouble(destCoords[0])
        );
        
        // 根据出行方式判断是否超出范围（单位：公里）
        switch (travelMode) {
            case "walking":
                return distance > 50; // 步行超过50公里使用降级
            case "transit":
                return distance > 100; // 公交超过100公里使用降级
            case "driving":
                return distance > 200; // 驾车超过200公里使用降级
            default:
                return false;
        }
    }
    
    /**
     * 构建高德API URL
     */
    private String buildAmapUrl(String origin, String destination, String travelMode) {
        String baseUrl = "https://restapi.amap.com/v3/direction/";
        
        switch (travelMode) {
            case "walking":
                return baseUrl + "walking?origin=" + origin + "&destination=" + destination + "&key=" + amapKey;
            case "transit":
                // 公交路线需要城市代码,这里默认使用杭州
                return baseUrl + "transit/integrated?origin=" + origin + "&destination=" + destination + "&city=330100&key=" + amapKey;
            case "driving":
            default:
                return baseUrl + "driving?origin=" + origin + "&destination=" + destination + "&key=" + amapKey;
        }
    }
    
    /**
     * 解析高德API响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAmapResponse(Map<String, Object> response, String travelMode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if ("driving".equals(travelMode) || "walking".equals(travelMode)) {
                Map<String, Object> route = (Map<String, Object>) response.get("route");
                List<Map<String, Object>> paths = (List<Map<String, Object>>) route.get("paths");
                
                if (paths != null && !paths.isEmpty()) {
                    Map<String, Object> path = paths.get(0);
                    result.put("distance", path.get("distance"));
                    result.put("duration", path.get("duration"));
                    
                    List<Map<String, Object>> steps = (List<Map<String, Object>>) path.get("steps");
                    result.put("steps", steps);
                    result.put("polyline", path.get("polyline"));
                }
            } else if ("transit".equals(travelMode)) {
                Map<String, Object> route = (Map<String, Object>) response.get("route");
                List<Map<String, Object>> transits = (List<Map<String, Object>>) route.get("transits");
                
                if (transits != null && !transits.isEmpty()) {
                    Map<String, Object> transit = transits.get(0);
                    result.put("distance", transit.get("distance"));
                    result.put("duration", transit.get("duration"));
                    result.put("steps", transit.get("segments"));
                }
            }
        } catch (Exception e) {
            log.error("解析高德API响应失败", e);
        }
        
        return result;
    }
    
    /**
     * 获取默认路线(当API调用失败时)
     */
    private Map<String, Object> getDefaultDirection(String origin, String destination) {
        String[] originCoords = origin.split(",");
        String[] destCoords = destination.split(",");
        
        double distance = calculateDistance(
            Double.parseDouble(originCoords[1]), Double.parseDouble(originCoords[0]),
            Double.parseDouble(destCoords[1]), Double.parseDouble(destCoords[0])
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("distance", (int) (distance * 1000)); // 转换为米
        result.put("duration", (int) (distance * 1000 / 40 * 60)); // 假设40km/h的速度
        result.put("steps", new ArrayList<>());
        result.put("polyline", "");
        result.put("fallback", true);  // 标记为降级方案
        
        return result;
    }
    
    /**
     * 计算两点之间的直线距离(单位:公里)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 地球半径(公里)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
