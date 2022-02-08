package study.jwtServer.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

@Slf4j
public class MyFilter3 implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;
        log.info("필터 3");

        if (Objects.equals(req.getMethod(), "POST")) {
            log.info("post 요청");
            String headerAuth = req.getHeader("Authorization");
            log.info("{}", headerAuth);

            // post 이며 cos 라는 입력이 있을때 다음 필터 실행
            // 이부분에서 token을 만들어 줘야함 -> ID, PW 로그인 완료 되면 토큰 만들어주고 그걸 응답
            // 요청할때 마다 header 에 Authorization에 value 값으로 토킅을 가지고옴
            // 이때 토큰이 넘어모면 토큰 검증만 하면 됨.
            if (headerAuth.equals("cos")) {
                chain.doFilter(req, rep);
            } else {
                PrintWriter out = rep.getWriter();
                out.println("인증안됨");
            }
        }
    }
}
