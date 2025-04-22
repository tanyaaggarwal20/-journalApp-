package com.learning.journalApp.entity;

import com.learning.journalApp.enums.Sentiment;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "journal_entries")
@Data
@NoArgsConstructor
public class JournalEntry {
    @Id
    private ObjectId id;
    @NotNull
    private String title;
    private String content;
    private LocalDateTime date;
    private Sentiment sentiment;
}
