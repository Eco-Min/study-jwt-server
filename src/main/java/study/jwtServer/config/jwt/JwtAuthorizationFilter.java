package study.jwtServer.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import study.jwtServer.config.auth.PrincipalDetails;
import study.jwtServer.model.Member;
import study.jwtServer.model.MemberRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// security filter 중 BasicAuthenticationFilter 라는 것이 있음
// 권한이나 인증이 필요한 특정 주소 요청시 -> 필터를 무조건 탐
// 권한이 인증이 필요한 주소가 아니라면 이 필터를 안탐
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    // 한번더 인증 작업을 거치기 위해 security 에서 di 로 받아옴
    private MemberRepository memberRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
        log.info("JwtAuthorizationFilter 확인");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("BasicAuthenticationFilter : 인증 권한 필요");
        // doFilter 를 사용하려면 이부분을 막아야 한다.
        // doFilter 의 역활을 하고 있으며 인증이 완료 될때 doFilter 대신 넘겨도 됨.
//        super.doFilterInternal(request, response, chain);

        String jwtHeader = request.getHeader("Authorization");
        log.info("jwtHeader = {}", jwtHeader);

        //header 가 있는 지확인
        if (jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
            chain.doFilter(request, response);
            return;
        }

        //JWT token 검증후 사용자 확인
        String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
//        log.info("jwtToken = {}", jwtToken);

        // JwtAuthenticationFilter 의 jwt 생성시 서버 고유의 값
        String username = JWT.require(Algorithm.HMAC512("cos"))
                .build()
                .verify(jwtToken)
                .getClaim("username").asString();
//        log.info("username = {}", username);


        // jwt 인증이 됨
        if (username != null) {
            Member member = memberRepository.findByUsername(username);
            PrincipalDetails principalDetails = new PrincipalDetails(member);

            //JwtAuthenticationFilter 에 있는 authentication 같은경우 login 진행시 만들어지는 것입니다.
            //JwtToken 으로 인증을 한후 다시 username 까지 확인을 거친거기 때문에 비밀번호 없어도 들어 오게 만드는 방법
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

            // 강제로 시큐리티의 세선에 접근 Authentication 객체 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

//            chain.doFilter(request, response);
        }
    }
}
