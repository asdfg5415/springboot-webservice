package com.ex.brandnew.config.auth;


import com.ex.brandnew.config.auth.dto.OAuthAttributes;
import com.ex.brandnew.config.auth.dto.SessionUser;
import com.ex.brandnew.domain.user.User;
import com.ex.brandnew.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        /*
        - 현재 로그인 진행 중인 서비스를 구분하는 코드이다.
        - 현재는 구글만 사용하기 때문에 상관이 없지만, 네이버나 카카오 등 다른 애들이랑 같이 사용하게되면 필요하다.
         */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();


        /*
        - OAuth2 로그인 진행 시 키가 되는 필드값을 이야기 합니다. Primary Key 와 같은 의미입니다.
        - 구글의 경우 기본적으로 코드를 지원하지만, 네이버 카카오 등은 기본 지원하지 않습니다.
        - 구글의 기본 코드는 "sub" 입니다.
        - 이후 다른 로그인들을 동시 지원할 때 사용됩니다.
         */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();


        /*
        - OAuthAttributes 클래스 : OAuth2UserService 를 통해서 가져온 OAuth2User의 attributes를 담을 클래스이다.
        - 이후 다른 로그인들도 이 클래스를 사용한다.
         */
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());


        /*

        - SessionUser 클래스 : 세션에 사용자 정보를 저장하기 위한 Dto 클래스이다.

        => 왜 User 클래스를 쓰지 않나요?
        => 직렬화 하지 않았다는 에러가 발생한다.
        => User는 엔티티 클래스이기 때문에 다른 엔티티와 연관 관계를 맺고 있을 확률이 높다. 따라서, 성능 이슈, 부수 효과가 발생할 확률이 높다.
        => 그래서 직렬화 기능을 가진 세션 Dto를 하나 추가로 만드는 것이 이후 운영 및 유지보수 때 많은 도움이 된다.
        */
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }


    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
