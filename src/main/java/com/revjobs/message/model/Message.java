package com.revjobs.message.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id; // MongoDB uses String IDs (ObjectId)

    @Indexed
    private Long senderId;

    @Indexed
    private Long receiverId;

    private String content;

    private Boolean isRead = false;

    @Indexed
    private LocalDateTime sentAt;

    private Long applicationId;

    // Auto-set timestamp before saving
    public void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
