package com.learning.journalApp.controller;

import com.learning.journalApp.dto.UserDTO;
import com.learning.journalApp.entity.User;
import com.learning.journalApp.service.UserDetailsServiceImpl;
import com.learning.journalApp.service.UserService;
import com.learning.journalApp.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
@Tag(name = "Public APIs", description = "Login, Signup and Health Check for Users")
public class PublicController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public PublicController(UserService userService, AuthenticationManager authenticationManager,
                            UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/health-check")
    @Operation(summary = "Health Check API")
    public String healthCheck() {
        return "Ok";
    }

    @PostMapping("/signup")
    @Operation(summary = "Signup for a new user")
    public void signup(@RequestBody UserDTO user){
        try{
            User newUser = new User();
            newUser.setEmail(user.getEmail());
            newUser.setUserName(user.getUserName());
            newUser.setPassword(user.getPassword());
            newUser.setSentimentAnalysis(user.isSentimentAnalysis());
            userService.saveNewUser(newUser);
        } catch (Exception e) {
            log.error("error while creating new user", e);
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/login")
    @Operation(summary = "Login for an existing user")
    public ResponseEntity<String> login (@RequestBody User user){
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred while createAuthenticationToken ", e);
            return new ResponseEntity<>("Invalid username or password", HttpStatus.BAD_REQUEST );
        }
    }
}
