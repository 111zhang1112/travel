package com.tourism.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 天气服务
 */
@Slf4j
@Service
public class WeatherService {
    
    private final RestTemplate restTemplate;
    
    @Value("${amap.key}")
    private String amapKey;
    
    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 根据城市名称获取天气信息
     */
    public Map<String, Object> getWeatherByCity(String city) {
        try {
            // 1. 先获取城市的adcode
            String adcode = getCityAdcode(city);
            if (adcode == null) {
                log.warn("无法获取城市{}的adcode", city);
                return getDefaultWeather();
            }
            
            // 2. 根据adcode获取天气
            return getWeatherByAdcode(adcode);
            
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            return getDefaultWeather();
        }
    }
    
    /**
     * 根据经纬度获取天气信息
     */
    public Map<String, Object> getWeatherByLocation(Double latitude, Double longitude) {
        try {
            log.info("开始获取天气信息，坐标：({}, {})", latitude, longitude);
            
            // 1. 先通过逆地理编码获取城市adcode
            String adcode = getAdcodeByLocation(latitude, longitude);
            if (adcode == null) {
                log.warn("无法获取坐标({},{})的adcode，使用降级方案", latitude, longitude);
                return getDefaultWeather();
            }
            
            log.info("获取到城市adcode: {}", adcode);
            
            // 2. 根据adcode获取天气
            Map<String, Object> weather = getWeatherByAdcode(adcode);
            log.info("天气信息获取成功");
            return weather;
            
        } catch (Exception e) {
            log.error("获取天气信息失败，使用降级方案", e);
            return getDefaultWeather();
        }
    }
    
    /**
     * 通过逆地理编码获取城市adcode
     */
    private String getAdcodeByLocation(Double latitude, Double longitude) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/geocode/regeo?location=%s,%s&key=%s",
                longitude, latitude, amapKey
            );
            
            log.info("调用逆地理编码API: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            log.info("逆地理编码API响应: {}", response);
            
            if (response != null && "1".equals(String.valueOf(response.get("status")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> regeocode = (Map<String, Object>) response.get("regeocode");
                if (regeocode != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> addressComponent = (Map<String, Object>) regeocode.get("addressComponent");
                    if (addressComponent != null) {
                        String adcode = String.valueOf(addressComponent.get("adcode"));
                        log.info("获取到adcode: {}", adcode);
                        return adcode;
                    }
                }
            }
            
            log.warn("逆地理编码失败，响应: {}", response);
        } catch (Exception e) {
            log.error("逆地理编码异常", e);
        }
        return null;
    }
    
    /**
     * 获取城市的adcode
     */
    private String getCityAdcode(String city) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/config/district?keywords=%s&subdistrict=0&key=%s",
                city, amapKey
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "1".equals(response.get("status").toString())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> districts = (List<Map<String, Object>>) response.get("districts");
                if (districts != null && !districts.isEmpty()) {
                    return districts.get(0).get("adcode").toString();
                }
            }
        } catch (Exception e) {
            log.error("获取城市adcode失败", e);
        }
        return null;
    }
    
    /**
     * 根据adcode获取天气信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getWeatherByAdcode(String adcode) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/weather/weatherInfo?city=%s&key=%s&extensions=all",
                adcode, amapKey
            );
            
            log.info("调用天气API: {}", url);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            log.info("天气API响应: {}", response);
            
            if (response != null && "1".equals(String.valueOf(response.get("status")))) {
                List<Map<String, Object>> forecasts = (List<Map<String, Object>>) response.get("forecasts");
                if (forecasts != null && !forecasts.isEmpty()) {
                    Map<String, Object> forecast = forecasts.get(0);
                    List<Map<String, Object>> casts = (List<Map<String, Object>>) forecast.get("casts");
                    
                    if (casts == null || casts.isEmpty()) {
                        log.warn("天气预报数据为空");
                        return getDefaultWeather();
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("city", forecast.get("city"));
                    result.put("province", forecast.get("province"));
                    result.put("reporttime", forecast.get("reporttime"));
                    
                    // 处理未来几天的天气
                    List<Map<String, Object>> weatherList = new ArrayList<>();
                    for (Map<String, Object> cast : casts) {
                        Map<String, Object> weather = new HashMap<>();
                        weather.put("date", cast.get("date"));
                        weather.put("week", cast.get("week"));
                        weather.put("dayweather", cast.get("dayweather"));
                        weather.put("nightweather", cast.get("nightweather"));
                        weather.put("daytemp", cast.get("daytemp"));
                        weather.put("nighttemp", cast.get("nighttemp"));
                        weather.put("daywind", cast.get("daywind"));
                        weather.put("nightwind", cast.get("nightwind"));
                        weather.put("daypower", cast.get("daypower"));
                        weather.put("nightpower", cast.get("nightpower"));
                        weatherList.add(weather);
                    }
                    
                    result.put("forecasts", weatherList);
                    log.info("天气数据解析成功，城市: {}", result.get("city"));
                    return result;
                }
            }
            
            log.warn("天气API返回失败，响应: {}", response);
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
        }
        
        return getDefaultWeather();
    }
    
    /**
     * 获取默认天气信息（当API调用失败时）
     */
    private Map<String, Object> getDefaultWeather() {
        Map<String, Object> result = new HashMap<>();
        result.put("city", "未知");
        result.put("province", "浙江");
        result.put("reporttime", new Date());
        
        List<Map<String, Object>> weatherList = new ArrayList<>();
        Map<String, Object> today = new HashMap<>();
        today.put("date", "今天");
        today.put("week", "");
        today.put("dayweather", "晴");
        today.put("nightweather", "晴");
        today.put("daytemp", "25");
        today.put("nighttemp", "18");
        today.put("daywind", "东南");
        today.put("nightwind", "东南");
        today.put("daypower", "≤3");
        today.put("nightpower", "≤3");
        weatherList.add(today);
        
        result.put("forecasts", weatherList);
        result.put("fallback", true);
        
        return result;
    }
}
