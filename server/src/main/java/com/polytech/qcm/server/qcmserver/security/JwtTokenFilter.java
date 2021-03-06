package com.polytech.qcm.server.qcmserver.security;

import com.polytech.qcm.server.qcmserver.exception.InvalidJwtAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JwtTokenFilter extends GenericFilterBean {

  private JwtTokenProvider jwtTokenProvider;

  public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
    throws IOException, ServletException {

    String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
    try {
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }  catch (InvalidJwtAuthenticationException exception) {
      //exceptions are not caught by the ResponseExceptionHandler
      //so we have to do a little hack
      HttpServletResponse response = (HttpServletResponse) res;
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      try (PrintWriter out = response.getWriter()) {
        out.println(String.format("{ \"error\": \"Bad authentication\", \"message\":\"%s\"}", exception.getMessage()));
      }
      return;
    }

    filterChain.doFilter(req, res);
  }


}