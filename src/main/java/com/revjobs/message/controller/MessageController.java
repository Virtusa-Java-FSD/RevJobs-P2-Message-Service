package com.revjobs.message.controller;

import com.revjobs.common.dto.ApiResponse;
import com.revjobs.message.model.Message;
import com.revjobs.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Message message) {
        messageService.sendMessage(message);
    }

    @RestController
    @RequestMapping("/messages")
    @RequiredArgsConstructor
    public static class MessageRestController {

        private final MessageService messageService;

        @PostMapping
        public ResponseEntity<ApiResponse<Message>> sendMessage(@RequestBody Message message) {
            Message sent = messageService.sendMessage(message);
            return ResponseEntity.ok(ApiResponse.success("Message sent successfully", sent));
        }

        @GetMapping("/conversation")
        public ResponseEntity<ApiResponse<List<Message>>> getConversation(
                @RequestParam Long user1Id, @RequestParam Long user2Id) {
            List<Message> messages = messageService.getConversation(user1Id, user2Id);
            return ResponseEntity.ok(ApiResponse.success(messages));
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<Message>>> getUserMessages(@PathVariable Long userId) {
            List<Message> messages = messageService.getUserMessages(userId);
            return ResponseEntity.ok(ApiResponse.success(messages));
        }

        @GetMapping("/unread/{userId}")
        public ResponseEntity<ApiResponse<List<Message>>> getUnreadMessages(@PathVariable Long userId) {
            List<Message> messages = messageService.getUnreadMessages(userId);
            return ResponseEntity.ok(ApiResponse.success(messages));
        }

        @GetMapping("/unread-count/{userId}")
        public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
            long count = messageService.getUnreadCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        }

        @PatchMapping("/{messageId}/read")
        public ResponseEntity<ApiResponse<Message>> markAsRead(@PathVariable String messageId) {
            Message message = messageService.markAsRead(messageId);
            return ResponseEntity.ok(ApiResponse.success("Message marked as read", message));
        }

        @PatchMapping("/conversation/read")
        public ResponseEntity<ApiResponse<Void>> markConversationAsRead(
                @RequestParam Long userId, @RequestParam Long otherUserId) {
            messageService.markConversationAsRead(userId, otherUserId);
            return ResponseEntity.ok(ApiResponse.success("Conversation marked as read", null));
        }
    }
}
