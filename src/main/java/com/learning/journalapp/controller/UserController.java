package com.learning.journalapp.controller;

import com.learning.journalapp.api.response.WeatherResponse;
import com.learning.journalapp.cache.AppCache;
import com.learning.journalapp.entity.User;
import com.learning.journalapp.repository.UserRepository;
import com.learning.journalapp.service.UserService;
import com.learning.journalapp.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "User APIs", description = "APIs for user management")
public class UserController {

    private final UserService userService;
    private final WeatherService weatherService;
    private final UserRepository userRepository;
    private final AppCache appCache;

    @Autowired
    public UserController(UserService userService, WeatherService weatherService, UserRepository userRepository, AppCache appCache) {
        this.userService = userService;
        this.weatherService = weatherService;
        this.userRepository = userRepository;
        this.appCache = appCache;
    }

    @PutMapping
    @Operation(summary = "Update username and password of the authenticated user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User userInDb = userService.findByUserName(userName);
            userInDb.setUserName(user.getUserName());
            userInDb.setPassword(user.getPassword());
            userService.saveNewUser(userInDb);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update user: " + e.getMessage());
        }

     }

    @DeleteMapping
    @Operation(summary = "Delete the authenticated user")
    public ResponseEntity<Void> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByUserName(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping
    @Operation(summary = "Greetings to the authenticated user")
    public ResponseEntity<String> greetings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WeatherResponse weatherResponse = weatherService.getWeather("Mumbai");
        String greeting = "";
        if(weatherResponse != null) {
             greeting = ", weather in Mumbai, India feels like " + weatherResponse.getCurrent().getFeelsLike();
        }
        return new ResponseEntity<>("Hi " + authentication.getName() + greeting, HttpStatus.OK);
    }

    @GetMapping("/clear-app-cache")
    @Operation(summary = "Clear the application cache")
    public void clearAppCache() {
        appCache.init();
    }
}
