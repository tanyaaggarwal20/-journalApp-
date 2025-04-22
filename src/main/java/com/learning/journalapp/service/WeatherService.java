package com.learning.journalapp.service;

import com.learning.journalapp.api.response.WeatherResponse;
import com.learning.journalapp.cache.AppCache;
import com.learning.journalapp.constants.Placeholders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service 
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey;

    private final AppCache appCache;
    private final RestTemplate restTemplate;
    private final RedisService redisService;

    @Autowired
    public WeatherService(AppCache appCache, RestTemplate restTemplate, RedisService redisService) {
        this.appCache = appCache;
        this.restTemplate = restTemplate;
        this.redisService = redisService;
    }

    public WeatherResponse getWeather(String city){
        WeatherResponse weatherResponse = redisService.get("weather_of_" + city, WeatherResponse.class);
        if(weatherResponse != null){
            return weatherResponse;
        } else {
            String finalAPI =  appCache.getAppCache().get(AppCache.keys.WEATHER_API.toString()).replace(Placeholders.CITY, city).replace(Placeholders.API_KEY , apiKey);
            ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalAPI, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse body = response.getBody();
            if(body != null) {
                redisService.set("weather_of_" + city, body, 300L);
                return body;
            } else {
                return null;
            }
        }

    }
}
