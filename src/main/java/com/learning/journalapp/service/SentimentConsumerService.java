package com.learning.journalApp.service;

import com.learning.journalApp.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SentimentConsumerService {

    private final EmailService emailService;

    @Autowired
    public SentimentConsumerService(EmailService emailService, KafkaTemplate<String, SentimentData> kafkaTemplate) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "weekly_sentiments", groupId = "weekly_sentiment_group")
    public void consume(SentimentData sentimentData) {
        sendEmail(sentimentData);
//        try {
//            sendEmail(sentimentData);
//        } catch (Exception e) {
//            kafkaTemplate.send("weekly_sentiments_dlq", sentimentData.getEmail(), sentimentData);
//        }
    }

    private void sendEmail(SentimentData sentimentData) {
        emailService.sendEmail(sentimentData.getEmail(), "Sentiment for previous week", sentimentData.getSentiment());
    }
}
