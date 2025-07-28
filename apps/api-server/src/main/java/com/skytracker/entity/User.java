package com.skytracker.entity;

import com.skytracker.dto.user.Role;
import com.skytracker.dto.user.SocialUserRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity{

    @Column(name = "user_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlightAlert> userFlightAlert = new ArrayList<>();

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