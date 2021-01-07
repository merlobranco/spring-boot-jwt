package com.merlobranco.springboot.app.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merlobranco.springboot.app.auth.SimpleGrantedAuthorityMixin;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTServiceImpl implements JWTService {

	@Override
	public String create(Authentication auth) throws IOException {
		SecretKey secretKey = generateKey();
		
//		String username = authResult.getName();
		String username = ((User)auth.getPrincipal()).getUsername();
		Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
		
		Claims claims = Jwts.claims();
		claims.put("authorities", new ObjectMapper().writeValueAsString(roles));
		
		String token = Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + (3600000 * 4))) // 4 Hours
				.signWith(secretKey)
				.compact();
		
		return token;
	}

	@Override
	public boolean validate(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {}
		return false;
	}

	@Override
	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(generateKey())
				.build()
				.parseClaimsJws(resolve(token))
				.getBody();
	}

	@Override
	public String getUsername(String token) {
		return getClaims(token).getSubject();
	}

	@Override
	public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException {
		Object roles = getClaims(token).get("authorities");
		return Arrays.asList(new ObjectMapper()
				.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
				.readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));
	}

	@Override
	public String resolve(String token) {
		return token != null ? token.replace("Bearer ", "") : null;
	}

	private SecretKey generateKey() {
        byte[] originalKey = "Alguna.Clave.Secreta.123456".getBytes(StandardCharsets.UTF_8);
        byte[] finalKey = new byte[256];
        System.arraycopy(originalKey, 0, finalKey, 0, originalKey.length);
        return Keys.hmacShaKeyFor(finalKey);
    } 
}
