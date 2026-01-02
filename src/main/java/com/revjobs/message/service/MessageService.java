package com.revjobs.message.service;

import com.revjobs.common.exception.ResourceNotFoundException;
import com.revjobs.message.dto.MessageDTO;
import com.revjobs.message.model.Message;
import com.revjobs.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Message sendMessage(Message message) {
        log.info("Sending message from {} to {}", message.getSenderId(), message.getReceiverId());

        Message saved = messageRepository.save(message);

        // Send real-time notification via WebSocket
        MessageDTO dto = convertToDTO(saved);
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId().toString(),
                "/queue/messages",
                dto);

        return saved;
    }

    public List<Message> getConversation(Long user1Id, Long user2Id) {
        return messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                user1Id, user2Id, user1Id, user2Id);
    }

    public List<Message> getUserMessages(Long userId) {
        return messageRepository.findBySenderIdOrReceiverIdOrderBySentAtDesc(userId, userId);
    }

    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId);
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Transactional
    public Message markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        message.setIsRead(true);
        return messageRepository.save(message);
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long otherUserId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsReadFalse(userId);

        unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(otherUserId))
                .forEach(msg -> msg.setIsRead(true));

        messageRepository.saveAll(unreadMessages);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        dto.setApplicationId(message.getApplicationId());
        return dto;
    }
}
