package com.revjobs.message.service;

import com.revjobs.common.exception.ResourceNotFoundException;
import com.revjobs.message.dto.MessageDTO;
import com.revjobs.message.model.Message;
import com.revjobs.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

        @Mock
        private MessageRepository messageRepository;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

        @InjectMocks
        private MessageService messageService;

        private Message testMessage;

        @BeforeEach
        void setUp() {
                testMessage = new Message();
                testMessage.setId("msg123");
                testMessage.setSenderId(100L);
                testMessage.setReceiverId(200L);
                testMessage.setContent("Hello, I am interested in the position");
                testMessage.setIsRead(false);
                testMessage.setSentAt(ZonedDateTime.now(ZoneId.of("UTC")));
                testMessage.setApplicationId(1L);
        }

        @Test
        void sendMessage_SavesAndSendsViaWebSocket() {
                // Given
                when(messageRepository.save(any(Message.class)))
                                .thenReturn(testMessage);
                doNothing().when(messagingTemplate)
                                .convertAndSendToUser(anyString(), anyString(), any(MessageDTO.class));

                // When
                Message result = messageService.sendMessage(testMessage);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo("msg123");
                assertThat(result.getSenderId()).isEqualTo(100L);
                assertThat(result.getReceiverId()).isEqualTo(200L);

                verify(messageRepository).save(testMessage);
                verify(messagingTemplate).convertAndSendToUser(
                                eq("200"),
                                eq("/queue/messages"),
                                any(MessageDTO.class));
        }

        @Test
        void getConversation_ReturnsConversationMessages() {
                // Given
                Message msg2 = new Message();
                msg2.setId("msg456");
                msg2.setSenderId(200L);
                msg2.setReceiverId(100L);
                msg2.setContent("Thank you for your interest");

                List<Message> conversation = Arrays.asList(testMessage, msg2);
                when(messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                                100L, 200L, 100L, 200L))
                                .thenReturn(conversation);

                // When
                List<Message> result = messageService.getConversation(100L, 200L);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).containsExactly(testMessage, msg2);
                verify(messageRepository).findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                                100L, 200L, 100L, 200L);
        }

        @Test
        void getUserMessages_ReturnsUserMessages() {
                // Given
                List<Message> messages = Arrays.asList(testMessage);
                when(messageRepository.findBySenderIdOrReceiverIdOrderBySentAtDesc(100L, 100L))
                                .thenReturn(messages);

                // When
                List<Message> result = messageService.getUserMessages(100L);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(testMessage);
                verify(messageRepository).findBySenderIdOrReceiverIdOrderBySentAtDesc(100L, 100L);
        }

        @Test
        void getUnreadMessages_ReturnsOnlyUnreadMessages() {
                // Given
                List<Message> unreadMessages = Arrays.asList(testMessage);
                when(messageRepository.findByReceiverIdAndIsReadFalse(200L))
                                .thenReturn(unreadMessages);

                // When
                List<Message> result = messageService.getUnreadMessages(200L);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getIsRead()).isFalse();
                verify(messageRepository).findByReceiverIdAndIsReadFalse(200L);
        }

        @Test
        void getUnreadCount_ReturnsCount() {
                // Given
                when(messageRepository.countByReceiverIdAndIsReadFalse(200L))
                                .thenReturn(5L);

                // When
                long count = messageService.getUnreadCount(200L);

                // Then
                assertThat(count).isEqualTo(5L);
                verify(messageRepository).countByReceiverIdAndIsReadFalse(200L);
        }

        @Test
        void markAsRead_Success() {
                // Given
                when(messageRepository.findById("msg123"))
                                .thenReturn(Optional.of(testMessage));

                Message readMessage = new Message();
                readMessage.setId("msg123");
                readMessage.setIsRead(true);

                when(messageRepository.save(any(Message.class)))
                                .thenReturn(readMessage);

                // When
                Message result = messageService.markAsRead("msg123");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getIsRead()).isTrue();
                verify(messageRepository).findById("msg123");
                verify(messageRepository).save(any(Message.class));
        }

        @Test
        void markAsRead_ThrowsResourceNotFoundException_WhenNotFound() {
                // Given
                when(messageRepository.findById("invalid"))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> messageService.markAsRead("invalid"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Message not found");

                verify(messageRepository).findById("invalid");
                verify(messageRepository, never()).save(any());
        }

        @Test
        void markConversationAsRead_MarksAllMessagesFromSenderAsRead() {
                // Given
                Message msg1 = new Message();
                msg1.setId("msg1");
                msg1.setSenderId(200L);
                msg1.setReceiverId(100L);
                msg1.setIsRead(false);

                Message msg2 = new Message();
                msg2.setId("msg2");
                msg2.setSenderId(200L);
                msg2.setReceiverId(100L);
                msg2.setIsRead(false);

                List<Message> unreadMessages = Arrays.asList(msg1, msg2);
                when(messageRepository.findByReceiverIdAndIsReadFalse(100L))
                                .thenReturn(unreadMessages);
                when(messageRepository.saveAll(any()))
                                .thenReturn(unreadMessages);

                // When
                messageService.markConversationAsRead(100L, 200L);

                // Then
                verify(messageRepository).findByReceiverIdAndIsReadFalse(100L);
                verify(messageRepository).saveAll(unreadMessages);
        }
}
