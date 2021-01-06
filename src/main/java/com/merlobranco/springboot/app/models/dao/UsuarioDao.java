package com.merlobranco.springboot.app.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.merlobranco.springboot.app.models.entity.Usuario;

public interface UsuarioDao extends CrudRepository<Usuario, Long> {

	public Usuario findByUsername(String username);
}
