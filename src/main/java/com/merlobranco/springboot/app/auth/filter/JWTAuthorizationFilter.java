package com.merlobranco.springboot.app.auth.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String header = request.getHeader("Authorization");
		if (!requiresAuthentication(header)) {
			chain.doFilter(request, response);
			return;
		}
		
		boolean tokenValid = false;
		Claims claims = null;
		try {
			claims = Jwts.parserBuilder()
				.setSigningKey(generateKey())
				.build()
				.parseClaimsJws(header.replace("Bearer ", ""))
				.getBody();
			tokenValid = true;
		} catch (JwtException | IllegalArgumentException e) {
			
		}
		
		if (tokenValid) {
			
		}
	}
	
	protected boolean requiresAuthentication(String header) {
		return header != null && header.startsWith("Bearer ");
	}
	
	private SecretKey generateKey() {
        byte[] originalKey = "Alguna.Clave.Secreta.123456".getBytes(StandardCharsets.UTF_8);
        byte[] finalKey = new byte[256];
        System.arraycopy(originalKey, 0, finalKey, 0, originalKey.length);
        return Keys.hmacShaKeyFor(finalKey);
    } 
}
