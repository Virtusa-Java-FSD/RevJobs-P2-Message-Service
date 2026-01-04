package com.revjobs.message.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.time.ZoneId;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime sentAt;

    private Long applicationId;
}
