package com.polytech.qcm.server.qcmserver.security;

import com.polytech.qcm.server.qcmserver.exception.BadCredentialsException;
import com.polytech.qcm.server.qcmserver.exception.InvalidJwtAuthenticationException;
import com.polytech.qcm.server.qcmserver.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//TUTORIEL: https://www.codementor.io/hantsy/protect-rest-apis-with-spring-security-and-jwt-ms5uu3zd6
@Component
public class JwtTokenProvider {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER = "Bearer ";
  private static final long VALIDITY_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(30); // infinite validity

  @Value("${security.jwt.token.secret-key}")
  private String secretKey; //encrypting base key for jwt token generation
  private final UserRepository userRepository;

  public JwtTokenProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public JwtToken createToken(String username, String role) {
    Claims claims = Jwts.claims().setSubject(username);
    claims.put("role", role);

    Date now = new Date();
    Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

    String token = Jwts.builder()
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(validity)
      .signWith(SignatureAlgorithm.HS256, secretKey)
      .compact();
    return new JwtToken(token, validity);
  }

  public Authentication getAuthentication(String token) {
    String username = getUsername(token);
    UserDetails userDetails = this.userRepository.findByUsername(username)
      .orElseThrow(() -> new BadCredentialsException("User with username " + username + " doesn't exists"));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER)) {
      return bearerToken.substring(BEARER.length());
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      return !claims.getBody().getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidJwtAuthenticationException("Expired or invalid JWT token", e);
    }
  }

  @Getter
  @AllArgsConstructor
  public static class JwtToken {
    private final String token;
    private final Date expiration;
  }
}
