package study.jwtServer.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class MyFilter1 implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;

        if (Objects.equals(req.getMethod(), "POST")) {
            log.info("post 요청");
            String headerAuth = req.getHeader("Authorization");
            log.info("{}", headerAuth);
        }

        log.info("필터 1");
        chain.doFilter(req, rep);
    }
}
