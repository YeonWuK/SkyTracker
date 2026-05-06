package com.skytracker.controller;

import com.skytracker.dto.RefreshTokenRequest;
import com.skytracker.dto.user.UserResponseDto;
import com.skytracker.dto.user.UserUpdateRequestDto;
import com.skytracker.security.auth.CustomUserDetails;
import com.skytracker.security.auth.JwtUtils;
import com.skytracker.service.token.TokenBlackListService;
import com.skytracker.service.UserService;
import com.skytracker.validation.CustomValidators;
import com.skytracker.validation.ValidationSequence;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtils jwtUtil;
    private final UserService userService;
    private final CustomValidators validators;
    private final TokenBlackListService tokenBlackListService;

    /**
     * 현재 로그인한 사용자의 회원 정보를 수정한다.
     */
    @PatchMapping
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                        @Validated(ValidationSequence.class) @RequestBody UserUpdateRequestDto dto,
                                        BindingResult bindingResult) {

        validators.updateValidate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        userService.update(customUserDetails.getUserId(), dto);
        UserResponseDto updateUser = userService.getUser(customUserDetails.getUserId());

        return ResponseEntity.ok(updateUser);
    }

    /**
     * 현재 로그인한 사용자의 회원 계정을 삭제한다.
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        userService.deleteUser(customUserDetails.getUserId());

        return ResponseEntity.ok("User deleted successfully." + customUserDetails.getUserId());
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회한다.
     */
    @GetMapping("/profile-screen")
    public ResponseEntity<UserResponseDto> profileScreen(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UserResponseDto user = userService.getUser(customUserDetails.getUserId());

        return ResponseEntity.ok(user);
    }

    /**
     * accessToken을 블랙리스트에 등록하고 SecurityContext를 비운다.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            long expiration = jwtUtil.extractAllClaims(accessToken).getExpiration().getTime() - System.currentTimeMillis();

            tokenBlackListService.addToBlackList(accessToken, expiration);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().body("로그아웃 완료");
    }

    /**
     * refreshToken을 검증하고 새 accessToken을 발급한다.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        }

        if (tokenBlackListService.isBlackList(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Blacklist refresh token expired");
        }

        String username = jwtUtil.extractUserEmail(refreshToken);
        String newAccessToken = jwtUtil.generateToken(username);

        return ResponseEntity.ok("{\"accessToken\": \"" + newAccessToken + "\"}");
    }

    /**
     * 현재 로그인한 사용자에게 새 refreshToken을 발급한다.
     */
    @PostMapping("/new-refresh-token")
    public ResponseEntity<?> newRefreshToken(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        UserResponseDto user = userService.getUser(customUserDetails.getUserId());

        String email = user.getEmail();

        String refreshToken = jwtUtil.generateRefreshToken(email);

        return ResponseEntity.ok("{\"refreshToken\": \"" + refreshToken + "\"}");
    }
}
