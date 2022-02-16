package study.jwtServer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import study.jwtServer.config.jwt.JwtAuthenticationFilter;
import study.jwtServer.config.jwt.JwtAuthorizationFilter;
import study.jwtServer.filter.MyFilter3;
import study.jwtServer.model.MemberRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CorsConfig corsConfig;
    private final MemberRepository memberRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // security filter 를 기준으로(filter종류를 알아야함) -> 다른 필터를 다른데 등록하더라도 security 가 가장먼저 실행되기 때문
//        http.addFilterBefore(new MyFilter3(), SecurityContextPersistenceFilter.class);
//        http.addFilterAfter(new MyFilter3(), SecurityContextPersistenceFilter.class);
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용X
                .and()
                .addFilter(corsConfig.corsFilter()) // 인증이 필요한경우 filter에 등록 해야함
                .formLogin().disable() // token 으로 발급 -> formLogin 을 사용 안하겠다
                .httpBasic().disable() // cookie 를 발급하는 기본 http 방식을 사용하지 않겠다는 뜻이빈다.
                .addFilter(new JwtAuthenticationFilter(authenticationManager())) // AutehnticationMager를 던져줘야한다
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), memberRepository))
                .authorizeRequests()
                .antMatchers("/api/v1/user/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/manager/**")
                .access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/admin/**")
                .access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll();

    }
}
