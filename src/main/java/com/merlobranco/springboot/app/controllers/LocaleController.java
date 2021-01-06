package com.merlobranco.springboot.app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LocaleController {

	@GetMapping("/locale")
	public String locale(HttpServletRequest request) {
		// Getting the last url just before changing the language
		String ultimaUrl = request.getHeader("referer");
		return "redirect:".concat(ultimaUrl);
	}
}
