package com.skytracker.security.oauth2;

import com.skytracker.dto.SocialUserRequestDto;
import com.skytracker.entity.User;
import com.skytracker.repository.UserRepository;
import com.skytracker.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Oauth2UserInfo oauth2UserInfo = getOauth2UserInfo(registrationId, oAuth2UserAttributes);

        User user = createUserFromOauth(oauth2UserInfo);

        if (!userRepository.existsByEmail(user.getEmail())){
            userRepository.save(user);
        }

        return new CustomUserDetails(user, oAuth2UserAttributes);
    }

    private Oauth2UserInfo getOauth2UserInfo(String registrationId, Map<String, Object> userAttributes) {
        if ("google".equals(registrationId)) {
            return new GoogleUserInfo(userAttributes);
        }
        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }

    private User createUserFromOauth(Oauth2UserInfo oauth2UserInfo){
        return User.from(SocialUserRequestDto.from(oauth2UserInfo));
    }
}
