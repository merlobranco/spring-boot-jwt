package com.merlobranco.springboot.app.auth.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		
		boolean validToken = false;
		Claims token = null;
		try {
			token = Jwts.parserBuilder()
				.setSigningKey(generateKey())
				.build()
				.parseClaimsJws(header.replace("Bearer ", ""))
				.getBody();
			validToken = true;
		} catch (JwtException | IllegalArgumentException e) {
			
		}
		
		UsernamePasswordAuthenticationToken authentication = null;
		
		// Initializing session with an authenticated token
		if (validToken) {
			String username = token.getSubject();
			Object roles = token.get("authorities");
			Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper().readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));
			
			authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
		}
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
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
