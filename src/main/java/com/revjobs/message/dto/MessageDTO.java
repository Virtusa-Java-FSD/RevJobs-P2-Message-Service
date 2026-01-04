package com.revjobs.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id; // MongoDB uses String IDs
    private Long senderId;
    private Long receiverId;
    private String content;
    private Boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime sentAt;

    private Long applicationId;
}
