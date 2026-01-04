package com.revjobs.message.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revjobs.message.model.Message;
import com.revjobs.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    void sendMessage_Success() throws Exception {
        Message message = new Message();
        message.setSenderId(100L);
        message.setReceiverId(200L);
        message.setContent("Hello, interested in the position");
        message.setApplicationId(1L);
        message.setIsRead(false);

        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderId").value(100))
                .andExpect(jsonPath("$.data.receiverId").value(200))
                .andExpect(jsonPath("$.data.content").value("Hello, interested in the position"))
                .andExpect(jsonPath("$.data.isRead").value(false));
    }

    @Test
    void getConversation_ReturnsMessages() throws Exception {
        // Create messages between user 100 and user 200
        Message msg1 = new Message();
        msg1.setSenderId(100L);
        msg1.setReceiverId(200L);
        msg1.setContent("Message 1");
        msg1.setIsRead(false);
        msg1.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        messageRepository.save(msg1);

        Message msg2 = new Message();
        msg2.setSenderId(200L);
        msg2.setReceiverId(100L);
        msg2.setContent("Message 2");
        msg2.setIsRead(false);
        msg2.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        messageRepository.save(msg2);

        mockMvc.perform(get("/messages/conversation")
                .param("user1Id", "100")
                .param("user2Id", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getUserMessages_ReturnsUserMessages() throws Exception {
        Message msg = new Message();
        msg.setSenderId(100L);
        msg.setReceiverId(200L);
        msg.setContent("Test message");
        msg.setIsRead(false);
        msg.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        messageRepository.save(msg);

        mockMvc.perform(get("/messages/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void getUnreadMessages_ReturnsOnlyUnread() throws Exception {
        // Unread message
        Message unread = new Message();
        unread.setSenderId(100L);
        unread.setReceiverId(200L);
        unread.setContent("Unread message");
        unread.setIsRead(false);
        unread.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        messageRepository.save(unread);

        // Read message
        Message read = new Message();
        read.setSenderId(100L);
        read.setReceiverId(200L);
        read.setContent("Read message");
        read.setIsRead(true);
        read.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        messageRepository.save(read);

        mockMvc.perform(get("/messages/user/200/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].isRead").value(false));
    }

    @Test
    void getUnreadCount_ReturnsCount() throws Exception {
        // Create 3 unread messages
        for (int i = 0; i < 3; i++) {
            Message msg = new Message();
            msg.setSenderId(100L);
            msg.setReceiverId(200L);
            msg.setContent("Unread " + i);
            msg.setIsRead(false);
            msg.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
            messageRepository.save(msg);
        }

        mockMvc.perform(get("/messages/user/200/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void markAsRead_Success() throws Exception {
        Message msg = new Message();
        msg.setSenderId(100L);
        msg.setReceiverId(200L);
        msg.setContent("Test message");
        msg.setIsRead(false);
        msg.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
        Message saved = messageRepository.save(msg);

        mockMvc.perform(patch("/messages/" + saved.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    void markAsRead_NotFound() throws Exception {
        mockMvc.perform(patch("/messages/invalid123/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Message not found"));
    }

    @Test
    void markConversationAsRead_Success() throws Exception {
        // Create some unread messages
        for (int i = 0; i < 2; i++) {
            Message msg = new Message();
            msg.setSenderId(200L);
            msg.setReceiverId(100L);
            msg.setContent("Unread " + i);
            msg.setIsRead(false);
            msg.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
            messageRepository.save(msg);
        }

        mockMvc.perform(patch("/messages/conversation/read")
                .param("userId", "100")
                .param("otherUserId", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Conversation marked as read"));
    }
}
