package app.service;

import java.util.Objects;

import org.hibernate.mapping.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dto.UserRequest;
import app.dto.UserResponse;
import app.exception.BadRequestException;
import app.exception.EmailAlreadyExistException;
import app.exception.LoginIdAlreadyExistException;
import app.exception.LoginIdNotFoundException;
import app.exception.ResouceLimitException;
import app.exception.UserForbiddenException;
import app.model.ChatRoom;
import app.model.Role;
import app.model.User;
import app.model.UserPreference;
import app.repository.ChatRoomRepository;
import app.repository.MessageRepository;
import app.repository.UserPreferenceRepository;
import app.repository.UserRepository;
import app.security.JwtProvider;
import app.utils.DateUtils;
import app.utils.MailUtils;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	private final UserPreferenceRepository preferenceRepository;

	private final ChatRoomRepository chatRoomRepository;

	private final MessageRepository messageRepository;

	private final PasswordEncoder passwordEncoder;

	private final JavaMailSender javaMailSender;

	private final RedisTemplate<String, String> redisTemplate;

	@Transactional
	public String join(UserRequest.JoinDto requestDto) {

		if (!Objects.equals(requestDto.password(), requestDto.passwordCheck())) {
			throw new UserForbiddenException("The password confirmation does not match!");
		}

		if (userRepository.findByLoginId(requestDto.loginId()).isPresent()) {
			throw new LoginIdAlreadyExistException("LoginId already exists!");
		}

		if (userRepository.findByEmail(requestDto.email()).isPresent()) {
			throw new EmailAlreadyExistException("Email address already exists!");
		}

		String encodedPassword = passwordEncoder.encode(requestDto.password());

		var user = requestDto.createUser(encodedPassword);

		try {
			return JwtProvider.create(userRepository.save(user));
		} catch (Exception e) {
			throw new ResouceLimitException("Failed registering account! Please try again.");
		}
	}

	public String issueJwtByLogin(UserRequest.LoginDto requestDto) {
		User user = userRepository.findByLoginId(requestDto.loginId())
				.orElseThrow(() -> new LoginIdNotFoundException("Login failed! Please try again."));

		if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
			throw new LoginIdNotFoundException("Login failed! Please try again.");
		}
		return JwtProvider.create(user);
	}

	public UserResponse.GetUserDto getUser(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new LoginIdNotFoundException("LoginId does not exist!"));

		List<UserPreference> userPreferences = preferenceRepository.findByUserId(id);

		return new UserResponse.GetUserDto(user, userPreferences);
	}

	@Transactional
	public String updateUser(Long id, UserRequest.UpdateDto requestDto) {
		
		var user = userRepository.findById(id).orElseThrow(() -> new ResouceLimitException("Error updating user! Please try again."));
		if (requestDto.loginId() != null) {
			if (!Objects.equals(user.getLoginId(), requestDto.loginId())) {
				if (userRepository.findByLoginId(requestDto.loginId()).isPresent()) {
					throw new EmailAlreadyExistException("LoginId already exists!");
				}
				user.updateLoginId(requestDto.loginId());
			}
		}
		if (requestDto.password() != null) {
			if (!Objects.equals(requestDto.password(), requestDto.passwordCheck())) {
				throw new LoginIdNotFoundException("LoginId does not exist!");
			}
			String encodedPassword = passwordEncoder.encode(requestDto.password());
			user.updatePassword(encodedPassword);
		}

		if (requestDto.name() != null) {
			user.updateName(requestDto.name());
		}

		if (requestDto.gender() != null) {
			user.updateGender(requestDto.gender());
		}

		if (requestDto.birth() != null) {
			user.updateBirth(DateUtils.convertStringToDate(requestDto.birth()));
		}

		if (requestDto.email() != null) {
			user.updateEmail(requestDto.email());
			user.updateRole(Role.ROLE_PENDING);
		}
		return JwtProvider.create(user);
	}

	@Transactional
	public void deleteUser(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new LoginIdNotFoundException("LoginId not found!"));
		List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(id);

		preferenceRepository.deleteAllByUserId(id);
		for (ChatRoom chatRoom : chatRooms) {
			messageRepository.deleteAllByChatroomId(chatRoom.getId());
			chatRoomRepository.deleteById(chatRoom.getId());
		}
		userRepository.delete(user);
	}

	public void validateLoginId(UserRequest.ValidateLoginIdDto requestDto) {
		if (userRepository.findByLoginId(requestDto.loginId()).isPresent()) {
			throw new LoginIdAlreadyExistException("LoginId already exists!");
		}
	}

	public void validateEmail(UserRequest.ValidateEmailDto requestDto) {
		if (userRepository.findByEmail(requestDto.email()).isPresent()) {
			throw new EmailAlreadyExistException("Email already exists!");
		}
	}

	public UserResponse.FindUserIdDto findUserId(UserRequest.FindUserIdDto requestDto) {
		User user = userRepository.findByEmail(requestDto.email())
				.orElseThrow(() -> new LoginIdNotFoundException("LoginId not found!"));

		return new UserResponse.FindUserIdDto(user.getLoginId());
	}

	@Transactional
	public void resetPassword(UserRequest.ResetPasswordDto requestDto) {
		User user = userRepository.findByLoginId(requestDto.loginId())
				.orElseThrow(() -> new LoginIdNotFoundException("LoginId not found!"));

		if (!Objects.equals(user.getEmail(), requestDto.email())) {
			throw new LoginIdNotFoundException("LoginId not found!");
		}

		String randomPassword = MailUtils.generateRandomPassword();
		String encodedPassword = passwordEncoder.encode(randomPassword);
		log.info("Temporary password: {}", randomPassword);
		user.updatePassword(encodedPassword);

		sendEmail(user.getEmail(), randomPassword);
	}

	private void sendEmail(String email, String password) {
		String subject = "Temporary password";
		String text = "Temporary password: " + password + "</br>";

		MailUtils.sendEmail(javaMailSender, email, subject, text);
	}
}
