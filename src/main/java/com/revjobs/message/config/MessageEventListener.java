package com.revjobs.message.config;

import com.revjobs.message.model.Message;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.ZoneId;

@Component
public class MessageEventListener extends AbstractMongoEventListener<Message> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Message> event) {
        Message message = event.getSource();

        // Auto-set timestamp before saving if not already set
        if (message.getSentAt() == null) {
            message.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        }

        // Auto-set isRead to false if not already set
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
    }
}
