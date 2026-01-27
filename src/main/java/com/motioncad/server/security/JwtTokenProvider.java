package com.motioncad.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret:very-secret-key-that-should-be-at-least-thirty-two-characters}")
	private String secretKey;

	@Value("${jwt.expiration:86400000}")
	private long validityInMilliseconds;

	private Key key;

	@PostConstruct
	protected void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String createToken(String email, Long userId) {
		Claims claims = Jwts.claims().setSubject(email);
		claims.put("userId", userId);
		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInMilliseconds);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);
		String email = claims.getSubject();
		Long userId = claims.get("userId", Long.class);

		List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		UserPrincipal principal = new UserPrincipal(userId, email, "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public String getSubject(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		}
		return false;
	}
}
