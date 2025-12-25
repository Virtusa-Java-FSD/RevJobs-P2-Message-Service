package com.revjobs.message.repository;

import com.revjobs.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdOrReceiverIdOrderBySentAtDesc(Long senderId, Long receiverId);
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
            Long senderId1, Long receiverId1, Long senderId2, Long receiverId2);
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}

