package app.controller;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dto.UserRequest;
import app.dto.UserResponse;
import app.exception.DateConversionException;
import app.exception.UserForbiddenException;
import app.model.MyUserDetails;
import app.security.JwtProvider;
import app.service.UserService;
import app.utils.ApiUtils;
import app.utils.DateUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

	final private UserService userService;

	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUser(@PathVariable Long id, @AuthenticationPrincipal MyUserDetails userDetails) {
		if (!Objects.equals(userDetails.getId(), id)) {
			throw new UserForbiddenException("User does not have the valid permission!");
		}
		UserResponse.GetUserDto getUserDto = userService.getUser(id);
		ApiUtils.Response<?> response = ApiUtils.success(getUserDto);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/join")
	public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDto requestDto, Errors errors) {
		validateBirthForm(requestDto.birth());
		var responseDto = userService.join(requestDto);

		var responseCookie = createRefreshTokenCookie(responseDto.refresh(), JwtProvider.REFRESH_EXP_SEC);

		return ResponseEntity.ok().header(JwtProvider.HEADER, responseDto.access())
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(ApiUtils.success());
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid UserRequest.LoginDto requestDto, Errors errors) {
		var tokensDto = userService.issueJwtByLogin(requestDto);

		var responseCookie = createRefreshTokenCookie(tokensDto.refresh(), JwtProvider.REFRESH_EXP_SEC);

		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok().header(JwtProvider.HEADER, tokensDto.access())
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(response);
	}

	@PostMapping("/authentication")
	public ResponseEntity<?> reIssueTokens(@CookieValue("refreshToken") String refreshToken) {
		var tokensDto = userService.reIssueTokens(refreshToken);

		var responseCookie = createRefreshTokenCookie(tokensDto.refresh(), JwtProvider.REFRESH_EXP_SEC);

		var response = ApiUtils.success();
		return ResponseEntity.ok().header(JwtProvider.HEADER, tokensDto.access())
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@AuthenticationPrincipal MyUserDetails userDetails) {
		userService.logout(userDetails.getId());

		var responseCookie = createRefreshTokenCookie("", 0);

		var response = ApiUtils.success();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(response);
	}

	private static ResponseCookie createRefreshTokenCookie(String refreshToken, int maxAge) {
		return ResponseCookie.from("refreshToken", refreshToken).httpOnly(true)
				.secure(true)
				.sameSite("None").maxAge(maxAge).build();
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @AuthenticationPrincipal MyUserDetails userDetails,
			@RequestBody @Valid UserRequest.UpdateDto requestDto, Errors errors) {
		if (!Objects.equals(userDetails.getId(), id)) {
			throw new UserForbiddenException("User does not have the valid permission!");
		}

		validateBirthForm(requestDto.birth());

		String jwt = userService.updateUser(id, requestDto);
		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok().header(JwtProvider.HEADER, jwt).body(response);
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal MyUserDetails userDetails) {
		if (!Objects.equals(userDetails.getId(), id)) {
			throw new UserForbiddenException("User does not have the valid permission!");
		}

		userService.deleteUser(id);
		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/help/loginId")
	public ResponseEntity<?> findUserId(@RequestBody @Valid UserRequest.FindUserIdDto requestDto, Errors errors) {
		UserResponse.FindUserIdDto responseDto = userService.findUserId(requestDto);
		ApiUtils.Response<?> response = ApiUtils.success(responseDto);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/help/password")
	public ResponseEntity<?> resetPassword(@RequestBody @Valid UserRequest.ResetPasswordDto requestDto, Errors errors) {
		userService.resetPassword(requestDto);
		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/validate/loginId")
	public ResponseEntity<?> validateLoginId(@RequestBody @Valid UserRequest.ValidateLoginIdDto requestDto,
			Errors errors) {
		userService.validateLoginId(requestDto);
		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok(response);
	}

	@PostMapping("/validate/email")
	public ResponseEntity<?> validateEmail(@RequestBody @Valid UserRequest.ValidateEmailDto requestDto, Errors errors) {
		userService.validateEmail(requestDto);
		ApiUtils.Response<?> response = ApiUtils.success();
		return ResponseEntity.ok(response);
	}

	private static void validateBirthForm(String birth) {
		if (birth != null) {
			List<Integer> birthSplit = Arrays.stream(birth.split("-")).map(Integer::parseInt).toList();
			if (!DateUtils.validateDayOfDateString(birthSplit.get(0), birthSplit.get(1), birthSplit.get(2)))
				throw new DateConversionException("Not a valid birth date format! ex) 1990-01-01");
		}
	}
}
