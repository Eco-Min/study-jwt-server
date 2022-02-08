package study.jwtServer.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import study.jwtServer.config.auth.PrincipalDetails;
import study.jwtServer.model.Member;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

// 스프링 시큐리티에 UsernamePasswordAuthenticationFilter 가 있음
// login 요청해서 username, password 전송하면(post) -> filter 동작
// spring security config 에서 formLogin.disabled() 동작을 안함 -> 다시 동작하기위해 filter를 추가해야한다.

/**
 * 1. username, password 받아서
 * 2. 정상인지 로그인 시도 -> authenticationManager로 로그인 시도 -> PrincipalDetailsService 실행
 * 3. PrincipalDetails를 세션에 담고 -> 권한관리를 위해
 * 4. JWT 토큰을 만들어서 응답.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    // /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("JwtAuthenticationFilter : 로그인 시도중");
        // parsing (url, json 방식이 따로 있음 -> 우린 json으로 처리)
        try {
            log.info("user to ByteObject = {}", request.getInputStream());
            ObjectMapper om = new ObjectMapper();
            Member member = om.readValue(request.getInputStream(), Member.class);
            log.info("member ObjectMapper ={}", member);

            // 로그인 시도 token 발행
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(member.getUsername(), member.getPassword());

            // PrincipalDetailsService 실행 -> authentication 리턴
            // DB에 있는 username 과 password 가 일치한다.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            log.info("principalDetails = {}", principalDetails.getUsername());

            // authentication 객체가 session영역에 저장 return -> security가 대신 해주기 때문
            // 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음 -> 권한 처리 때문에 session 넣어줌
            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    //attemptAuthentication 실행후 인증완료 -> successfulAuthentication 실행
    // JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        // Hash암호 방식
        String jwtToken = JWT.create()
                // token 이름 같은것
                .withSubject("jwt cos")
                .withExpiresAt(new Date(System.currentTimeMillis() + (60000 * 10)))
                // 비공개 value 값 -> 지정할수 있지만 확인쉽게하기위해
                .withClaim("id", principalDetails.getMember().getId())
                .withClaim("username", principalDetails.getUsername())
                // 내 서버 권한을 아는 고유의 값 -> 일단은 cos로 진행
                .sign(Algorithm.HMAC512("cos"));

        log.info("successfulAuthentication 실행 : 인증이 완료");
        response.addHeader("Authorization", "Bearer "+jwtToken);
    }
}
