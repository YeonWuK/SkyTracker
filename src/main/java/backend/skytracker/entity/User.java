package backend.skytracker.entity;

import backend.skytracker.dto.Role;
import backend.skytracker.dto.SocialUserRequestDto;
import backend.skytracker.security.oauth2.Oauth2UserInfo;
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

    @Column(name = "passowrd")
    private String password;

    @Column(name = "provider", nullable = false)
    private String provider;

    private String providerId;

    @Column(name = "name")
    private String name;

    public static User from(SocialUserRequestDto socialUserRequestDto) {
        return User.builder()
                .provider(socialUserRequestDto.getProvider())
                .build();
    }
}
