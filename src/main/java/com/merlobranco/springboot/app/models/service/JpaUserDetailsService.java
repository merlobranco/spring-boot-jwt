package com.merlobranco.springboot.app.models.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.merlobranco.springboot.app.models.dao.UsuarioDao;
import com.merlobranco.springboot.app.models.entity.Usuario;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UsuarioDao usuarioDao;
	
	@Override
	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioDao.findByUsername(username);
		
		if (usuario == null) {
			log.error("Error login: no existe el usuario '" + username + "'");
			throw new UsernameNotFoundException("Username " + username + " no existe en el sistema!");
		}
		
		List<GrantedAuthority> authorities = usuario.getRoles()
												.stream()
												.map(r -> new SimpleGrantedAuthority(r.getAuthority()))
												.collect(Collectors.toList());
		
		if (authorities.isEmpty()) {
			log.error("Error login: usuario '" + username + "' no tiene roles asignados!");
			throw new UsernameNotFoundException("Usuario '" + username + "' no tiene roles asignados!");
		}
		
		return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
	}

}
