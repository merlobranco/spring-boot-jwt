package com.merlobranco.springboot.app.auth.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.merlobranco.springboot.app.auth.service.JWTKeys;
import com.merlobranco.springboot.app.auth.service.JWTService;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	
	private JWTService jwtService;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTService jwtService) {
		super(authenticationManager);
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String header = request.getHeader(JWTKeys.HEADER_STRING);
		if (!requiresAuthentication(header)) {
			chain.doFilter(request, response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = null;
		
		// Initializing session with an authenticated token
		if (jwtService.validate(header)) {
			String username = jwtService.getUsername(header);
			Collection<? extends GrantedAuthority> authorities = jwtService.getRoles(header);
			authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
		}
		
		// Authenticating the session
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}
	
	protected boolean requiresAuthentication(String header) {
		return header != null && header.startsWith(JWTKeys.TOKEN_PREFIX);
	}
}
