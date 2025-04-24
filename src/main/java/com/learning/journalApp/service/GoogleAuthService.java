package com.learning.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import com.learning.journalApp.entity.User;
import com.learning.journalApp.repository.UserRepository;
import com.learning.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public GoogleAuthService(RestTemplate restTemplate, UserDetailsServiceImpl userDetailsService,
                             PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, String>> processGoogleCallback(String code) {
        try {
            ResponseEntity<Map<String, Object>> tokenResponse = getTokenResponse(code);
            String idToken = null;
            if (tokenResponse.getBody() != null) {
                log.info("Retrieving token response");
                idToken = (String) tokenResponse.getBody().get("id_token");
            }
            if (idToken == null) {
                log.error("Failed to retrieve ID token from the token response");
                throw new IllegalStateException("Failed to retrieve ID token from the token response");
            }

            ResponseEntity<Map<String, Object>> userInfoResponse = getUserInfo(idToken);
            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");

                processUser(email);
                String jwtToken = generateJwtToken(email);

                Map<String, String> response = new HashMap<>();
                response.put("token", jwtToken);

                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to retrieve user info from Google");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Exception occurred while processing Google callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Map<String, Object>> getTokenResponse(String code) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", "https://developers.google.com/oauthplayground");
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        return restTemplate.exchange(
                tokenEndpoint,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
    }

    private ResponseEntity<Map<String, Object>> getUserInfo(String idToken) {
        String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        return restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
    }

    private void processUser(String email) {
        try {
            userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            User user = new User();
            user.setEmail(email);
            user.setUserName(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRoles(List.of("USER"));
            userRepository.save(user);
        }
    }

    private String generateJwtToken(String email) {
        return jwtUtil.generateToken(email);
    }
}