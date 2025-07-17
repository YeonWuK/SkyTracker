package com.skytracker.entity;

import com.skytracker.dto.Role;
import com.skytracker.dto.SocialUserRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "username")
    private String username;

    @Column(name = "nickname")
    private String nickname;

    public static User from(SocialUserRequestDto socialUserRequestDto) {
        return User.builder()
                .email(socialUserRequestDto.getEmail())
                .provider(socialUserRequestDto.getProvider())
                .nickname(null)
                .username(socialUserRequestDto.getName())
                .role(socialUserRequestDto.getRole())
                .build();
    }
}
