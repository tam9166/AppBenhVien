package com.hospital.repository;

import com.hospital.entity.ChatbotConversationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotConversationLogRepository extends JpaRepository<ChatbotConversationLog, Long> {
}
