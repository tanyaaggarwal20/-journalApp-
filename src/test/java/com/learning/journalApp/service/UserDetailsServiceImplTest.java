//package com.learning.journalApp.service;

import com.learning.journalApp.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;


import static org.mockito.Mockito.*;

//public class UserDetailsServiceImplTest {
//    @InjectMocks
//    private UserDetailsServiceImpl userDetailsService;
//
//     @Mock
//     private UserRepository userRepository;
//
//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.initMocks(this);
//     }
//
////    @Test
////    void loadUserByUserNameTest() {
////        when(userRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(User.builder().username("ram").password("ewvw"));
////        UserDetails user =  userDetailsService.loadUserByUsername("ram");
////        Assertions.assertNotNull(user);
////    }
//}
