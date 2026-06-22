package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 路线规划控制器
 */
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;
    
    /**
     * 规划多景点路线
     * @param request 包含景点ID列表和出行方式
     * @return 路线规划结果
     */
    @PostMapping("/plan")
    public Result<Map<String, Object>> planRoute(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Object> scenicIdsObj = (List<Object>) request.get("scenicIds");
        String travelMode = (String) request.getOrDefault("travelMode", "driving");
        
        if (scenicIdsObj == null || scenicIdsObj.size() < 2) {
            return Result.error("至少需要选择2个景点");
        }
        
        // 将Integer/Long转换为Long类型
        List<Long> scenicIds = scenicIdsObj.stream()
                .map(obj -> {
                    if (obj instanceof Integer) {
                        return ((Integer) obj).longValue();
                    } else if (obj instanceof Long) {
                        return (Long) obj;
                    } else {
                        return Long.parseLong(obj.toString());
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = routeService.planRoute(scenicIds, travelMode);
        return Result.success(result);
    }
    
    /**
     * 获取两点之间的路线
     * @param origin 起点坐标 "经度,纬度"
     * @param destination 终点坐标 "经度,纬度"
     * @param travelMode 出行方式: driving(驾车), walking(步行), transit(公交)
     * @return 路线详情
     */
    @GetMapping("/direction")
    public Result<Map<String, Object>> getDirection(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "driving") String travelMode) {
        
        Map<String, Object> result = routeService.getDirection(origin, destination, travelMode);
        return Result.success(result);
    }
}
