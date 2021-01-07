package com.merlobranco.springboot.app.auth.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private AuthenticationManager authenticationManager;
	

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}
		
		if (username != null && password != null) {
			logger.info("Username desde request parameter (form-data): " + username);
			logger.info("Password desde request parameter (form-data): " + password);
		}

		username = username.trim();
		
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
		return authenticationManager.authenticate(authToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		SecretKey secretKey = generateKey();
		
//		String username = authResult.getName();
		String username = ((User)authResult.getPrincipal()).getUsername();
		String token = Jwts.builder()
				.setSubject(username)
				.signWith(secretKey)
				.compact();
		
		response.addHeader("Authorization", "Bearer " + token);
		
		Map<String, Object> body = new HashMap<>();
		body.put("token", token);
		body.put("user", (User)authResult.getPrincipal());
		body.put("mensaje", String.format("Hola %s, has iniciado sesión con éxito!", username));
		
		// Converting the map to Json using jackson
		response.getWriter().write(new ObjectMapper().writeValueAsString(body));
		response.setStatus(200);
		response.setContentType("application/json");
	}
	
	private SecretKey generateKey() {
        byte[] originalKey = "Alguna.Clave.Secreta.123456".getBytes(StandardCharsets.UTF_8);
        byte[] finalKey = new byte[256];
        System.arraycopy(originalKey, 0, finalKey, 0, originalKey.length);
        return Keys.hmacShaKeyFor(finalKey);
    } 
	
}