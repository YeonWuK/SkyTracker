package com.skytracker.controller;

import com.skytracker.dto.user.UserResponseDto;
import com.skytracker.dto.user.UserUpdateRequestDto;
import com.skytracker.security.auth.CustomUserDetails;
import com.skytracker.security.auth.JwtUtils;
import com.skytracker.service.TokenBlackListService;
import com.skytracker.service.UserService;
import com.skytracker.validation.CustomValidators;
import com.skytracker.validation.ValidationSequence;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtils jwtUtil;
    private final UserService userService;
    private final CustomValidators validators;
    private final TokenBlackListService tokenBlackListService;

    /**
     * 회원 정보 수정
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
     *  회원 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        userService.deleteUser(customUserDetails.getUserId());

        return ResponseEntity.ok("User deleted successfully." + customUserDetails.getUserId());
    }


    /**
     * 로그아웃
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
     * accessToken 재발금
     */
    @PostMapping("/refresh-toekn")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token failed");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(null);

        if (refreshToken == null || jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        }

        if (tokenBlackListService.isBlackList(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Blacklist refresh token expired");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        String newAccessToken = jwtUtil.generateToken(username);

        return ResponseEntity.ok("{\"newAccessToken\": \"" + newAccessToken + "\"}");
    }
}