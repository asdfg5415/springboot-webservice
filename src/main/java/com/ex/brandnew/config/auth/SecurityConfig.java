package com.ex.brandnew.config.auth;

import com.ex.brandnew.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity      // SpringSecurity 설정을 활성화시켜줍니다.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()     // H2-console 화면을 사용하기위해 해당 옵션들을 disable 해준다.
                .and()
                    //
                    /*
                    - authorizeRequests() 는 URL 별 권한 관리를 설정하는 옵션의 시작점이다.
                    - antMatchers 로 권한 관리 대상을 지정한다. URL,HTTP 메소드별로 관리가 가능함
                      '/' 로 지정된 애들은 전체 열람 권한을 준다.
                      '/api/v1/**' 인 애들은 USER  권한을 가진 사람만 가능하다.
                    - anyRequest는 설정된 값들 이외 나머지 URL들을 나타낸다.
                    - authenticated() 를 추가해서 나머지 URL들은 모두 인증된 사용자들에게만 허용하게 한다.
                     */
                    .authorizeRequests()
                    .antMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/profile").permitAll()
                    .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                    .anyRequest().authenticated()
                .and()
                    /*
                    - logout() : 로그아웃 기능에 대한 여러 설정의 진입점이다.
                    - logSuccessUrl() : 로그아웃이 잘 되면 해당 URL 로 이동한다.
                     */
                    .logout()
                        .logoutSuccessUrl("/")
                .and()
                    /*
                    - oauth2Login() : OAuth2 로그인 기능에 대한 여러 설정의 진입점이다.
                    - userInfoEndpoint() : 로그인 성공 이후 사용자 정보를 가져올 떄의 설정들을 담당한다.
                    - userService(customOAuth2UserService) : 소셜 로그인 성공 시 후속 조치를 진행할 UserService의 구현체를 등록한다.
                     */
                    .oauth2Login()
                        .userInfoEndpoint()
                            .userService(customOAuth2UserService);
    }
}