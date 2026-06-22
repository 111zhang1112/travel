package com.tourism.controller;

import com.tourism.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 天气控制器
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    
    private final WeatherService weatherService;
    
    /**
     * 根据城市名称获取天气
     */
    @GetMapping("/city/{city}")
    public Map<String, Object> getWeatherByCity(@PathVariable String city) {
        Map<String, Object> weather = weatherService.getWeatherByCity(city);
        return Map.of("code", 200, "data", weather);
    }
    
    /**
     * 根据经纬度获取天气
     */
    @GetMapping("/location")
    public Map<String, Object> getWeatherByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        Map<String, Object> weather = weatherService.getWeatherByLocation(latitude, longitude);
        return Map.of("code", 200, "data", weather);
    }
}
