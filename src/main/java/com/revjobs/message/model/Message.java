package com.revjobs.message.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_sender", columnList = "sender_id"),
           @Index(name = "idx_receiver", columnList = "receiver_id"),
           @Index(name = "idx_sent_at", columnList = "sentAt")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "application_id")
    private Long applicationId;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}

