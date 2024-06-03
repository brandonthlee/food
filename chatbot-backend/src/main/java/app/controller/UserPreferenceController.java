import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dto.UserPreferenceRequest;
import app.model.MyUserDetails;
import app.service.UserPreferenceService;
import app.utils.ApiUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserPreferenceController {

    final private UserPreferenceService preferenceService;

    @PostMapping("/favors")
    public ResponseEntity<?> saveUserFoodPreference(@AuthenticationPrincipal MyUserDetails userDetails,
                                                    @RequestBody @Valid UserPreferenceRequest.SaveUserFoodPreferenceDto requestDto, Errors errors) {
    	preferenceService.saveUserFoodPreference(userDetails.getId(), requestDto);
        ApiUtils.Response<?> response = ApiUtils.success(requestDto);
        return ResponseEntity.ok().body(response);
    }
}