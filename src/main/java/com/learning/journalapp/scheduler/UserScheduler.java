package com.learning.journalapp.scheduler;

import com.learning.journalapp.cache.AppCache;
import com.learning.journalapp.entity.JournalEntry;
import com.learning.journalapp.model.SentimentData;
import com.learning.journalapp.entity.User;
import com.learning.journalapp.enums.Sentiment;
import com.learning.journalapp.repository.UserRepositoryImpl;
import com.learning.journalapp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler  {

    private final EmailService emailService;
    private final UserRepositoryImpl userRepository;
    private final AppCache appCache;
    private final KafkaTemplate<String, SentimentData> kafkaTemplate;

    @Autowired
    public UserScheduler(EmailService emailService, UserRepositoryImpl userRepository, AppCache appCache, KafkaTemplate<String, SentimentData> kafkaTemplate) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.appCache = appCache;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSaMail() {
        List<User> users = userRepository.getUsersForSA();
        for(User user: users) {
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());
            Map<Sentiment, Integer> sentimentCounts = new HashMap<>();
            for(Sentiment sentiment: sentiments) {
                if(sentiment != null)
                    sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
            }
            Sentiment mostFrequentSentiment = null;
            int maxCount = 0;
            for (Map.Entry<Sentiment, Integer> entry : sentimentCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostFrequentSentiment = entry.getKey();
                }
            }
            if (mostFrequentSentiment != null) {
                SentimentData sentimentData = SentimentData.builder().email(user.getEmail()).sentiment("Sentiment for last 7 days " + mostFrequentSentiment).build();
                try {
                    kafkaTemplate.send("weekly_sentiments", sentimentData.getEmail() , sentimentData);
                } catch (Exception e) {
                    //kafka fallback
                    emailService.sendEmail(sentimentData.getEmail(), "Sentiment for previous week", sentimentData.getSentiment());
                }
//                emailService.sendEmail(user.getEmail(), "Sentiment for last 7 days", mostFrequentSentiment.toString());
            }
        }
    }

    @Scheduled(cron = "0 */10 * * * * ")
    public void clearAppCache() {
        appCache.init();
    }
}
