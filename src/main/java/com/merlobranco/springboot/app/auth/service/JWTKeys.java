package com.merlobranco.springboot.app.auth.service;

import org.springframework.util.Base64Utils;

public class JWTKeys {
	public static final String SECRET= Base64Utils.encodeToString("Alguna.Clave.Secreta.123456".getBytes());
	public static final long EXPIRATION_DATE = 3600000 * 4;
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
}
