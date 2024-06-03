package app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dto.UserVerificationRequest;
import app.model.MyUserDetails;
import app.security.JwtProvider;
import app.utils.ApiUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserVerificationController {
    final private UserVerificationController verificationService;

    @PostMapping("/email-verifications")
    public ResponseEntity<?> sendVerificationCode(@AuthenticationPrincipal MyUserDetails userDetails) {
        verificationService.sendVerificationCode(userDetails.getId());
        ApiUtils.Response<?> response = ApiUtils.success();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/email-verifications/confirm")
    public ResponseEntity<?> verifyVerificationCode(@AuthenticationPrincipal MyUserDetails userDetails,
                                                    @RequestBody @Valid UserVerificationRequest.VerificationCodeDto requestDto,
                                                    Errors errors) {
        String jwt = verificationService.verifyVerificationCode(userDetails.getId(), requestDto);
        ApiUtils.Response<?> response = ApiUtils.success();
        return ResponseEntity.ok().header(JwtProvider.HEADER, jwt).body(response);
    }
}