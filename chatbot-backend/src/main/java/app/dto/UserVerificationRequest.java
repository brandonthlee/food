package app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserVerificationRequest {
	public record VerificationCodeDto(
			@NotEmpty @Size(min = 6, max = 6, message = "6 digit code.") @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Not a valid code format.") String verificationCode) {
	}
}
