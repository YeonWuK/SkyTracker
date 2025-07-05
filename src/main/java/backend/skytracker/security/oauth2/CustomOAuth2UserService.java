package backend.skytracker.security.oauth2;

import backend.skytracker.dto.SocialUserRequestDto;
import backend.skytracker.entity.User;
import backend.skytracker.security.auth.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Oauth2UserInfo oauth2UserInfo = getOauth2UserInfo(registrationId, oAuth2UserAttributes);

        User user = createUserFromOauth(oauth2UserInfo);

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
