package com.learning.journalApp.controller;

import com.learning.journalApp.entity.EmailEntity;
import com.learning.journalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/send-email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    private ResponseEntity<String> sendEmail(@RequestBody EmailEntity request){
        try {
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
            return new ResponseEntity<>("Email sent successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
