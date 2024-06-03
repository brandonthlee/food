package app.repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

	void deleteAllByChatroomId(Long chatroomId);

	List<Message> findTop38ByChatroomIdOrderByIdDesc(Long chatroomId);

	Optional<Message> findTop1ByChatroomIdOrderByIdDesc(Long chatroomId);

	List<Message> findAllByChatroomIdAndIdLessThanOrderByIdDesc(Long chatroomId, Long key, Pageable pageable);

	List<Message> findAllByChatroomIdOrderByIdDesc(Long chatroomId, Pageable pageable);

	List findAllByChatroomIdOrderByIdDesc(Long chatroomId);
}