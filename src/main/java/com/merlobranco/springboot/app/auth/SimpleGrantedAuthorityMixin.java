package com.merlobranco.springboot.app.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleGrantedAuthorityMixin {

	// Mapping the property that actually contains the role
	@JsonCreator
	public SimpleGrantedAuthorityMixin(@JsonProperty("authority") String role) {}
}
