package app.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.model.ChatLog;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

    Long countByIpAndCreatedAtBetween(String ipAddr, LocalDateTime start, LocalDateTime end);
}