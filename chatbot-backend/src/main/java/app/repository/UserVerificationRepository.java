package app.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.UserVerification;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
	
	Optional<UserVerification> findByEmail(String email);

	Long countByEmailAndCreatedAtBetween(String email, LocalDateTime start, LocalDateTime end);

	Optional<UserVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
}
