package com.motioncad.server.security;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update provider info if user exists
            if (user.getProvider() == null) {
                user.setProvider(registrationId.toUpperCase());
                user.setProviderId(userInfo.getProviderId());
                user = userRepository.save(user);
            }
        } else {
            // Create new user
            user = createUser(userInfo, registrationId);
        }

        return oauth2User;
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, java.util.Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }

    private User createUser(OAuth2UserInfo userInfo, String registrationId) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .provider(registrationId.toUpperCase())
                .providerId(userInfo.getProviderId())
                .userSettings(UserSettings.builder()
                        .handSensitivity(50)
                        .isLeftHanded(false)
                        .cameraResolution("720p")
                        .uiTheme("dark")
                        .build())
                .build();

        return userRepository.save(user);
    }
}
