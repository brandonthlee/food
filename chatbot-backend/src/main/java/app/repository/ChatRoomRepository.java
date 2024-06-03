package app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import app.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findAllByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT c FROM Chatroom c JOIN FETCH c.user u WHERE c.id=:id")
    Optional<ChatRoom> findByIdJoinUser(Long id);

    List<ChatRoom> findAllByUserId(Long id);
}